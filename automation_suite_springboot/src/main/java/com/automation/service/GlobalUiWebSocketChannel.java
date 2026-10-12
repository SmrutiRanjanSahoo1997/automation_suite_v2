package com.automation.service;

import java.util.Map;
import org.springframework.web.socket.WebSocketSession;

/**
 * Interface defining the Global UI WebSocket Channel capabilities for session management,
 * listener registration, and frame transmission.
 */
public interface GlobalUiWebSocketChannel {

    void registerListener(GlobalUiWebSocketListener listener);

    void unregisterListener(GlobalUiWebSocketListener listener);

    void registerUiSession(WebSocketSession session);

    void unregisterUiSession(WebSocketSession session);

    void handleUiMessage(WebSocketSession session, String payload);

    void broadcastToUi(String jsonMessage);

    void sendToSession(WebSocketSession session, String jsonMessage);

    Map<String, WebSocketSession> getUiSessions();
}
