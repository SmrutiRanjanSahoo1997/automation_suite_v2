package com.automation.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Concrete implementation of GlobalUiWebSocketChannel.
 * Completely independent module managing UI WebSocket sessions (/ws/app).
 * Broadcasts all incoming UI frames to registered listeners without pre-filtering.
 */
@Service
public class GlobalUiWebSocketService implements GlobalUiWebSocketChannel {

    private static final Logger log = LoggerFactory.getLogger(GlobalUiWebSocketService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, WebSocketSession> uiSessions = new ConcurrentHashMap<>();
    private final List<GlobalUiWebSocketListener> listeners = new CopyOnWriteArrayList<>();

    public GlobalUiWebSocketService() {
        log.info("Initialized independent GlobalUiWebSocketService (0 constructor dependencies)");
    }

    @Override
    public void registerListener(GlobalUiWebSocketListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            log.info("Registered UI WebSocket listener: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void unregisterListener(GlobalUiWebSocketListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            log.info("Unregistered UI WebSocket listener: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void registerUiSession(WebSocketSession session) {
        uiSessions.put(session.getId(), session);
        log.info("Registered UI WebSocket session: {}", session.getId());
        for (GlobalUiWebSocketListener listener : listeners) {
            try {
                listener.onSessionConnected(session);
            } catch (Exception e) {
                log.error("Error in onSessionConnected for listener {}: {}", listener.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void unregisterUiSession(WebSocketSession session) {
        uiSessions.remove(session.getId());
        log.info("Unregistered UI WebSocket session: {}", session.getId());
        for (GlobalUiWebSocketListener listener : listeners) {
            try {
                listener.onSessionDisconnected(session, "Closed");
            } catch (Exception e) {
                log.error("Error in onSessionDisconnected for listener {}: {}", listener.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void handleUiMessage(WebSocketSession session, String payload) {
        try {
            JsonNode root = MAPPER.readTree(payload);
            String wire = root.path("event-type").asText(null);
            if (wire == null || wire.isBlank()) {
                wire = root.path("event").asText(null);
            }
            if (wire == null || wire.isBlank()) {
                wire = root.path("eventType").asText(null);
            }
            if (wire == null || wire.isBlank()) {
                wire = "UNKNOWN";
            }

            // Distribute all incoming frames to every registered listener without pre-filtering
            for (GlobalUiWebSocketListener listener : listeners) {
                try {
                    listener.onUiMessage(session, wire, root, payload);
                } catch (Exception e) {
                    log.error("Error handling UI event {} in listener {}: {}", wire, listener.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse UI message JSON from session {}: {}", session.getId(), e.getMessage());
        }
    }

    @Override
    public void broadcastToUi(String jsonMessage) {
        if (jsonMessage == null || jsonMessage.isBlank()) return;
        TextMessage textMessage = new TextMessage(jsonMessage);
        uiSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    log.warn("Failed to broadcast message to session {}: {}", session.getId(), e.getMessage());
                    try {
                        session.close();
                    } catch (IOException ignored) {
                    }
                    uiSessions.remove(session.getId());
                }
            }
        });
    }

    @Override
    public void sendToSession(WebSocketSession session, String jsonMessage) {
        if (session == null || !session.isOpen() || jsonMessage == null) return;
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (IOException e) {
            log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
            try {
                session.close();
            } catch (IOException ignored) {
            }
            uiSessions.remove(session.getId());
        }
    }

    @Override
    public Map<String, WebSocketSession> getUiSessions() {
        return uiSessions;
    }
}
