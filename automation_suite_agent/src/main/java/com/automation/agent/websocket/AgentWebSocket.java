package com.automation.agent.websocket;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.protocol.TerminalProtocol.TerminalEventType;
import com.automation.agent.terminal.TerminalManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Embedded WebSocket SERVER that listens for incoming client connections (Spring Boot,
 * test consoles, CLI clients) on port 9010.
 * Authenticates incoming handshakes via x-agent-key (header or query parameter),
 * routes inbound protocol events to {@link TerminalManager}, and broadcasts/sends
 * terminal events back to connected clients.
 */
public final class AgentWebSocket extends WebSocketServer implements Consumer<Object> {

    private static final Logger log = LoggerFactory.getLogger(AgentWebSocket.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String agentKey;
    private final TerminalManager manager;
    private final Set<WebSocket> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public AgentWebSocket(int port, String agentKey, TerminalManager manager) {
        super(new InetSocketAddress(port));
        this.agentKey = agentKey;
        this.manager = manager;
        setReuseAddr(true);
    }

    public AgentWebSocket(InetSocketAddress address, String agentKey, TerminalManager manager) {
        super(address);
        this.agentKey = agentKey;
        this.manager = manager;
        setReuseAddr(true);
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(
            WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);

        String key = request.getFieldValue("x-agent-key");
        if (key == null || key.isBlank()) {
            String descriptor = request.getResourceDescriptor();
            if (descriptor != null && descriptor.contains("x-agent-key=")) {
                int idx = descriptor.indexOf("x-agent-key=");
                key = descriptor.substring(idx + "x-agent-key=".length());
                int amp = key.indexOf('&');
                if (amp >= 0) {
                    key = key.substring(0, amp);
                }
                try {
                    key = java.net.URLDecoder.decode(key, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                }
            }
        }
        if (key == null || key.isBlank()) {
            key = request.getFieldValue("Sec-WebSocket-Protocol");
        }

        if (agentKey != null && !agentKey.equals(key)) {
            log.warn("Rejecting WebSocket connection from {}: bad agent key (provided='{}', expected='{}')",
                    conn.getRemoteSocketAddress(), key, agentKey);
            throw new InvalidDataException(4401, "bad agent key");
        }
        return builder;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log.info("Client connected to agent WebSocket: {}", conn.getRemoteSocketAddress());
        clients.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Client disconnected: {} (code={}, reason={})", conn.getRemoteSocketAddress(), code, reason);
        clients.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            handleInbound(conn, message);
        } catch (Exception e) {
            log.warn("Error handling client message from {}: {}", conn.getRemoteSocketAddress(), e.toString());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.warn("WebSocket server error: {}", ex == null ? "unknown" : ex.toString());
    }

    @Override
    public void onStart() {
        log.info("Automation Suite Agent WebSocket server listening on port {}", getPort());
    }

    /** Graceful shutdown: stop server and close all client sockets. */
    public void shutdown() {
        try {
            stop(1000);
        } catch (Exception ignored) {
        }
    }

    /** Event sink for TerminalManager. Broadcasts terminal events to all connected authenticated clients. */
    @Override
    public synchronized void accept(Object eventPojo) {
        if (clients.isEmpty()) {
            log.debug("No connected clients; dropping outbound terminal event: {}", eventPojo);
            return;
        }
        try {
            String json = (eventPojo instanceof String s) ? s : MAPPER.writeValueAsString(eventPojo);
            for (WebSocket c : clients) {
                if (c.isOpen()) {
                    c.send(json);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send terminal event: {}", e.toString());
        }
    }

    private void handleInbound(WebSocket conn, String rawJson) {
        JsonNode root;
        try {
            root = MAPPER.readTree(rawJson);
        } catch (Exception e) {
            log.warn("Ignoring non-JSON frame: {}", e.toString());
            return;
        }
        String wire = root.path("event-type").asText(null);
        TerminalEventType type = TerminalEventType.fromWire(wire);
        if (type == null) {
            log.warn("Ignoring unknown event-type: {}", wire);
            return;
        }
        String terminalId = root.path("terminalid").asText(null);
        switch (type) {
            case TERMINAL_START -> {
                String initPath = root.has("path") ? root.path("path").asText(null) : root.path("currentPath").asText(null);
                manager.start(terminalId, root.path("platform").asText(null), initPath);
            }
            case TERMINAL_EXECUTE ->
                manager.execute(terminalId, root.path("command").asText(""));
            case TERMINAL_STOP ->
                manager.stop(terminalId);
            case TERMINAL_CLOSE ->
                manager.close(terminalId);
            case TERMINAL_REMOVE ->
                manager.remove(terminalId);
            case METADATA_LAST_RESULT ->
                manager.metadataLastResult(terminalId);
            case METADATA_LAST_ERROR ->
                manager.metadataLastError(terminalId);
            case METADATA_LIST ->
                manager.metadataList();
            default ->
                log.warn("Ignoring outbound-only event from client: {}", wire);
        }
    }
}