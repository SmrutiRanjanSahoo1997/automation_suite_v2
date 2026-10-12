package com.automation.mssql.client;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MssqlAgentClient extends WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(MssqlAgentClient.class);

    private final String host;
    private final MssqlAgentClientManager manager;

    public MssqlAgentClient(URI serverUri, String host, MssqlAgentClientManager manager) {
        super(serverUri);
        this.host = host;
        this.manager = manager;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("Connected to MSSQL Agent on host {}", host);
        manager.onAgentConnected(host);
    }

    @Override
    public void onMessage(String message) {
        log.trace("Received message from MSSQL Agent {}: {}", host, message);
        manager.onAgentMessage(host, message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("MSSQL Agent connection closed on host {}: code={}, reason={}", host, code, reason);
        manager.onAgentDisconnected(host);
    }

    @Override
    public void onError(Exception ex) {
        log.warn("MSSQL Agent connection error on host {}: {}", host, ex != null ? ex.getMessage() : "unknown");
    }

    public static URI buildMssqlAgentUri(String host, int port) {
        try {
            return new URI("ws://" + host + ":" + port + "/ws/mssql");
        } catch (Exception e) {
            throw new RuntimeException("Invalid MSSQL Agent URI for host: " + host, e);
        }
    }
}
