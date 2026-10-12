package com.automation.aiagent.client;

/**
 * Interface defining the AI Agent WebSocket Channel capabilities for listener registration
 * and task transmission to the AI Agent Server (Port 9020).
 */
public interface AiAgentWebSocketChannel {

    void registerListener(AiAgentWebSocketListener listener);

    void unregisterListener(AiAgentWebSocketListener listener);

    boolean sendPayload(Object payload);

    boolean isConnected();

    String getServerUrl();
}
