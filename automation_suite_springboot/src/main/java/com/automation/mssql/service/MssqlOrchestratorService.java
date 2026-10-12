package com.automation.mssql.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.automation.mssql.client.MssqlAgentClientManager;
import com.automation.mssql.entity.MssqlQueryAuditLog;
import com.automation.mssql.repository.MssqlQueryAuditLogRepository;
import com.automation.service.GlobalUiWebSocketChannel;
import com.automation.service.GlobalUiWebSocketListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;

@Service
public class MssqlOrchestratorService implements GlobalUiWebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(MssqlOrchestratorService.class);

    private final GlobalUiWebSocketChannel channelService;
    private final MssqlAgentClientManager agentClientManager;
    private final MssqlQueryAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class TaskRoutingInfo {
        public final String taskId;
        public final String sessionId;
        public final String userId;
        public final String host;
        public final String environment;

        public TaskRoutingInfo(String taskId, String sessionId, String userId, String host, String environment) {
            this.taskId = taskId;
            this.sessionId = sessionId;
            this.userId = userId;
            this.host = host;
            this.environment = environment;
        }
    }

    private final ConcurrentHashMap<String, TaskRoutingInfo> activeTaskRoutings = new ConcurrentHashMap<>();

    public MssqlOrchestratorService(GlobalUiWebSocketChannel channelService,
                                    MssqlAgentClientManager agentClientManager,
                                    MssqlQueryAuditLogRepository auditLogRepository) {
        this.channelService = channelService;
        this.agentClientManager = agentClientManager;
        this.auditLogRepository = auditLogRepository;
    }

    @PostConstruct
    public void registerSelf() {
        channelService.registerListener(this);
        log.info("MssqlOrchestratorService registered as GlobalUiWebSocketListener");
    }

    @Override
    public void onUiMessage(WebSocketSession session, String eventType, JsonNode payload, String rawMessage) {
        if (eventType == null || !eventType.startsWith("MSSQL_")) {
            return;
        }

        log.info("Received UI MSSQL event: {} from session: {}", eventType, session != null ? session.getId() : "null");

        switch (eventType) {
            case "MSSQL_EXECUTE_QUERY" -> handleExecuteQuery(session, payload);
            case "MSSQL_GET_DATABASES" -> handleGetDatabases(session, payload);
            case "MSSQL_GET_TABLES" -> handleGetTables(session, payload);
            case "MSSQL_GET_GENERAL_QUERY" -> handleGetGeneralQuery(session, payload);
            case "MSSQL_GET_STATUS" -> handleGetStatus(session);
            default -> log.debug("Unhandled MSSQL event: {}", eventType);
        }
    }

    private String selectLowestQueueHost() {
        List<String> connectedHosts = agentClientManager.getConnectedHosts();
        if (connectedHosts.isEmpty()) {
            return null;
        }

        return connectedHosts.stream()
                .min(Comparator.comparingInt(agentClientManager::getHostQueueCount))
                .orElse(connectedHosts.get(0));
    }

    private void handleExecuteQuery(WebSocketSession session, JsonNode payload) {
        String taskId = payload.has("taskId") ? payload.get("taskId").asText() : UUID.randomUUID().toString();
        String userId = payload.has("userId") ? payload.get("userId").asText() : "unknown";
        String environment = payload.has("environment") ? payload.get("environment").asText() : "prod";
        String sql = payload.has("sql") ? payload.get("sql").asText() : "";

        if (sql.isBlank()) {
            sendError(session, taskId, environment, "SQL query cannot be empty.");
            return;
        }

        String chosenHost = selectLowestQueueHost();
        if (chosenHost == null) {
            sendError(session, taskId, environment, "No MSSQL Agent hosts are currently connected or available.");
            return;
        }

        // Audit logging: strictly input SQL only, userId or userId_ASR_AI
        try {
            MssqlQueryAuditLog logEntry = new MssqlQueryAuditLog(
                    taskId, userId, environment, chosenHost, sql, System.currentTimeMillis()
            );
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to persist MSSQL audit log for task {}: {}", taskId, e.getMessage());
        }

        // Route mapping
        String sessId = session != null ? session.getId() : null;
        activeTaskRoutings.put(taskId, new TaskRoutingInfo(taskId, sessId, userId, chosenHost, environment));

        try {
            ObjectNode agentReq = objectMapper.createObjectNode();
            agentReq.put("event", "EXECUTE_QUERY");
            agentReq.put("taskId", taskId);
            agentReq.put("userId", userId);
            agentReq.put("environment", environment);
            agentReq.put("sql", sql);

            boolean sent = agentClientManager.sendToHost(chosenHost, objectMapper.writeValueAsString(agentReq));
            if (!sent) {
                sendError(session, taskId, environment, "Failed to dispatch query to MSSQL host: " + chosenHost);
                activeTaskRoutings.remove(taskId);
            }
        } catch (Exception e) {
            log.error("Error creating agent query payload for task {}: {}", taskId, e.getMessage());
            sendError(session, taskId, environment, "Internal error preparing MSSQL query: " + e.getMessage());
            activeTaskRoutings.remove(taskId);
        }
    }

    private void handleGetDatabases(WebSocketSession session, JsonNode payload) {
        String taskId = payload.has("taskId") ? payload.get("taskId").asText() : UUID.randomUUID().toString();
        String userId = payload.has("userId") ? payload.get("userId").asText() : "unknown";
        String environment = payload.has("environment") ? payload.get("environment").asText() : "prod";

        String chosenHost = selectLowestQueueHost();
        if (chosenHost == null) {
            sendError(session, taskId, environment, "No MSSQL Agent hosts are currently connected or available.");
            return;
        }

        String sessId = session != null ? session.getId() : null;
        activeTaskRoutings.put(taskId, new TaskRoutingInfo(taskId, sessId, userId, chosenHost, environment));

        try {
            ObjectNode agentReq = objectMapper.createObjectNode();
            agentReq.put("event", "GET_DATABASES");
            agentReq.put("taskId", taskId);
            agentReq.put("userId", userId);
            agentReq.put("environment", environment);

            boolean sent = agentClientManager.sendToHost(chosenHost, objectMapper.writeValueAsString(agentReq));
            if (!sent) {
                sendError(session, taskId, environment, "Failed to dispatch GET_DATABASES to MSSQL host: " + chosenHost);
                activeTaskRoutings.remove(taskId);
            }
        } catch (Exception e) {
            log.error("Error preparing GET_DATABASES: {}", e.getMessage());
            sendError(session, taskId, environment, "Internal error: " + e.getMessage());
            activeTaskRoutings.remove(taskId);
        }
    }

    private void handleGetTables(WebSocketSession session, JsonNode payload) {
        String taskId = payload.has("taskId") ? payload.get("taskId").asText() : UUID.randomUUID().toString();
        String userId = payload.has("userId") ? payload.get("userId").asText() : "unknown";
        String environment = payload.has("environment") ? payload.get("environment").asText() : "prod";
        String database = payload.has("database") ? payload.get("database").asText() : "master";

        String chosenHost = selectLowestQueueHost();
        if (chosenHost == null) {
            sendError(session, taskId, environment, "No MSSQL Agent hosts are currently connected or available.");
            return;
        }

        String sessId = session != null ? session.getId() : null;
        activeTaskRoutings.put(taskId, new TaskRoutingInfo(taskId, sessId, userId, chosenHost, environment));

        try {
            ObjectNode agentReq = objectMapper.createObjectNode();
            agentReq.put("event", "GET_TABLES");
            agentReq.put("taskId", taskId);
            agentReq.put("userId", userId);
            agentReq.put("environment", environment);
            agentReq.put("database", database);

            boolean sent = agentClientManager.sendToHost(chosenHost, objectMapper.writeValueAsString(agentReq));
            if (!sent) {
                sendError(session, taskId, environment, "Failed to dispatch GET_TABLES to MSSQL host: " + chosenHost);
                activeTaskRoutings.remove(taskId);
            }
        } catch (Exception e) {
            log.error("Error preparing GET_TABLES: {}", e.getMessage());
            sendError(session, taskId, environment, "Internal error: " + e.getMessage());
            activeTaskRoutings.remove(taskId);
        }
    }

    private void handleGetGeneralQuery(WebSocketSession session, JsonNode payload) {
        String taskId = payload.has("taskId") ? payload.get("taskId").asText() : UUID.randomUUID().toString();
        String userId = payload.has("userId") ? payload.get("userId").asText() : "unknown";
        String environment = payload.has("environment") ? payload.get("environment").asText() : "prod";
        String database = payload.has("database") ? payload.get("database").asText() : "master";
        String table = payload.has("table") ? payload.get("table").asText() : "Table";

        String chosenHost = selectLowestQueueHost();
        if (chosenHost == null) {
            sendError(session, taskId, environment, "No MSSQL Agent hosts are currently connected or available.");
            return;
        }

        String sessId = session != null ? session.getId() : null;
        activeTaskRoutings.put(taskId, new TaskRoutingInfo(taskId, sessId, userId, chosenHost, environment));

        try {
            ObjectNode agentReq = objectMapper.createObjectNode();
            agentReq.put("event", "GET_GENERAL_QUERY");
            agentReq.put("taskId", taskId);
            agentReq.put("userId", userId);
            agentReq.put("environment", environment);
            agentReq.put("database", database);
            agentReq.put("table", table);

            boolean sent = agentClientManager.sendToHost(chosenHost, objectMapper.writeValueAsString(agentReq));
            if (!sent) {
                sendError(session, taskId, environment, "Failed to dispatch GET_GENERAL_QUERY to MSSQL host: " + chosenHost);
                activeTaskRoutings.remove(taskId);
            }
        } catch (Exception e) {
            log.error("Error preparing GET_GENERAL_QUERY: {}", e.getMessage());
            sendError(session, taskId, environment, "Internal error: " + e.getMessage());
            activeTaskRoutings.remove(taskId);
        }
    }

    private void handleGetStatus(WebSocketSession session) {
        try {
            ObjectNode res = objectMapper.createObjectNode();
            res.put("type", "mssqlAgentStatus");
            res.put("connectedCount", agentClientManager.getConnectedHosts().size());
            res.set("queueCounts", objectMapper.valueToTree(agentClientManager.getQueueCountMap()));
            sendToSessionOrBroadcast(session, objectMapper.writeValueAsString(res));
        } catch (Exception e) {
            log.error("Failed to build MSSQL status: {}", e.getMessage());
        }
    }

    public void handleAgentMessage(String host, String rawMessage) {
        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            String type = root.has("type") ? root.get("type").asText() : "";
            String taskId = root.has("taskId") ? root.get("taskId").asText() : null;

            TaskRoutingInfo routing = null;
            if (taskId != null) {
                routing = activeTaskRoutings.get(taskId);
            }

            // If taskId wasn't in root but is in active tasks, find by host
            if (routing == null) {
                for (TaskRoutingInfo info : activeTaskRoutings.values()) {
                    if (host.equalsIgnoreCase(info.host)) {
                        routing = info;
                        break;
                    }
                }
            }

            // Add taskId to outbound frame if missing so UI can match it
            String messageToSend = rawMessage;
            if (routing != null && (!root.has("taskId") || root.get("taskId").isNull())) {
                if (root.isObject()) {
                    ((ObjectNode) root).put("taskId", routing.taskId);
                    messageToSend = objectMapper.writeValueAsString(root);
                }
            }

            // Relay to UI session
            boolean delivered = false;
            if (routing != null && routing.sessionId != null) {
                WebSocketSession session = channelService.getUiSessions().get(routing.sessionId);
                if (session != null && session.isOpen()) {
                    channelService.sendToSession(session, messageToSend);
                    delivered = true;
                }
            }

            if (!delivered) {
                channelService.broadcastToUi(messageToSend);
            }

            // Check if task is finished
            boolean isEnd = (root.has("end") && root.get("end").asBoolean())
                    || "closed".equalsIgnoreCase(type)
                    || "databases".equalsIgnoreCase(type)
                    || "tables".equalsIgnoreCase(type)
                    || "generalQuery".equalsIgnoreCase(type);

            if (isEnd && routing != null) {
                activeTaskRoutings.remove(routing.taskId);
            }

        } catch (Exception e) {
            log.error("Failed to relay MSSQL agent message from host {}: {}", host, e.getMessage(), e);
        }
    }

    private void sendError(WebSocketSession session, String taskId, String env, String errorMsg) {
        String json = String.format(
                "{\"type\":\"output\",\"taskId\":\"%s\",\"env\":\"%s\",\"data\":[],\"rowCount\":0,\"end\":true,\"success\":false,\"error\":\"%s\"}",
                taskId, env != null ? env : "prod", errorMsg.replace("\"", "\\\"")
        );
        sendToSessionOrBroadcast(session, json);
    }

    private void sendToSessionOrBroadcast(WebSocketSession session, String json) {
        if (session != null && session.isOpen()) {
            channelService.sendToSession(session, json);
        } else {
            channelService.broadcastToUi(json);
        }
    }

    @Override
    public void onSessionDisconnected(WebSocketSession session, String reason) {
        if (session == null) return;
        String sessId = session.getId();
        activeTaskRoutings.values().removeIf(routing -> sessId.equals(routing.sessionId));
    }
}
