package com.automation.aiagent.client;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Spring @Component WebSocket Client connecting to the AI Agent Server running on Port 9020 (ws://127.0.0.1:9020/ws/ai_agent).
 * Includes automatic 5-second reconnection loop and typed listener callbacks.
 */
@Component
public class AiAgentWebSocketClient implements AiAgentWebSocketChannel {

    private static final Logger log = LoggerFactory.getLogger(AiAgentWebSocketClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${ai.agent.ws-url:ws://127.0.0.1:9020/ws/ai_agent}")
    private String serverUrl;

    private InnerWebSocketClient innerClient;
    private final List<AiAgentWebSocketListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean isConnected = false;

    @Override
    public void registerListener(AiAgentWebSocketListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            log.info("Registered AI Agent listener: {}", listener.getClass().getSimpleName());
            if (isConnected) {
                try {
                    listener.onConnected(serverUrl);
                } catch (Exception e) {
                    log.error("Error invoking onConnected for listener {}: {}", listener.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }

    @Override
    public void unregisterListener(AiAgentWebSocketListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            log.info("Unregistered AI Agent listener: {}", listener.getClass().getSimpleName());
        }
    }

    public void addListener(AiAgentWebSocketListener listener) {
        registerListener(listener);
    }

    public void removeListener(AiAgentWebSocketListener listener) {
        unregisterListener(listener);
    }

    @PostConstruct
    public void init() {
        log.info("Initializing AI Agent WebSocket Client for URL: {}", serverUrl);
        connectClient();
    }

    @PreDestroy
    public void shutdown() {
        disconnectClient();
    }

    public synchronized void connectClient() {
        if (innerClient != null && !innerClient.isClosed()) {
            return;
        }
        try {
            URI uri = new URI(serverUrl);
            innerClient = new InnerWebSocketClient(uri);
            innerClient.connect();
        } catch (Exception e) {
            log.warn("Failed to initiate connection to AI Agent at {}: {}", serverUrl, e.getMessage());
        }
    }

    public synchronized void disconnectClient() {
        if (innerClient != null) {
            try {
                innerClient.close();
            } catch (Exception ignored) {
            }
            innerClient = null;
        }
        isConnected = false;
    }

    @Scheduled(fixedDelay = 5000)
    public void autoReconnectLoop() {
        if (!isConnected) {
            if (innerClient == null || innerClient.isClosed()) {
                log.debug("Auto-reconnecting AI Agent WebSocket client at {}", serverUrl);
                connectClient();
            }
        }
    }

    public boolean sendPayload(Object payload) {
        if (!isConnected || innerClient == null || !innerClient.isOpen()) {
            log.warn("Cannot send payload to AI Agent: Client is not connected");
            return false;
        }
        try {
            String jsonStr = (payload instanceof String) ? (String) payload : MAPPER.writeValueAsString(payload);
            innerClient.send(jsonStr);
            log.debug("Sent payload to AI Agent: {}", jsonStr);
            return true;
        } catch (Exception e) {
            log.error("Error sending payload to AI Agent: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected && innerClient != null && innerClient.isOpen();
    }

    public String getServerUrl() {
        return serverUrl;
    }

    private class InnerWebSocketClient extends WebSocketClient {

        public InnerWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            log.info("Successfully connected to AI Agent WebSocket Server at {}", serverUrl);
            isConnected = true;
            for (AiAgentWebSocketListener listener : listeners) {
                try {
                    listener.onConnected(serverUrl);
                } catch (Exception e) {
                    log.error("Error in onConnected callback for {}: {}", listener.getClass().getName(), e.getMessage());
                }
            }
        }

        @Override
        public void onMessage(String message) {
            log.trace("Received message from AI Agent: {}", message);
            try {
                JsonNode root = MAPPER.readTree(message);
                String eventType = root.path("event").asText(null);
                if (eventType == null || eventType.isBlank()) {
                    eventType = root.path("event-type").asText(null);
                }
                if (eventType == null || eventType.isBlank()) {
                    eventType = root.path("action").asText("UNKNOWN");
                }

                for (AiAgentWebSocketListener listener : listeners) {
                    try {
                        listener.onMessageReceived(eventType, root, message);
                    } catch (Exception e) {
                        log.error("Error in onMessageReceived callback for {}: {}", listener.getClass().getName(), e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse message from AI Agent: {}", e.getMessage());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.warn("AI Agent WebSocket connection closed at {}: code={}, reason={}, remote={}", serverUrl, code, reason, remote);
            isConnected = false;
            for (AiAgentWebSocketListener listener : listeners) {
                try {
                    listener.onDisconnected(serverUrl, reason);
                } catch (Exception e) {
                    log.error("Error in onDisconnected callback for {}: {}", listener.getClass().getName(), e.getMessage());
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            log.warn("AI Agent WebSocket client error at {}: {}", serverUrl, ex == null ? "unknown" : ex.getMessage());
        }
    }
}
