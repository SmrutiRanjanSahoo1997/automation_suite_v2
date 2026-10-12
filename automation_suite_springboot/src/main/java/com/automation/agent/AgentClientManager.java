package com.automation.agent;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.automation.config.AppProperties;
import com.automation.config.ServerInventory;
import com.automation.config.ServerInventory.ServerConfig;
import com.automation.service.TerminalOrchestratorService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class AgentClientManager {

    private static final Logger log = LoggerFactory.getLogger(AgentClientManager.class);

    private final AppProperties properties;
    private final TerminalOrchestratorService orchestratorService;

    private final ConcurrentHashMap<String, AgentWebSocketClient> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AgentConnectionStatus> statusMap = new ConcurrentHashMap<>();

    public AgentClientManager(AppProperties properties, @Lazy TerminalOrchestratorService orchestratorService) {
        this.properties = properties;
        this.orchestratorService = orchestratorService;
    }

    @PostConstruct
    public void init() {
        for (ServerConfig config : ServerInventory.SERVERS) {
            statusMap.put(config.getHost(), new AgentConnectionStatus(config.getHost(), false, null, 0));
        }
        connectAll();
    }

    @PreDestroy
    public void shutdown() {
        for (Map.Entry<String, AgentWebSocketClient> entry : clients.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void connectAll() {
        for (ServerConfig config : ServerInventory.SERVERS) {
            connectHost(config);
        }
    }

    @Scheduled(fixedDelayString = "${agent.reconnect-interval-ms:5000}")
    public void autoReconnectLoop() {
        for (ServerConfig config : ServerInventory.SERVERS) {
            AgentConnectionStatus status = statusMap.get(config.getHost());
            if (status != null && !status.isConnected()) {
                AgentWebSocketClient existing = clients.get(config.getHost());
                if (existing == null || existing.isClosed()) {
                    log.debug("Auto-reconnecting agent for host {}", config.getHost());
                    status.setReconnectAttempts(status.getReconnectAttempts() + 1);
                    connectHost(config);
                }
            }
        }
    }

    private void connectHost(ServerConfig config) {
        try {
            AgentWebSocketClient old = clients.get(config.getHost());
            if (old != null && !old.isClosed()) {
                return;
            }
            URI uri = AgentWebSocketClient.buildAgentUri(config.getHost(), config.getPort(), properties.getSecretKey());
            AgentWebSocketClient client = new AgentWebSocketClient(uri, config.getHost(), this);
            clients.put(config.getHost(), client);
            client.connect();
        } catch (Exception e) {
            log.warn("Failed to initiate connection to agent at {}: {}", config.getHost(), e.getMessage());
        }
    }

    public void registerClient(String host, AgentWebSocketClient client) {
        clients.put(host, client);
        onAgentConnected(host);
    }

    public void onAgentConnected(String host) {
        AgentConnectionStatus status = statusMap.computeIfAbsent(host, h -> new AgentConnectionStatus(h, false, null, 0));
        status.setConnected(true);
        status.setLastConnectedAt(System.currentTimeMillis());
        status.setReconnectAttempts(0);
        log.info("Agent host {} is now CONNECTED", host);
    }

    public void onAgentDisconnected(String host) {
        AgentConnectionStatus status = statusMap.get(host);
        boolean wasConnected = status != null && status.isConnected();
        if (status != null) {
            status.setConnected(false);
        }
        clients.remove(host);
        if (wasConnected) {
            log.info("Agent host {} is now DISCONNECTED", host);
        } else {
            log.debug("Agent host {} connection attempt failed", host);
        }
    }

    public void onAgentMessage(String host, String messageJson) {
        orchestratorService.handleAgentMessage(host, messageJson);
    }

    public boolean sendToAgent(String host, String jsonMessage) {
        AgentWebSocketClient client = clients.get(host);
        if (client != null && client.isOpen()) {
            client.send(jsonMessage);
            return true;
        }
        log.warn("Cannot send message to host {}: agent WebSocket client is not connected", host);
        return false;
    }

    public Map<String, AgentConnectionStatus> getStatusMap() {
        return statusMap;
    }
}
