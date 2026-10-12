package com.automation.service;

import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface implemented by feature orchestrators to listen to all incoming UI WebSocket events.
 * No pre-filtering is performed by the Global UI WebSocket feature;
 * each listener inspects eventType and handles its own events independently.
 */
public interface GlobalUiWebSocketListener {

    void onUiMessage(WebSocketSession session, String eventType, JsonNode payload, String rawMessage);

    default void onSessionConnected(WebSocketSession session) {}

    default void onSessionDisconnected(WebSocketSession session, String reason) {}
}
