package com.automation.asrai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.automation.aiagent.client.AiAgentWebSocketClient;
import com.automation.aiagent.client.AiAgentWebSocketListener;
import com.automation.aiagent.constants.AiAgentWebSocketEvents;
import com.automation.asrai.protocol.AsrAiProtocol.AiExecuteCmdResponse;
import com.automation.service.GlobalUiWebSocketChannel;
import com.automation.service.GlobalUiWebSocketListener;
import com.automation.service.TerminalOrchestratorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

/**
 * ASR_AI Orchestrator Service.
 * Acts as the bridge between React UI (/ws/app) and AI Agent Server (Port 9020).
 * Implements UiWebSocketListener for handling UI messages and AiAgentWebSocketListener for receiving AI Agent events.
 */
@Service
public class AsrAiOrchestratorService implements GlobalUiWebSocketListener, AiAgentWebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(AsrAiOrchestratorService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final com.automation.aiagent.client.AiAgentWebSocketChannel aiAgentChannel;
    private final GlobalUiWebSocketChannel globalUiWebSocketChannel;
    private final TerminalOrchestratorService terminalOrchestratorService;
    private final PromptTemplateService promptTemplateService;

    public AsrAiOrchestratorService(com.automation.aiagent.client.AiAgentWebSocketChannel aiAgentChannel,
                                    GlobalUiWebSocketChannel globalUiWebSocketChannel,
                                    @Lazy TerminalOrchestratorService terminalOrchestratorService,
                                    PromptTemplateService promptTemplateService) {
        this.aiAgentChannel = aiAgentChannel;
        this.globalUiWebSocketChannel = globalUiWebSocketChannel;
        this.terminalOrchestratorService = terminalOrchestratorService;
        this.promptTemplateService = promptTemplateService;
    }

    @PostConstruct
    public void init() {
        log.info("Registering AsrAiOrchestratorService with GlobalUiWebSocketChannel and AiAgentWebSocketChannel");
        globalUiWebSocketChannel.registerListener(this);
        aiAgentChannel.registerListener(this);
    }

    // =========================================================================
    // GlobalUiWebSocketListener Implementation (UI Messages -> AI Agent / Terminal)
    // =========================================================================

    @Override
    public void onUiMessage(WebSocketSession session, String eventType, JsonNode payload, String rawMessage) {
        if (eventType == null) return;
        String upper = eventType.toUpperCase();
        boolean isAiEvent = upper.startsWith("AI_") ||
               AiAgentWebSocketEvents.AGENT_EXECUTE_TASK.equalsIgnoreCase(upper) ||
               AiAgentWebSocketEvents.AGENT_EXECUTE_DELETE.equalsIgnoreCase(upper);
        if (!isAiEvent) {
            // Not an AI event, ignore silently
            return;
        }

        log.info("AsrAiOrchestratorService processing UI event: {}", wireEventName(eventType));

        switch (upper) {
            case AiAgentWebSocketEvents.UI_AI_EXECUTE_TASK, AiAgentWebSocketEvents.AGENT_EXECUTE_TASK ->
                handleExecuteTaskFromUi(payload);

            case AiAgentWebSocketEvents.UI_AI_DELETE_REQ, AiAgentWebSocketEvents.AGENT_EXECUTE_DELETE, "AI_DELETE_CONVERSATION_REQ" ->
                handleDeleteTaskFromUi(payload);

            case "AI_EXECUTE_CMD_REQ" ->
                processExecutionOnAiTerminal(payload);

            case AiAgentWebSocketEvents.UI_AI_RUNBOOKS_ROOT_PATH ->
                handleRunbooksRootPathFromUi(payload);

            default ->
                log.debug("No custom handling required for UI event: {}", eventType);
        }
    }

    private void handleExecuteTaskFromUi(JsonNode payload) {
        String rawConvId = payload.path("conversationId").asText(null);
        String conversationId = (rawConvId == null || rawConvId.isBlank() || "null".equalsIgnoreCase(rawConvId)) ? null : rawConvId.trim();
        String taskId = payload.path("taskId").asText("task_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));
        String prompt = payload.path("prompt").asText("");

        // Check if AI Agent Server is connected
        if (!aiAgentChannel.isConnected()) {
            log.warn("AI Agent client is offline. Returning FAILED task result for taskId {}", taskId);
            broadcastTaskResultToUi(taskId, conversationId, "FAILED", null, "AI Agent Server (Port 9020) is offline.");
            return;
        }

        Map<String, Object> req = new HashMap<>();
        req.put("event", AiAgentWebSocketEvents.AGENT_EXECUTE_TASK);
        req.put("taskId", taskId);
        req.put("conversationId", conversationId);
        req.put("prompt", prompt);

        if (payload.has("files_needed") && payload.path("files_needed").isArray()) {
            req.put("files_path", MAPPER.convertValue(payload.path("files_needed"), List.class));
            req.put("files_needed", MAPPER.convertValue(payload.path("files_needed"), List.class));
        } else if (payload.has("files_path") && payload.path("files_path").isArray()) {
            req.put("files_path", MAPPER.convertValue(payload.path("files_path"), List.class));
        }

        boolean sent = aiAgentChannel.sendPayload(req);
        if (!sent) {
            broadcastTaskResultToUi(taskId, conversationId, "FAILED", null, "Failed to send task request to AI Agent Server.");
        }
    }

    private void handleRunbooksRootPathFromUi(JsonNode payload) {
        if (!aiAgentChannel.isConnected()) {
            log.warn("AI Agent client is offline. Cannot query AI_RUNBOOKS_ROOT_PATH.");
            return;
        }
        Map<String, Object> req = new HashMap<>();
        req.put("event", AiAgentWebSocketEvents.AGENT_AI_RUNBOOKS_ROOT_PATH);
        req.put("event-type", AiAgentWebSocketEvents.UI_AI_RUNBOOKS_ROOT_PATH);
        aiAgentChannel.sendPayload(req);
    }

    private void handleDeleteTaskFromUi(JsonNode payload) {
        String rawConvId = payload.path("conversationId").asText(null);
        String conversationId = (rawConvId == null || rawConvId.isBlank() || "null".equalsIgnoreCase(rawConvId)) ? null : rawConvId.trim();

        if (conversationId == null) {
            log.warn("Delete task rejected: conversationId is missing");
            broadcastDeleteResponseToUi(null, "FAILED", null, "conversationId is mandatory for delete operation.");
            return;
        }

        if (!aiAgentChannel.isConnected()) {
            log.warn("AI Agent client is offline. Returning FAILED delete response for convId {}", conversationId);
            broadcastDeleteResponseToUi(conversationId, "FAILED", null, "AI Agent Server (Port 9020) is offline.");
            return;
        }

        Map<String, Object> req = new HashMap<>();
        req.put("event", AiAgentWebSocketEvents.AGENT_EXECUTE_DELETE);
        req.put("conversationId", conversationId);

        boolean sent = aiAgentChannel.sendPayload(req);
        if (!sent) {
            broadcastDeleteResponseToUi(conversationId, "FAILED", null, "Failed to send delete request to AI Agent Server.");
        }
    }

    private void processExecutionOnAiTerminal(JsonNode root) {
        String conversationId = root.path("conversationId").asText("");
        String host = root.path("host").asText("localhost");
        String command = root.path("command").asText("");
        String platform = root.path("platform").asText("PS").toUpperCase();
        String userId = root.path("userId").asText("user");

        String platformSuffix = "CMD".equalsIgnoreCase(platform) ? "CMD" : "PS";
        String reservedTerminalId = userId + "_ASR_AI_" + platformSuffix;

        log.info("Executing AI command on reserved terminal {} at host {}", reservedTerminalId, host);

        AiExecuteCmdResponse resp = new AiExecuteCmdResponse();
        resp.conversationId = conversationId;
        resp.host = host;
        resp.terminalId = reservedTerminalId;

        try {
            String startPayload = String.format(
                    "{\"event-type\":\"terminal_start\",\"terminalid\":\"%s\",\"host\":\"%s\",\"userId\":\"%s\",\"platform\":\"%s\",\"name\":\"ASR AI (%s)\"}",
                    reservedTerminalId, host, userId, platformSuffix, platformSuffix
            );
            terminalOrchestratorService.handleUiMessageDirect(startPayload);

            Thread.sleep(200);

            String execPayload = String.format(
                    "{\"event-type\":\"terminal_execute\",\"terminalid\":\"%s\",\"host\":\"%s\",\"userId\":\"%s\",\"command\":\"%s\"}",
                    reservedTerminalId, host, userId, escapeJson(command)
            );
            terminalOrchestratorService.handleUiMessageDirect(execPayload);

            resp.status = "SUCCESS";

        } catch (Exception e) {
            log.error("Failed to execute command on AI reserved terminal {}: {}", reservedTerminalId, e.getMessage());
            resp.status = "FAILED";
            resp.error = e.getMessage();
        }

        try {
            globalUiWebSocketChannel.broadcastToUi(MAPPER.writeValueAsString(resp));
        } catch (Exception e) {
            log.warn("Failed to broadcast AI_EXECUTE_CMD_RESP: {}", e.getMessage());
        }
    }

    // =========================================================================
    // AiAgentWebSocketListener Implementation (AI Agent -> UI Relay)
    // =========================================================================

    @Override
    public void onConnected(String serverUrl) {
        log.info("AI Agent connected at {}. Broadcasting health update to UI.", serverUrl);
        broadcastHealthStatusToUi("OK", true, 0, 0);
    }

    @Override
    public void onDisconnected(String serverUrl, String reason) {
        log.warn("AI Agent disconnected at {}: {}. Broadcasting health update to UI.", serverUrl, reason);
        broadcastHealthStatusToUi("NOT_OK", false, 0, 0);
    }

    @Override
    public void onMessageReceived(String eventType, JsonNode payload, String rawMessage) {
        if (eventType == null) return;
        String upper = eventType.toUpperCase();

        switch (upper) {
            case AiAgentWebSocketEvents.AGENT_TASK_RESULT, AiAgentWebSocketEvents.UI_AI_TASK_RESULT -> {
                String taskId = payload.path("taskId").asText(null);
                String conversationId = payload.path("conversationId").asText(null);
                String status = payload.path("status").asText("RESPONSE");
                String response = payload.path("response").asText(null);
                String error = payload.path("error").asText(null);
                broadcastTaskResultToUi(taskId, conversationId, status, response, error);
            }
            case AiAgentWebSocketEvents.AGENT_DELETE_RESPONSE, AiAgentWebSocketEvents.UI_AI_DELETE_RESP -> {
                String conversationId = payload.path("conversationId").asText(null);
                String status = payload.path("status").asText("RESPONSE");
                String message = payload.path("message").asText(null);
                String error = payload.path("error").asText(null);
                broadcastDeleteResponseToUi(conversationId, status, message, error);
            }
            case AiAgentWebSocketEvents.AGENT_AI_HEALTH, AiAgentWebSocketEvents.UI_AI_HEALTH_STATUS -> {
                String status = payload.path("status").asText("OK");
                boolean extsnConnected = payload.path("extsnConnected").asBoolean(false);
                int activeTasks = payload.path("activeTasks").asInt(0);
                int queuedTasks = payload.path("queuedTasks").asInt(0);
                broadcastHealthStatusToUi(status, extsnConnected, activeTasks, queuedTasks);
            }
            case AiAgentWebSocketEvents.AGENT_AI_RUNBOOKS_ROOT_PATH -> {
                String path = payload.path("path").asText(null);
                broadcastRunbooksRootPathToUi(path);
            }
            default ->
                log.debug("Unhandled event type from AI Agent: {}", eventType);
        }
    }

    // =========================================================================
    // UI Broadcast Utility Methods
    // =========================================================================

    public void broadcastTaskResultToUi(String taskId, String conversationId, String status, String rawResponse, String error) {
        Map<String, Object> uiMsg = new HashMap<>();
        uiMsg.put("event-type", AiAgentWebSocketEvents.UI_AI_TASK_RESULT);
        uiMsg.put("taskId", taskId);
        uiMsg.put("conversationId", conversationId);
        uiMsg.put("status", status);
        uiMsg.put("rawResponse", rawResponse);
        uiMsg.put("error", error);

        try {
            globalUiWebSocketChannel.broadcastToUi(MAPPER.writeValueAsString(uiMsg));
        } catch (Exception e) {
            log.error("Failed to broadcast AI_TASK_RESULT to UI: {}", e.getMessage());
        }
    }

    public void broadcastDeleteResponseToUi(String conversationId, String status, String message, String error) {
        Map<String, Object> uiMsg = new HashMap<>();
        uiMsg.put("event-type", AiAgentWebSocketEvents.UI_AI_DELETE_RESP);
        uiMsg.put("conversationId", conversationId);
        uiMsg.put("status", status);
        uiMsg.put("message", message);
        uiMsg.put("error", error);

        try {
            globalUiWebSocketChannel.broadcastToUi(MAPPER.writeValueAsString(uiMsg));
        } catch (Exception e) {
            log.error("Failed to broadcast AI_DELETE_RESP to UI: {}", e.getMessage());
        }
    }

    public void broadcastHealthStatusToUi(String status, boolean extsnConnected, int activeTasks, int queuedTasks) {
        Map<String, Object> health = new HashMap<>();
        health.put("event-type", AiAgentWebSocketEvents.UI_AI_HEALTH_STATUS);
        health.put("status", status);
        health.put("extsnConnected", extsnConnected);
        health.put("activeTasks", activeTasks);
        health.put("queuedTasks", queuedTasks);
        health.put("timestamp", System.currentTimeMillis());

        try {
            globalUiWebSocketChannel.broadcastToUi(MAPPER.writeValueAsString(health));
        } catch (Exception e) {
            log.warn("Failed to broadcast ai_health_status to UI: {}", e.getMessage());
        }
    }

    public void broadcastRunbooksRootPathToUi(String path) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("event-type", AiAgentWebSocketEvents.UI_AI_RUNBOOKS_ROOT_PATH);
        msg.put("path", path);
        try {
            globalUiWebSocketChannel.broadcastToUi(MAPPER.writeValueAsString(msg));
        } catch (Exception e) {
            log.error("Failed to broadcast AI_RUNBOOKS_ROOT_PATH to UI: {}", e.getMessage());
        }
    }

    private String wireEventName(String event) {
        return event == null ? "" : event;
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }
}
