# 🚀 Automation Suite AI Agent Server - Detailed Source Code

> 📅 **Generated On:** `2026-09-28 22:17:35`  
> 📂 **Source Root:** `C:/Ranjan/AutomationSuite_v2/automation_suite_ai_agent`  
> 📊 **Total Source Files:** `10`  
> 📝 **Total Lines of Code:** `1,202`  
> 💾 **Total Size:** `50.97 KB`  

---

## 📌 Executive Summary

AI Agent WebSocket Server module running on port 9020. Connects to SpringBoot orchestrator and Chrome Extension, resolves target host selection and remediation prompt tasks, and manages AI execution pipelines.

---

## 📂 Project Directory Structure

```text
automation_suite_ai_agent/
├── config.properties
├── pom.xml
├── run_ai_agent.bat
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── automation/
│                   └── aiagent/
│                       ├── Main.java
│                       ├── constants/
│                       │   └── EventConstants.java
│                       ├── scheduler/
│                       │   └── FileCleanupScheduler.java
│                       ├── server/
│                       │   └── AiAgentServer.java
│                       └── service/
│                           ├── ChromeMonitorService.java
│                           └── TempFileService.java
└── test_client.html
```

---

## 📑 File Index

1. [`config.properties`](#file-1)
2. [`pom.xml`](#file-2)
3. [`run_ai_agent.bat`](#file-3)
4. [`src/main/java/com/automation/aiagent/Main.java`](#file-4)
5. [`src/main/java/com/automation/aiagent/constants/EventConstants.java`](#file-5)
6. [`src/main/java/com/automation/aiagent/scheduler/FileCleanupScheduler.java`](#file-6)
7. [`src/main/java/com/automation/aiagent/server/AiAgentServer.java`](#file-7)
8. [`src/main/java/com/automation/aiagent/service/ChromeMonitorService.java`](#file-8)
9. [`src/main/java/com/automation/aiagent/service/TempFileService.java`](#file-9)
10. [`test_client.html`](#file-10)

---

## 💻 Comprehensive Source Code

<a id="file-1"></a>
### 1. `config.properties`

**Path:** `config.properties` | **Size:** `0.25 KB` | **Lines:** `4` | **Language:** `properties`

```properties
# AI Agent Configuration
ai.agent.port=9020
ai.agent.temp.path=C:/Ranjan/AutomationSuite_v2/ai_temp_files
ai.agent.allowed.extensions=.txt,.log,.pdf,.doc,.docx,.xlsx,.xls,.csv,.ppt,.pptx,.xml,.html,.htm,.json,.md,.png,.jpg,.jpeg,.webp,.gif,.bmp,.svg,.py,.java

```

---

<a id="file-2"></a>
### 2. `pom.xml`

**Path:** `pom.xml` | **Size:** `2.60 KB` | **Lines:** `68` | **Language:** `xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.automation</groupId>
    <artifactId>automation_suite_ai_agent</artifactId>
    <version>1.0.0</version>
    <name>automation_suite_ai_agent</name>
    <description>Lightweight Plain Java AI Agent Server for Automation Suite V2</description>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Java-WebSocket for WebSocket Server/Client -->
        <dependency>
            <groupId>org.java-websocket</groupId>
            <artifactId>Java-WebSocket</artifactId>
            <version>1.5.6</version>
        </dependency>

        <!-- Jackson Databind for JSON Processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.16.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.automation.aiagent.Main</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

```

---

<a id="file-3"></a>
### 3. `run_ai_agent.bat`

**Path:** `run_ai_agent.bat` | **Size:** `0.56 KB` | **Lines:** `17` | **Language:** `cmd`

```cmd
@echo off
title Automation Suite AI Agent Server (Port 9020)
echo ===================================================
echo  Starting Automation Suite AI Agent Server...
echo ===================================================

cd /d "%~dp0"

echo [INFO] Checking for stale process on Port 9020...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr /R /C:":9020 " ^| findstr LISTENING') do (
    echo [INFO] Killing process PID %%a on Port 9020...
    taskkill /F /PID %%a >nul 2>&1
)
timeout /t 1 /nobreak >nul

java -cp "target/classes;lib/*" com.automation.aiagent.Main
pause

```

---

<a id="file-4"></a>
### 4. `src/main/java/com/automation/aiagent/Main.java`

**Path:** `src/main/java/com/automation/aiagent/Main.java` | **Size:** `2.83 KB` | **Lines:** `69` | **Language:** `java`

```java
package com.automation.aiagent;

import com.automation.aiagent.scheduler.FileCleanupScheduler;
import com.automation.aiagent.server.AiAgentServer;
import com.automation.aiagent.service.ChromeMonitorService;
import com.automation.aiagent.service.TempFileService;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Main Entry Point for Plain Java AI Agent Server.
 * Runs on Port 9020 without Spring Boot.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" Starting Automation Suite AI Agent Server...");
        System.out.println("=================================================");

        Properties props = loadConfigProperties();

        int port = Integer.parseInt(props.getProperty("ai.agent.port", "9020"));
        String tempPath = props.getProperty("ai.agent.temp.path", "C:/Ranjan/AutomationSuite_v2/ai_temp_files");
        String allowedExts = props.getProperty("ai.agent.allowed.extensions",
                ".txt,.log,.pdf,.doc,.docx,.xlsx,.xls,.csv,.ppt,.pptx,.xml,.html,.htm,.json,.md,.png,.jpg,.jpeg,.webp,.gif,.bmp,.svg,.py,.java");

        // 1. Initialize Temp File Service
        TempFileService tempFileService = new TempFileService(tempPath, allowedExts);

        // 2. Initialize File Cleanup Scheduler
        FileCleanupScheduler fileCleanupScheduler = new FileCleanupScheduler(tempFileService);
        fileCleanupScheduler.start();

        // 3. Initialize Chrome Monitor Service
        ChromeMonitorService chromeMonitorService = new ChromeMonitorService();
        chromeMonitorService.start();

        // 4. Initialize Dual WebSocket Server on Port 9020
        AiAgentServer agentServer = new AiAgentServer(port, tempFileService, chromeMonitorService);
        agentServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Main] Shutting down AI Agent Server...");
            agentServer.stopServer();
            chromeMonitorService.stop();
            fileCleanupScheduler.stop();
            System.out.println("[Main] Shutdown complete.");
        }));
    }

    private static Properties loadConfigProperties() {
        Properties props = new Properties();
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                System.out.println("[Main] Loaded config.properties");
            } catch (Exception e) {
                System.err.println("[Main] Error reading config.properties: " + e.getMessage());
            }
        } else {
            System.out.println("[Main] config.properties not found. Using default settings.");
        }
        return props;
    }
}

```

---

<a id="file-5"></a>
### 5. `src/main/java/com/automation/aiagent/constants/EventConstants.java`

**Path:** `src/main/java/com/automation/aiagent/constants/EventConstants.java` | **Size:** `1.75 KB` | **Lines:** `47` | **Language:** `java`

```java
package com.automation.aiagent.constants;

/**
 * Centralized Event Constants for WebSocket Communication.
 * Guaranteed no string typos across AI Agent, SpringBoot, and Chrome Extension.
 */
public class EventConstants {

    // Extension Heartbeat
    public static final String EXTSN_PING = "EXTSN_PING";
    public static final String EXTSN_PONG = "EXTSN_PONG";

    // AI Agent Health Status pushed to SpringBoot
    public static final String AI_HEALTH = "AI_HEALTH";

    // Task Commands & Results
    public static final String EXECUTE_TASK = "EXECUTE_TASK";
    public static final String TASK_RESULT = "TASK_RESULT";

    // Delete Conversation Events
    public static final String EXECUTE_DELETE = "EXECUTE_DELETE";
    public static final String DELETE_RESPONSE = "DELETE_RESPONSE";
    public static final String EXTSN_DELETE = "EXTSN_DELETE";
    public static final String EXTSN_DELETE_RESPONSE = "EXTSN_DELETE_RESPONSE";

    // Minimization Toggle Command
    public static final String SET_MINIMIZABLE = "SET_MINIMIZABLE";

    // Commands to Extension
    public static final String EXTSN_CHAT = "EXTSN_CHAT";
    public static final String EXTSN_SENT = "EXTSN_SENT";
    public static final String EXTSN_CHAT_RESPONSE = "EXTSN_CHAT_RESPONSE";

    // File Processing Events
    public static final String EXTSN_FILE_REQUEST = "EXTSN_FILE_REQUEST";
    public static final String EXTSN_FILE_RESPONSE = "EXTSN_FILE_RESPONSE";

    // Status Values
    public static final String STATUS_OK = "OK";
    public static final String STATUS_NOT_OK = "NOT_OK";
    public static final String STATUS_RESPONSE = "RESPONSE";
    public static final String STATUS_FAILED = "FAILED";

    private EventConstants() {
        // Private constructor to prevent instantiation
    }
}

```

---

<a id="file-6"></a>
### 6. `src/main/java/com/automation/aiagent/scheduler/FileCleanupScheduler.java`

**Path:** `src/main/java/com/automation/aiagent/scheduler/FileCleanupScheduler.java` | **Size:** `2.32 KB` | **Lines:** `67` | **Language:** `java`

```java
package com.automation.aiagent.scheduler;

import com.automation.aiagent.service.TempFileService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Hourly Temp File Cleanup Scheduler.
 * Scans C:\Ranjan\AutomationSuite_v2\ai_temp_files every 1 hour and deletes files older than 1 hour.
 */
public class FileCleanupScheduler {

    private static final long ONE_HOUR_MS = 3600000L;
    private final TempFileService tempFileService;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public FileCleanupScheduler(TempFileService tempFileService) {
        this.tempFileService = tempFileService;
    }

    public void start() {
        // Run cleanup every 1 hour (initial delay 10 minutes)
        executor.scheduleAtFixedRate(this::cleanOldFiles, 10, 60, TimeUnit.MINUTES);
        System.out.println("[FileCleanupScheduler] Initialized hourly file cleanup scheduler.");
    }

    public void cleanOldFiles() {
        try {
            Path tempDir = tempFileService.getTempDirPath();
            if (!Files.exists(tempDir)) return;

            File[] files = tempDir.toFile().listFiles();
            if (files == null || files.length == 0) return;

            long now = System.currentTimeMillis();
            int deletedCount = 0;

            for (File file : files) {
                if (file.isFile()) {
                    long ageMs = now - file.lastModified();
                    if (ageMs > ONE_HOUR_MS) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            deletedCount++;
                            System.out.println("[FileCleanupScheduler] Deleted expired temp file (age > 1hr): " + file.getName());
                        }
                    }
                }
            }

            if (deletedCount > 0) {
                System.out.println("[FileCleanupScheduler] Cleaned up " + deletedCount + " temp file(s).");
            }
        } catch (Exception e) {
            System.err.println("[FileCleanupScheduler] Error during temp file cleanup: " + e.getMessage());
        }
    }

    public void stop() {
        executor.shutdown();
    }
}

```

---

<a id="file-7"></a>
### 7. `src/main/java/com/automation/aiagent/server/AiAgentServer.java`

**Path:** `src/main/java/com/automation/aiagent/server/AiAgentServer.java` | **Size:** `23.83 KB` | **Lines:** `514` | **Language:** `java`

```java
package com.automation.aiagent.server;

import com.automation.aiagent.constants.EventConstants;
import com.automation.aiagent.service.ChromeMonitorService;
import com.automation.aiagent.service.TempFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Single Port 9020 Dual WebSocket Server.
 * Implements Parallel Execution Pool (up to 10 active tasks) with Task Queue, TaskId tracking,
 * and Conversation Deletion support.
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

        public TaskContext(String taskId, String conversationId, String prompt, List<String> filesPath, WebSocket clientSocket) {
            this(taskId, conversationId, prompt, filesPath, clientSocket, "CHAT");
        }

        public TaskContext(String taskId, String conversationId, String prompt, List<String> filesPath, WebSocket clientSocket, String taskType) {
            this.taskId = taskId;
            this.conversationId = conversationId;
            this.prompt = prompt;
            this.filesPath = filesPath != null ? filesPath : Collections.emptyList();
            this.clientSocket = clientSocket;
            this.enqueuedAtMs = System.currentTimeMillis();
            this.taskType = taskType != null ? taskType : "CHAT";

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
    }

    private static final int MAX_CONCURRENT_TASKS = 10;
    private final TempFileService tempFileService;
    private final ChromeMonitorService chromeMonitorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebSocket extensionSession = null;
    private final Set<WebSocket> springBootSessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile long lastExtsnPingTime = 0L;

    // Concurrency Task Queue & Cached Concurrent Running Tasks Map
    private final Queue<TaskContext> taskQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, TaskContext> runningTasksMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService healthScheduler = Executors.newSingleThreadScheduledExecutor();

    public AiAgentServer(int port, TempFileService tempFileService, ChromeMonitorService chromeMonitorService) {
        super(new InetSocketAddress(port));
        this.tempFileService = tempFileService;
        this.chromeMonitorService = chromeMonitorService;
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
            }

        } catch (Exception e) {
            System.err.println("[AiAgentServer] Error processing incoming message: " + e.getMessage());
        }
    }

    private void handleExecuteTaskFromSpringBoot(WebSocket conn, Map<String, Object> payload) {
        String taskId = (String) payload.get("taskId");
        if (taskId == null || taskId.trim().isEmpty()) {
            taskId = "task_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6);
        }

        String conversationId = (String) payload.get("conversationId");
        String prompt = (String) payload.get("prompt");
        @SuppressWarnings("unchecked")
        List<String> filesPath = (List<String>) payload.get("files_path");
        if (filesPath == null) filesPath = Collections.emptyList();

        System.out.println("[AiAgentServer] Received EXECUTE_TASK [TaskId: " + taskId + ", ConvId: " + conversationId + "]");

        // Duplicate Conversation Rejection Check
        if (isConversationActiveOrQueued(conversationId)) {
            System.err.println("[AiAgentServer] Task REJECTED - conversationId '" + conversationId + "' is already active or queued.");
            sendTaskResultToSpringBoot(EventConstants.STATUS_FAILED, taskId, conversationId, null,
                    "Task rejected: Conversation '" + conversationId + "' already has an active or queued task.", 0, System.currentTimeMillis(), conn);
            return;
        }

        TaskContext ctx = new TaskContext(taskId, conversationId, prompt, filesPath, conn, "CHAT");
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
        sendTaskResultToSpringBoot(status, taskId, conversationId, response, error, responseTimeMs, System.currentTimeMillis(), clientConn);

        dispatchNextTasks();
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

```

---

<a id="file-8"></a>
### 8. `src/main/java/com/automation/aiagent/service/ChromeMonitorService.java`

**Path:** `src/main/java/com/automation/aiagent/service/ChromeMonitorService.java` | **Size:** `7.34 KB` | **Lines:** `175` | **Language:** `java`

```java
package com.automation.aiagent.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Chrome Monitor & Minimization Service.
 * Periodically checks Chrome process and window state using Win32 API via PowerShell.
 * Returns JSON {"running": true/false, "minimized": true/false}.
 */
public class ChromeMonitorService {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean minimizable = false;
    private volatile boolean taskExecuting = false;
    private volatile String currentChromeState = "UNKNOWN";

    private static final String SCRIPT_STATE_CHECK =
            "if (-not ([System.Management.Automation.PSTypeName]'WinState').Type) {\n" +
            "    Add-Type 'using System; using System.Runtime.InteropServices; public class WinState { [DllImport(\"user32.dll\")] public static extern bool IsIconic(IntPtr hWnd); }';\n" +
            "}\n" +
            "$procs = Get-Process chrome -ErrorAction SilentlyContinue;\n" +
            "if (-not $procs) {\n" +
            "    '{\"running\":false,\"minimized\":false}'\n" +
            "} else {\n" +
            "    $minimized = $false;\n" +
            "    $hasWindow = $false;\n" +
            "    foreach ($p in $procs) {\n" +
            "        if ($p.MainWindowHandle -ne [IntPtr]::Zero) {\n" +
            "            $hasWindow = $true;\n" +
            "            if ([WinState]::IsIconic($p.MainWindowHandle)) {\n" +
            "                $minimized = $true;\n" +
            "            } else {\n" +
            "                $minimized = $false;\n" +
            "                break;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    if (-not $hasWindow) {\n" +
            "        '{\"running\":true,\"minimized\":false}'\n" +
            "    } elseif ($minimized) {\n" +
            "        '{\"running\":true,\"minimized\":true}'\n" +
            "    } else {\n" +
            "        '{\"running\":true,\"minimized\":false}'\n" +
            "    }\n" +
            "}\n";

    private static final String SCRIPT_MINIMIZE =
            "Add-Type '[DllImport(\"user32.dll\")] public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);' -Name Win32Min -Namespace NativeMin -ErrorAction SilentlyContinue;\n" +
            "Get-Process chrome -ErrorAction SilentlyContinue | Where-Object {$_.MainWindowHandle -ne 0} | ForEach-Object {[NativeMin.Win32Min]::ShowWindowAsync($_.MainWindowHandle, 6)}\n";

    private static final String SCRIPT_RESTORE =
            "Add-Type '[DllImport(\"user32.dll\")] public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);' -Name Win32Res -Namespace NativeRes -ErrorAction SilentlyContinue;\n" +
            "Get-Process chrome -ErrorAction SilentlyContinue | Where-Object {$_.MainWindowHandle -ne 0} | ForEach-Object {[NativeRes.Win32Res]::ShowWindowAsync($_.MainWindowHandle, 9)}\n";

    public void start() {
        executor.scheduleAtFixedRate(this::checkAndUpdateChromeState, 0, 2, TimeUnit.SECONDS);
        System.out.println("[ChromeMonitorService] Chrome state monitor started.");
    }

    public void checkAndUpdateChromeState() {
        try {
            String jsonOutput = executePowerShellScript(SCRIPT_STATE_CHECK);
            if (jsonOutput == null || jsonOutput.isEmpty()) return;

            boolean running = jsonOutput.contains("\"running\":true");
            boolean minimized = jsonOutput.contains("\"minimized\":true");

            if (!running) {
                currentChromeState = "NOT RUNNING";
                System.out.println("[ChromeMonitorService] Chrome is NOT RUNNING. Auto-launching Chrome...");
                launchChrome();
            } else if (minimized) {
                currentChromeState = "RUNNING - MINIMIZED";
            } else {
                currentChromeState = "RUNNING - NOT MINIMIZED";
                if (minimizable && !taskExecuting) {
                    System.out.println("[ChromeMonitorService] Chrome running not minimized. Minimizing per minimizable=true...");
                    minimizeChrome();
                }
            }
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Error during Chrome monitor cycle: " + e.getMessage());
        }
    }

    public void setTaskExecuting(boolean taskExecuting) {
        this.taskExecuting = taskExecuting;
        System.out.println("[ChromeMonitorService] Task executing flag set to: " + taskExecuting);
    }

    public boolean isTaskExecuting() {
        return taskExecuting;
    }

    public void launchChrome() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "chrome.exe", "https://copilot.com/chat");
            pb.start();
            System.out.println("[ChromeMonitorService] Launched chrome.exe to https://copilot.com/chat");
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Could not launch Chrome: " + e.getMessage());
        }
    }

    public void minimizeChrome() {
        try {
            executePowerShellScript(SCRIPT_MINIMIZE);
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Error minimizing Chrome: " + e.getMessage());
        }
    }

    public void restoreChrome() {
        try {
            executePowerShellScript(SCRIPT_RESTORE);
        } catch (Exception e) {
            System.err.println("[ChromeMonitorService] Error restoring Chrome: " + e.getMessage());
        }
    }

    public void setMinimizable(boolean minimizable) {
        this.minimizable = minimizable;
        System.out.println("[ChromeMonitorService] Updated minimizable flag to: " + minimizable);
        if (!minimizable) {
            restoreChrome();
        } else {
            minimizeChrome();
        }
    }

    public boolean isMinimizable() {
        return minimizable;
    }

    public String getCurrentChromeState() {
        return currentChromeState;
    }

    private String executePowerShellScript(String script) {
        try {
            byte[] bytes = script.getBytes(StandardCharsets.UTF_16LE);
            String encoded = Base64.getEncoder().encodeToString(bytes);

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encoded);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                        return trimmed;
                    }
                    sb.append(line).append("\n");
                }
            }
            process.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void stop() {
        executor.shutdown();
    }
}

```

---

<a id="file-9"></a>
### 9. `src/main/java/com/automation/aiagent/service/TempFileService.java`

**Path:** `src/main/java/com/automation/aiagent/service/TempFileService.java` | **Size:** `4.04 KB` | **Lines:** `107` | **Language:** `java`

```java
package com.automation.aiagent.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Temp File Service.
 * Manages copying local files to C:\Ranjan\AutomationSuite_v2\ai_temp_files,
 * appends .txt for forbidden file extensions, enforces max 3 file limit, and provides Base64 data.
 */
public class TempFileService {

    private final Path tempDirPath;
    private final Set<String> allowedExtensions;

    public TempFileService(String tempPathStr, String allowedExtsStr) {
        this.tempDirPath = Paths.get(tempPathStr);
        this.allowedExtensions = new HashSet<>();

        if (allowedExtsStr != null && !allowedExtsStr.trim().isEmpty()) {
            for (String ext : allowedExtsStr.split(",")) {
                allowedExtensions.add(ext.trim().toLowerCase());
            }
        }

        try {
            Files.createDirectories(tempDirPath);
            System.out.println("[TempFileService] Initialized temp dir: " + tempDirPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[TempFileService] Could not create temp dir: " + e.getMessage());
        }
    }

    public List<String> processAndCopyFiles(List<String> originalPaths) throws IllegalArgumentException, IOException {
        if (originalPaths == null || originalPaths.isEmpty()) {
            return Collections.emptyList();
        }

        if (originalPaths.size() > 3) {
            throw new IllegalArgumentException("Max 3 files allowed. Provided: " + originalPaths.size());
        }

        List<String> tempFilePaths = new ArrayList<>();

        for (String originalPathStr : originalPaths) {
            if (originalPathStr == null || originalPathStr.trim().isEmpty()) continue;

            Path srcPath = Paths.get(originalPathStr.trim());
            if (!Files.exists(srcPath) || !Files.isRegularFile(srcPath)) {
                throw new IOException("File not found on disk: " + originalPathStr);
            }

            String originalFileName = srcPath.getFileName().toString();
            String ext = "";
            int dotIdx = originalFileName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalFileName.substring(dotIdx).toLowerCase();
            }

            String targetFileName = originalFileName;
            if (!ext.isEmpty() && !allowedExtensions.contains(ext)) {
                // Append .txt if extension is forbidden (e.g. xyz.bat -> xyz.bat.txt)
                targetFileName = originalFileName + ".txt";
                System.out.println("[TempFileService] Forbidden extension '" + ext + "'. Disguising file as: " + targetFileName);
            }

            Path destPath = tempDirPath.resolve(targetFileName);
            Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            tempFilePaths.add(destPath.toAbsolutePath().toString());
            System.out.println("[TempFileService] Copied '" + originalFileName + "' to temp path: " + destPath);
        }

        return tempFilePaths;
    }

    public Map<String, Object> readFileData(String filePathStr) throws IOException {
        Path path = Paths.get(filePathStr);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("File not found in temp storage: " + filePathStr);
        }

        byte[] fileBytes = Files.readAllBytes(path);
        String base64 = Base64.getEncoder().encodeToString(fileBytes);
        String fileName = path.getFileName().toString();
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("fileName", fileName);
        map.put("mimeType", mimeType);
        map.put("size", fileBytes.length);
        map.put("base64", base64);
        return map;
    }

    public Path getTempDirPath() {
        return tempDirPath;
    }
}

```

---

<a id="file-10"></a>
### 10. `test_client.html`

**Path:** `test_client.html` | **Size:** `5.44 KB` | **Lines:** `134` | **Language:** `html`

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>SpringBoot AI Agent WebSocket Test Client</title>
  <style>
    body { font-family: 'Segoe UI', sans-serif; background: #0f172a; color: #f8fafc; padding: 20px; }
    h1 { color: #38bdf8; font-size: 1.5rem; margin-bottom: 15px; }
    .status-card { background: #1e293b; padding: 15px; border-radius: 8px; border: 1px solid #475569; margin-bottom: 20px; }
    .badge { padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 0.8rem; }
    .badge-ok { background: #166534; color: #4ade80; }
    .badge-notok { background: #991b1b; color: #fca5a5; }
    .form-group { margin-bottom: 15px; }
    label { display: block; margin-bottom: 5px; color: #94a3b8; font-size: 0.9rem; }
    input[type="text"], textarea { width: 100%; padding: 10px; background: #334155; border: 1px solid #475569; border-radius: 6px; color: #fff; font-size: 0.9rem; box-sizing: border-box; }
    textarea { height: 100px; }
    .btn { padding: 10px 18px; background: #0284c7; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; margin-right: 10px; }
    .btn:hover { background: #0369a1; }
    .log-box { background: #020617; border: 1px solid #334155; padding: 15px; height: 300px; overflow-y: auto; font-family: monospace; font-size: 0.85rem; color: #38bdf8; border-radius: 6px; }
    .log-entry { margin-bottom: 6px; border-bottom: 1px solid #1e293b; padding-bottom: 4px; }
  </style>
</head>
<body>
  <h1>🤖 SpringBoot AI Agent WebSocket Test Client</h1>

  <div class="status-card">
    <div>WebSocket Server Connection: <span id="ws-status" class="badge badge-notok">DISCONNECTED</span></div>
    <div style="margin-top: 8px;">Extension Health Status: <span id="health-status" class="badge badge-notok">NOT_OK</span></div>
    <div style="margin-top: 8px;">Chrome Window State: <strong id="chrome-state">Unknown</strong></div>
  </div>

  <div class="form-group">
    <label>Conversation ID (or null)</label>
    <input type="text" id="conv-id" value="null">
  </div>

  <div class="form-group">
    <label>Prompt</label>
    <textarea id="prompt-input">Hello Copilot, reply with exactly the word APPLE.</textarea>
  </div>

  <div class="form-group">
    <label>Files Array (comma separated paths)</label>
    <input type="text" id="files-input" placeholder="e.g. C:\Ranjan\AutomationSuite_v2\ai_chrome_extension\sample_files_for_testing_file_attach\test_sample.txt">
  </div>

  <div class="form-group">
    <button class="btn" onclick="sendExecuteTask()">🚀 Send EXECUTE_TASK to AI Agent</button>
    <button class="btn" style="background: #475569;" onclick="toggleMinimizable(true)">Minimize Chrome</button>
    <button class="btn" style="background: #475569;" onclick="toggleMinimizable(false)">Disable Minimization</button>
  </div>

  <h2>📜 Live Event Log</h2>
  <div id="log-box" class="log-box"></div>

  <script>
    let ws = null;

    function connect() {
      ws = new WebSocket("ws://127.0.0.1:9020/ws/ai_agent");

      ws.onopen = () => {
        document.getElementById('ws-status').className = 'badge badge-ok';
        document.getElementById('ws-status').innerText = 'CONNECTED';
        logMsg("Connected to ws://127.0.0.1:9020/ws/ai_agent");
      };

      ws.onmessage = (event) => {
        logMsg("Received message: " + event.data);
        try {
          const data = JSON.parse(event.data);
          if (data.event === "AI_HEALTH") {
            const hEl = document.getElementById('health-status');
            hEl.innerText = data.status;
            hEl.className = data.status === 'OK' ? 'badge badge-ok' : 'badge badge-notok';
            document.getElementById('chrome-state').innerText = data.chromeState || 'Unknown';
          }
        } catch (e) {}
      };

      ws.onclose = () => {
        document.getElementById('ws-status').className = 'badge badge-notok';
        document.getElementById('ws-status').innerText = 'DISCONNECTED';
        logMsg("Disconnected. Reconnecting in 3s...");
        setTimeout(connect, 3000);
      };
    }

    function sendExecuteTask() {
      let convId = document.getElementById('conv-id').value.trim();
      if (convId.toLowerCase() === 'null' || !convId) convId = null;
      const prompt = document.getElementById('prompt-input').value.trim();
      const filesStr = document.getElementById('files-input').value.trim();
      const files_path = filesStr ? filesStr.split(',').map(s => s.trim()).filter(Boolean) : [];

      const payload = {
        event: "EXECUTE_TASK",
        conversationId: convId,
        prompt: prompt,
        files_path: files_path
      };

      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(payload));
        logMsg("Sent EXECUTE_TASK: " + JSON.stringify(payload));
      } else {
        alert("WebSocket is not connected!");
      }
    }

    function toggleMinimizable(flag) {
      const payload = {
        event: "SET_MINIMIZABLE",
        minimizable: flag
      };
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(payload));
        logMsg("Sent SET_MINIMIZABLE: " + JSON.stringify(payload));
      }
    }

    function logMsg(msg) {
      const logBox = document.getElementById('log-box');
      const div = document.createElement('div');
      div.className = 'log-entry';
      div.innerText = `[${new Date().toLocaleTimeString()}] ${msg}`;
      logBox.appendChild(div);
      logBox.scrollTop = logBox.scrollHeight;
    }

    connect();
  </script>
</body>
</html>

```

---
