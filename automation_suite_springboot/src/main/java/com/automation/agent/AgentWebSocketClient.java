package com.automation.agent;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentWebSocketClient extends WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(AgentWebSocketClient.class);

    private final String host;
    private final AgentClientManager manager;

    public AgentWebSocketClient(URI serverUri, String host, AgentClientManager manager) {
        super(serverUri);
        this.host = host;
        this.manager = manager;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("Successfully connected to Agent on host {}", host);
        manager.onAgentConnected(host);
    }

    @Override
    public void onMessage(String message) {
        log.trace("Received raw message from Agent on host {}: {}", host, message);
        manager.onAgentMessage(host, message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("Agent WebSocket connection closed for host {}: code={}, reason={}, remote={}", host, code, reason, remote);
        manager.onAgentDisconnected(host);
    }

    @Override
    public void onError(Exception ex) {
        log.warn("Error in Agent WebSocket connection for host {}: {}", host, ex == null ? "unknown" : ex.getMessage());
    }

    public static URI buildAgentUri(String host, int port, String secretKey) {
        try {
            String encodedKey = java.net.URLEncoder.encode(secretKey, StandardCharsets.UTF_8);
            return new URI("ws://" + host + ":" + port + "/ws/agent?x-agent-key=" + encodedKey);
        } catch (Exception e) {
            throw new RuntimeException("Invalid URI for host: " + host, e);
        }
    }
}
