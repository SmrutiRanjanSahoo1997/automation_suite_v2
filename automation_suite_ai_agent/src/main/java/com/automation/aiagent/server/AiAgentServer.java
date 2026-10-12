package com.automation.aiagent.server;

import com.automation.aiagent.constants.EventConstants;
import com.automation.aiagent.service.ChromeMonitorService;
import com.automation.aiagent.service.ClickVerification;
import com.automation.aiagent.service.RunbookService;
import com.automation.aiagent.service.TempFileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Single Port 9020 Dual WebSocket Server.
 * Implements Parallel Execution Pool (up to 10 active tasks) with Task Queue, TaskId tracking,
 * Conversation Deletion support, and Runbook Management.
 */
public class AiAgentServer extends WebSocketServer {

    public static class TaskContext {
        private final String taskId;
        private final String conversationId;
        private final String prompt;
        private final List<String> filesPath;
        private final String trackingKey;
        private final WebSocket clientSocket;
        private final long enqueuedAtMs;
        private final String taskType; // "CHAT" or "DELETE"
        private final String userId;

        public TaskContext(String taskId, String conversationId, String prompt, List<String> filesPath, WebSocket clientSocket) {
            this(taskId, conversationId, prompt, filesPath, clientSocket, "CHAT", "default");
        }

        public TaskContext(String taskId, String conversationId, String prompt, List<String> filesPath, WebSocket clientSocket, String taskType) {
            this(taskId, conversationId, prompt, filesPath, clientSocket, taskType, "default");
        }

        public TaskContext(String taskId, String conversationId, String prompt, List<String> filesPath, WebSocket clientSocket, String taskType, String userId) {
            this.taskId = taskId;
            this.conversationId = conversationId;
            this.prompt = prompt;
            this.filesPath = filesPath != null ? filesPath : Collections.emptyList();
            this.clientSocket = clientSocket;
            this.enqueuedAtMs = System.currentTimeMillis();
            this.taskType = taskType != null ? taskType : "CHAT";
            this.userId = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : "default";

            boolean isConvValid = conversationId != null && !conversationId.trim().isEmpty() && !"null".equalsIgnoreCase(conversationId);
            this.trackingKey = isConvValid ? conversationId.trim() : taskId;
        }

        public String getTaskId() { return taskId; }
        public String getConversationId() { return conversationId; }
        public String getPrompt() { return prompt; }
        public List<String> getFilesPath() { return filesPath; }
        public String getTrackingKey() { return trackingKey; }
        public WebSocket getClientSocket() { return clientSocket; }
        public long getEnqueuedAtMs() { return enqueuedAtMs; }
        public String getTaskType() { return taskType; }
        public String getUserId() { return userId; }
    }

    private static final int MAX_CONCURRENT_TASKS = 10;
    private final TempFileService tempFileService;
    private final ChromeMonitorService chromeMonitorService;
    private final RunbookService runbookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebSocket extensionSession = null;
    private final Set<WebSocket> springBootSessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile long lastExtsnPingTime = 0L;

    // Concurrency Task Queue & Cached Concurrent Running Tasks Map
    private final Queue<TaskContext> taskQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, TaskContext> runningTasksMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService healthScheduler = Executors.newSingleThreadScheduledExecutor();

    public AiAgentServer(int port, TempFileService tempFileService, ChromeMonitorService chromeMonitorService) {
        this(port, tempFileService, chromeMonitorService, null);
    }

    public AiAgentServer(int port, TempFileService tempFileService, ChromeMonitorService chromeMonitorService, RunbookService runbookService) {
        super(new InetSocketAddress(port));
        this.tempFileService = tempFileService;
        this.chromeMonitorService = chromeMonitorService;
        this.runbookService = runbookService;
        setConnectionLostTimeout(0);
        setReuseAddr(true);
    }

    @Override
    public void onStart() {
        System.out.println("=================================================");
        System.out.println(" AI Agent WebSocket Server Running on Port: " + getPort());
        System.out.println(" Concurrency Limit: " + MAX_CONCURRENT_TASKS + " Parallel Tasks");
        System.out.println(" Endpoints:");
        System.out.println("   Extension:  ws://127.0.0.1:" + getPort() + "/ws/extension");
        System.out.println("   SpringBoot: ws://0.0.0.0:" + getPort() + "/ws/ai_agent");
        System.out.println("=================================================");

        healthScheduler.scheduleAtFixedRate(this::pushHealthStatusToSpringBoot, 5, 60, TimeUnit.SECONDS);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String resourceDescriptor = handshake.getResourceDescriptor();
        System.out.println("[AiAgentServer] New client connected on path: " + resourceDescriptor + " from " + conn.getRemoteSocketAddress());

        if (resourceDescriptor != null && resourceDescriptor.startsWith("/ws/extension")) {
            this.extensionSession = conn;
            this.lastExtsnPingTime = System.currentTimeMillis();
            System.out.println("[AiAgentServer] Extension WebSocket connected!");
            pushHealthStatusToSpringBoot();
            dispatchNextTasks();
        } else if (resourceDescriptor != null && resourceDescriptor.startsWith("/ws/ai_agent")) {
            this.springBootSessions.add(conn);
            System.out.println("[AiAgentServer] SpringBoot WebSocket client connected!");
            pushHealthStatusToSpringBoot();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[AiAgentServer] Client disconnected: " + conn.getRemoteSocketAddress() + " (Reason: " + reason + ")");
        if (conn == extensionSession) {
            extensionSession = null;
            System.out.println("[AiAgentServer] Extension WebSocket disconnected!");
            pushHealthStatusToSpringBoot();
        } else {
            springBootSessions.remove(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String event = (String) payload.get("event");

            if (event == null) {
                event = (String) payload.get("action");
            }

            if (EventConstants.EXTSN_PING.equals(event)) {
                lastExtsnPingTime = System.currentTimeMillis();
                Map<String, Object> pong = new HashMap<>();
                pong.put("event", EventConstants.EXTSN_PONG);
                pong.put("status", EventConstants.STATUS_OK);
                conn.send(objectMapper.writeValueAsString(pong));

            } else if (EventConstants.EXECUTE_TASK.equals(event)) {
                handleExecuteTaskFromSpringBoot(conn, payload);

            } else if (EventConstants.EXECUTE_DELETE.equals(event)) {
                handleExecuteDeleteFromSpringBoot(conn, payload);

            } else if (EventConstants.EXTSN_CHAT_RESPONSE.equals(event)) {
                handleChatResponseFromExtension(payload);

            } else if (EventConstants.EXTSN_DELETE_RESPONSE.equals(event)) {
                handleDeleteResponseFromExtension(payload);

            } else if (EventConstants.EXTSN_SENT.equals(event)) {
                handleExtsnSentFromExtension(payload);

            } else if (EventConstants.SET_MINIMIZABLE.equals(event)) {
                Boolean min = (Boolean) payload.get("minimizable");
                if (min != null) {
                    chromeMonitorService.setMinimizable(min);
                }

            } else if (EventConstants.EXTSN_FILE_REQUEST.equals(event)) {
                handleFileRequestFromExtension(conn, payload);

            } else if (EventConstants.EXTSN_VERIFICATION_REQUIRED.equals(event)) {
                handleVerificationRequiredFromExtension(conn, payload);

            } else if (EventConstants.AI_RUNBOOKS_ROOT_PATH.equals(event) || "AI_RUNBOOKS_ROOT_PATH".equals(event)) {
                handleRunbooksRootPathRequest(conn);
            }

        } catch (Exception e) {
            System.err.println("[AiAgentServer] Error processing incoming message: " + e.getMessage());
        }
    }

    private void handleRunbooksRootPathRequest(WebSocket conn) {
        String rootPath = runbookService != null && runbookService.getRawRunbookPath() != null
                ? runbookService.getRawRunbookPath()
                : "C:/Ranjan/AutomationSuite_v2/Runbooks";
        Map<String, Object> resp = new HashMap<>();
        resp.put("event", EventConstants.AI_RUNBOOKS_ROOT_PATH);
        resp.put("event-type", EventConstants.AI_RUNBOOKS_ROOT_PATH);
        resp.put("path", rootPath);
        try {
            String json = objectMapper.writeValueAsString(resp);
            if (conn != null && conn.isOpen()) {
                conn.send(json);
            } else {
                for (WebSocket sbConn : springBootSessions) {
                    if (sbConn.isOpen()) {
                        sbConn.send(json);
                    }
                }
            }
            System.out.println("[AiAgentServer] Responded with AI_RUNBOOKS_ROOT_PATH: " + rootPath);
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Error responding with AI_RUNBOOKS_ROOT_PATH: " + e.getMessage());
        }
    }

    private void handleExecuteTaskFromSpringBoot(WebSocket conn, Map<String, Object> payload) {
        String taskId = (String) payload.get("taskId");
        if (taskId == null || taskId.trim().isEmpty()) {
            taskId = "task_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
        }

        String conversationId = (String) payload.get("conversationId");
        String prompt = (String) payload.get("prompt");
        String userId = (String) payload.get("userId");
        Boolean attachCatalog = (Boolean) payload.get("attach_runbook_catalog");

        @SuppressWarnings("unchecked")
        List<String> filesPath = (List<String>) payload.get("files_needed");
        if (filesPath == null || filesPath.isEmpty()) {
            filesPath = (List<String>) payload.get("files_path");
        }
        if ((filesPath == null || filesPath.isEmpty()) && Boolean.TRUE.equals(attachCatalog) && runbookService != null) {
            filesPath = new ArrayList<>();
            filesPath.add(runbookService.getContentsFile().getAbsolutePath());
            filesPath.add(runbookService.getUserContentsFile(userId).getAbsolutePath());
            System.out.println("[AiAgentServer] Automatically attached Runbook catalog files for User: " + userId);
        } else if (filesPath == null) {
            filesPath = Collections.emptyList();
        }

        System.out.println("[AiAgentServer] Received EXECUTE_TASK [TaskId: " + taskId + ", ConvId: " + conversationId + "]");

        // Duplicate Conversation Rejection Check
        if (isConversationActiveOrQueued(conversationId)) {
            System.err.println("[AiAgentServer] Task REJECTED - conversationId '" + conversationId + "' is already active or queued.");
            sendTaskResultToSpringBoot(EventConstants.STATUS_FAILED, taskId, conversationId, null,
                    "Task rejected: Conversation '" + conversationId + "' already has an active or queued task.", 0, System.currentTimeMillis(), conn);
            return;
        }

        TaskContext ctx = new TaskContext(taskId, conversationId, prompt, filesPath, conn, "CHAT", userId);
        taskQueue.add(ctx);
        System.out.println("[AiAgentServer] Task enqueued [TaskId: " + taskId + "]. Queue size: " + taskQueue.size());

        dispatchNextTasks();
    }

    private void handleExecuteDeleteFromSpringBoot(WebSocket conn, Map<String, Object> payload) {
        String conversationId = (String) payload.get("conversationId");
        boolean isConvValid = conversationId != null && !conversationId.trim().isEmpty() && !"null".equalsIgnoreCase(conversationId);

        if (!isConvValid) {
            System.err.println("[AiAgentServer] Delete REJECTED - conversationId is mandatory.");
            sendDeleteResponseToSpringBoot(EventConstants.STATUS_FAILED, conversationId, null, "conversationId is mandatory for delete operation.", conn);
            return;
        }

        if (isConversationActiveOrQueued(conversationId)) {
            System.err.println("[AiAgentServer] Delete REJECTED - conversationId '" + conversationId + "' is already active or queued.");
            sendDeleteResponseToSpringBoot(EventConstants.STATUS_FAILED, conversationId, null, "Conversation is already active or queued.", conn);
            return;
        }

        String taskId = "del_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
        TaskContext ctx = new TaskContext(taskId, conversationId, null, null, conn, "DELETE");
        taskQueue.add(ctx);
        System.out.println("[AiAgentServer] Delete task enqueued [TaskId: " + taskId + ", ConvId: " + conversationId + "]. Queue size: " + taskQueue.size());

        dispatchNextTasks();
    }

    private boolean isConversationActiveOrQueued(String convId) {
        if (convId == null || convId.trim().isEmpty() || "null".equalsIgnoreCase(convId)) {
            return false;
        }
        String trimmed = convId.trim();
        if (runningTasksMap.containsKey(trimmed)) {
            return true;
        }
        for (TaskContext ctx : taskQueue) {
            if (trimmed.equalsIgnoreCase(ctx.getConversationId())) {
                return true;
            }
        }
        return false;
    }

    private synchronized void dispatchNextTasks() {
        while (runningTasksMap.size() < MAX_CONCURRENT_TASKS && !taskQueue.isEmpty()) {
            TaskContext ctx = taskQueue.poll();
            if (ctx == null) break;

            if (extensionSession == null || !extensionSession.isOpen()) {
                System.err.println("[AiAgentServer] Cannot dispatch task [" + ctx.getTaskId() + "]: Extension disconnected.");
                if ("DELETE".equals(ctx.getTaskType())) {
                    sendDeleteResponseToSpringBoot(EventConstants.STATUS_FAILED, ctx.getConversationId(), null, "Extension is disconnected or unreachable.", ctx.getClientSocket());
                } else {
                    sendTaskResultToSpringBoot(EventConstants.STATUS_FAILED, ctx.getTaskId(), ctx.getConversationId(), null,
                            "Extension is disconnected or unreachable.", 0, System.currentTimeMillis(), ctx.getClientSocket());
                }
                continue;
            }

            // Register task in runningTasksMap under both trackingKey and taskId
            runningTasksMap.put(ctx.getTrackingKey(), ctx);
            if (!ctx.getTrackingKey().equals(ctx.getTaskId())) {
                runningTasksMap.put(ctx.getTaskId(), ctx);
            }

            if ("DELETE".equals(ctx.getTaskType())) {
                chromeMonitorService.setTaskExecuting(true);
                chromeMonitorService.restoreChrome();

                try {
                    Map<String, Object> extMsg = new HashMap<>();
                    extMsg.put("event", EventConstants.EXTSN_DELETE);
                    extMsg.put("taskId", ctx.getTaskId());
                    extMsg.put("conversationId", ctx.getConversationId());

                    extensionSession.send(objectMapper.writeValueAsString(extMsg));
                    System.out.println("[AiAgentServer] Dispatched EXTSN_DELETE to Extension [TaskId: " + ctx.getTaskId() + ", ConvId: " + ctx.getConversationId() + "]. Active running tasks: " + runningTasksMap.size());
                } catch (Exception e) {
                    chromeMonitorService.setTaskExecuting(false);
                    runningTasksMap.remove(ctx.getTrackingKey());
                    runningTasksMap.remove(ctx.getTaskId());
                    sendDeleteResponseToSpringBoot(EventConstants.STATUS_FAILED, ctx.getConversationId(), null, "Failed to send delete command to Extension: " + e.getMessage(), ctx.getClientSocket());
                }
                continue;
            }

            // For CHAT tasks: Pause auto-minimization and restore Chrome
            List<String> tempPaths = Collections.emptyList();
            if (!ctx.getFilesPath().isEmpty()) {
                try {
                    tempPaths = tempFileService.processAndCopyFiles(ctx.getFilesPath());
                } catch (Exception e) {
                    System.err.println("[AiAgentServer] File processing failed for taskId " + ctx.getTaskId() + ": " + e.getMessage());
                    sendTaskResultToSpringBoot(EventConstants.STATUS_FAILED, ctx.getTaskId(), ctx.getConversationId(), null,
                            "File processing failed: " + e.getMessage(), 0, System.currentTimeMillis(), ctx.getClientSocket());
                    continue;
                }
            }

            chromeMonitorService.setTaskExecuting(true);
            chromeMonitorService.restoreChrome();

            // Allow OS buffer for Chrome to restore and gain foreground focus before prompt arrival
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}

            try {
                Map<String, Object> extMsg = new HashMap<>();
                extMsg.put("event", EventConstants.EXTSN_CHAT);
                extMsg.put("taskId", ctx.getTaskId());
                extMsg.put("conversationId", ctx.getConversationId());
                extMsg.put("prompt", ctx.getPrompt());
                extMsg.put("files_path", tempPaths);

                extensionSession.send(objectMapper.writeValueAsString(extMsg));
                System.out.println("[AiAgentServer] Dispatched EXTSN_CHAT to Extension [TaskId: " + ctx.getTaskId() + ", ConvId: " + ctx.getConversationId() + "]. Active running tasks: " + runningTasksMap.size());
            } catch (Exception e) {
                chromeMonitorService.setTaskExecuting(false);
                runningTasksMap.remove(ctx.getTrackingKey());
                runningTasksMap.remove(ctx.getTaskId());
                sendTaskResultToSpringBoot(EventConstants.STATUS_FAILED, ctx.getTaskId(), ctx.getConversationId(), null,
                        "Failed to send command to Extension: " + e.getMessage(), 0, System.currentTimeMillis(), ctx.getClientSocket());
            }
        }
    }

    private void handleExtsnSentFromExtension(Map<String, Object> payload) {
        String taskId = (String) payload.get("taskId");
        System.out.println("[AiAgentServer] Received EXTSN_SENT for taskId: " + taskId);
        chromeMonitorService.setTaskExecuting(false);
        if (chromeMonitorService.isMinimizable()) {
            System.out.println("[AiAgentServer] Prompt sent. Minimizing Chrome per minimizable=true...");
            chromeMonitorService.minimizeChrome();
        }
    }

    private void handleChatResponseFromExtension(Map<String, Object> payload) {
        chromeMonitorService.setTaskExecuting(false);
        String status = (String) payload.getOrDefault("status", EventConstants.STATUS_RESPONSE);
        String taskId = (String) payload.get("taskId");
        String conversationId = (String) payload.get("conversationId");
        String response = (String) payload.get("response");
        String error = (String) payload.get("error");
        Number responseTimeMsNum = (Number) payload.get("responseTimeMs");
        long responseTimeMs = responseTimeMsNum != null ? responseTimeMsNum.longValue() : 0L;

        TaskContext ctx = null;
        if (taskId != null) {
            ctx = runningTasksMap.remove(taskId);
        }
        if (ctx != null) {
            runningTasksMap.remove(ctx.getTrackingKey());
        } else if (conversationId != null) {
            ctx = runningTasksMap.remove(conversationId);
        }

        WebSocket clientConn = ctx != null ? ctx.getClientSocket() : null;
        String user = ctx != null ? ctx.getUserId() : "default";

        // Execute any AI runbook actions (create, append, line-update)
        processPotentialRunbookAction(response, user);

        sendTaskResultToSpringBoot(status, taskId, conversationId, response, error, responseTimeMs, System.currentTimeMillis(), clientConn);

        dispatchNextTasks();
    }

    private void processPotentialRunbookAction(String response, String userId) {
        if (response == null || runbookService == null) return;
        try {
            String trimmed = response.trim();
            if (trimmed.contains("runbook_action")) {
                if (trimmed.contains("```")) {
                    int firstBrace = trimmed.indexOf('{');
                    int lastBrace = trimmed.lastIndexOf('}');
                    if (firstBrace >= 0 && lastBrace > firstBrace) {
                        trimmed = trimmed.substring(firstBrace, lastBrace + 1);
                    }
                }
                String sanitized = trimmed.replaceAll("\\\\(?![\"\\\\/bfnrtu]|u[0-9a-fA-F]{4})", "\\\\\\\\");
                JsonNode rootNode = objectMapper.readTree(sanitized);
                if (rootNode.has("runbook_action") && !rootNode.get("runbook_action").isNull()) {
                    JsonNode actionNode = rootNode.get("runbook_action");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> actionMap = objectMapper.convertValue(actionNode, Map.class);
                    Map<String, Object> actionRes = runbookService.applyRunbookAction(actionMap, userId);
                    System.out.println("[AiAgentServer] Applied runbook_action result: " + actionRes);
                }
            }
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Could not parse/apply runbook_action: " + e.getMessage());
        }
    }

    private void handleDeleteResponseFromExtension(Map<String, Object> payload) {
        chromeMonitorService.setTaskExecuting(false);
        if (chromeMonitorService.isMinimizable()) {
            System.out.println("[AiAgentServer] Delete task finished. Re-minimizing Chrome per minimizable=true...");
            chromeMonitorService.minimizeChrome();
        }

        String status = (String) payload.getOrDefault("status", EventConstants.STATUS_RESPONSE);
        String taskId = (String) payload.get("taskId");
        String conversationId = (String) payload.get("conversationId");
        String message = (String) payload.get("message");
        if (message == null && EventConstants.STATUS_RESPONSE.equals(status)) {
            message = "The conversation deleted";
        }
        String error = (String) payload.get("error");

        TaskContext ctx = null;
        if (taskId != null) {
            ctx = runningTasksMap.remove(taskId);
        }
        if (ctx != null) {
            runningTasksMap.remove(ctx.getTrackingKey());
        } else if (conversationId != null) {
            ctx = runningTasksMap.remove(conversationId);
        }

        WebSocket clientConn = ctx != null ? ctx.getClientSocket() : null;
        sendDeleteResponseToSpringBoot(status, conversationId, message, error, clientConn);

        dispatchNextTasks();
    }

    private void sendTaskResultToSpringBoot(String status, String taskId, String conversationId, String response, String error, long responseTimeMs, long responseAt, WebSocket targetConn) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("event", EventConstants.TASK_RESULT);
            result.put("taskId", taskId);
            result.put("status", status);
            result.put("conversationId", conversationId);
            result.put("response", response);
            result.put("error", error);
            result.put("responseTimeMs", responseTimeMs);
            result.put("responseAt", responseAt);

            String jsonStr = objectMapper.writeValueAsString(result);

            if (targetConn != null && targetConn.isOpen()) {
                targetConn.send(jsonStr);
            } else {
                for (WebSocket sbConn : springBootSessions) {
                    if (sbConn.isOpen()) {
                        sbConn.send(jsonStr);
                    }
                }
            }
            System.out.println("[AiAgentServer] Pushed TASK_RESULT [TaskId: " + taskId + ", Status: " + status + "]");
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Could not send TASK_RESULT: " + e.getMessage());
        }
    }

    private void sendDeleteResponseToSpringBoot(String status, String conversationId, String message, String error, WebSocket targetConn) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("event", EventConstants.DELETE_RESPONSE);
            result.put("status", status);
            result.put("conversationId", conversationId);
            result.put("message", message);
            result.put("error", error);

            String jsonStr = objectMapper.writeValueAsString(result);

            if (targetConn != null && targetConn.isOpen()) {
                targetConn.send(jsonStr);
            } else {
                for (WebSocket sbConn : springBootSessions) {
                    if (sbConn.isOpen()) {
                        sbConn.send(jsonStr);
                    }
                }
            }
            System.out.println("[AiAgentServer] Pushed DELETE_RESPONSE [ConvId: " + conversationId + ", Status: " + status + "]");
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Could not send DELETE_RESPONSE: " + e.getMessage());
        }
    }

    private void handleFileRequestFromExtension(WebSocket conn, Map<String, Object> payload) {
        String path = (String) payload.get("path");
        Map<String, Object> resp = new HashMap<>();
        resp.put("event", EventConstants.EXTSN_FILE_RESPONSE);
        resp.put("path", path);

        try {
            Map<String, Object> fileData = tempFileService.readFileData(path);
            resp.putAll(fileData);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getMessage());
        }

        try {
            conn.send(objectMapper.writeValueAsString(resp));
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Failed to send EXTSN_FILE_RESPONSE: " + e.getMessage());
        }
    }

    private void handleVerificationRequiredFromExtension(WebSocket conn, Map<String, Object> payload) {
        String taskId = (String) payload.get("taskId");
        Number screenXNum = (Number) payload.get("screenX");
        Number screenYNum = (Number) payload.get("screenY");
        double screenX = screenXNum != null ? screenXNum.doubleValue() : 0.0;
        double screenY = screenYNum != null ? screenYNum.doubleValue() : 0.0;

        System.out.println("[AiAgentServer] Received EXTSN_VERIFICATION_REQUIRED for taskId: " + taskId + " at (" + screenX + ", " + screenY + "). Bringing Chrome to foreground and executing verification clicker...");

        // Ensure Chrome is brought to foreground and unminimized
        chromeMonitorService.restoreChrome();

        // Run Java verification clicker
        try {
            ClickVerification.clickVerificationBox(screenX, screenY);
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Error executing verification clicker: " + e.getMessage());
        }

        // Send confirmation back to Extension
        try {
            Map<String, Object> resp = new HashMap<>();
            resp.put("event", EventConstants.EXTSN_VERIFICATION_RESOLVED);
            resp.put("status", EventConstants.STATUS_OK);
            resp.put("taskId", taskId);
            conn.send(objectMapper.writeValueAsString(resp));
            System.out.println("[AiAgentServer] Sent EXTSN_VERIFICATION_RESOLVED to Extension for taskId: " + taskId);
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Failed to send EXTSN_VERIFICATION_RESOLVED: " + e.getMessage());
        }
    }

    public void pushHealthStatusToSpringBoot() {
        try {
            boolean extConnected = isExtensionConnected();
            String healthStatus = extConnected ? EventConstants.STATUS_OK : EventConstants.STATUS_NOT_OK;

            Map<String, Object> health = new HashMap<>();
            health.put("event", EventConstants.AI_HEALTH);
            health.put("status", healthStatus);
            health.put("extsnConnected", extConnected);
            health.put("chromeState", chromeMonitorService.getCurrentChromeState());
            health.put("activeTasks", runningTasksMap.size());
            health.put("queuedTasks", taskQueue.size());
            health.put("timestamp", System.currentTimeMillis());

            String jsonStr = objectMapper.writeValueAsString(health);
            for (WebSocket sbConn : springBootSessions) {
                if (sbConn.isOpen()) {
                    sbConn.send(jsonStr);
                }
            }
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Could not push AI_HEALTH: " + e.getMessage());
        }
    }

    public boolean isExtensionConnected() {
        if (extensionSession == null || !extensionSession.isOpen()) {
            return false;
        }
        return (System.currentTimeMillis() - lastExtsnPingTime) < 90000L;
    }

    public void stopServer() {
        try {
            healthScheduler.shutdownNow();
            super.stop();
        } catch (Exception e) {
            System.err.println("[AiAgentServer] Error stopping server: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[AiAgentServer] WebSocket Server Error on connection " + (conn != null ? conn.getRemoteSocketAddress() : "null") + ": " + ex.getMessage());
    }
}
