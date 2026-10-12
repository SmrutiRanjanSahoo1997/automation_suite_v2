package com.automation.aiagent.client;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for listening to AI Agent (Port 9020) WebSocket lifecycle and message events.
 */
public interface AiAgentWebSocketListener {

    void onConnected(String serverUrl);

    void onDisconnected(String serverUrl, String reason);

    void onMessageReceived(String eventType, JsonNode payload, String rawMessage);

    default boolean onSend(String eventType, Object payload) {
        return true;
    }
}
