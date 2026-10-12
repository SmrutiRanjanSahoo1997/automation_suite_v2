package com.automation.mssql.server;

import java.net.InetSocketAddress;
import java.util.UUID;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.mssql.model.MssqlTask;
import com.automation.mssql.queue.MssqlQueueManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MssqlWebSocketServer extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(MssqlWebSocketServer.class);

    private final MssqlQueueManager queueManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long startTime;

    public MssqlWebSocketServer(int port, MssqlQueueManager queueManager) {
        super(new InetSocketAddress(port));
        this.queueManager = queueManager;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log.info("Client connected: {} (URI: {})", conn.getRemoteSocketAddress(), handshake.getResourceDescriptor());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Client disconnected: {} (code={}, reason={})", conn.getRemoteSocketAddress(), code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String event = root.has("event") ? root.get("event").asText() : "";

            if ("GET_STATUS".equalsIgnoreCase(event)) {
                long uptimeSec = (System.currentTimeMillis() - startTime) / 1000;
                String activeId = queueManager.getActiveTaskId();
                String statusJson = String.format(
                        "{\"type\":\"status\",\"queueCount\":%d,\"activeTaskId\":%s,\"uptimeSeconds\":%d}",
                        queueManager.getQueueCount(),
                        activeId != null ? "\"" + activeId + "\"" : "null",
                        uptimeSec
                );
                conn.send(statusJson);
                return;
            }

            String taskId = root.has("taskId") ? root.get("taskId").asText() : UUID.randomUUID().toString();
            String userId = root.has("userId") ? root.get("userId").asText() : "unknown";
            String environment = root.has("environment") ? root.get("environment").asText() : "prod";
            String database = root.has("database") ? root.get("database").asText() : null;
            String table = root.has("table") ? root.get("table").asText() : null;
            String sql = root.has("sql") ? root.get("sql").asText() : null;

            MssqlTask task = new MssqlTask(taskId, event, userId, environment, database, table, sql, conn);
            queueManager.enqueue(task);

        } catch (Exception e) {
            log.error("Failed to parse or process incoming WebSocket message: {}", message, e);
            try {
                conn.send("{\"type\":\"error\",\"message\":\"Invalid message format: " + e.getMessage() + "\"}");
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("WebSocket server error on conn {}: {}", conn != null ? conn.getRemoteSocketAddress() : "null", ex.getMessage());
    }

    @Override
    public void onStart() {
        log.info("MSSQL Agent WebSocket Server started successfully on port {}", getPort());
    }
}
