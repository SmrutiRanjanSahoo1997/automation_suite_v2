package com.automation.mssql.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.automation.config.ServerInventory;
import com.automation.config.ServerInventory.ServerConfig;
import com.automation.mssql.service.MssqlOrchestratorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class MssqlAgentClientManager {

    private static final Logger log = LoggerFactory.getLogger(MssqlAgentClientManager.class);

    @Value("${mssql.agent.port:9012}")
    private int mssqlAgentPort;

    private final MssqlOrchestratorService orchestratorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, MssqlAgentClient> clients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> connectedMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> queueCountMap = new ConcurrentHashMap<>();

    public MssqlAgentClientManager(@Lazy MssqlOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostConstruct
    public void init() {
        for (ServerConfig cfg : ServerInventory.SERVERS) {
            connectedMap.put(cfg.getHost(), false);
            queueCountMap.put(cfg.getHost(), 0);
        }
        connectAll();
    }

    @PreDestroy
    public void shutdown() {
        for (MssqlAgentClient client : clients.values()) {
            try {
                client.close();
            } catch (Exception ignored) {}
        }
    }

    public synchronized void connectAll() {
        for (ServerConfig cfg : ServerInventory.SERVERS) {
            connectHost(cfg.getHost());
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void autoReconnectLoop() {
        for (ServerConfig cfg : ServerInventory.SERVERS) {
            String host = cfg.getHost();
            Boolean isConnected = connectedMap.getOrDefault(host, false);
            if (!isConnected) {
                MssqlAgentClient existing = clients.get(host);
                if (existing == null || existing.isClosed()) {
                    connectHost(host);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void pollStatusLoop() {
        String pingMsg = "{\"event\":\"GET_STATUS\"}";
        for (Map.Entry<String, MssqlAgentClient> entry : clients.entrySet()) {
            String host = entry.getKey();
            MssqlAgentClient client = entry.getValue();
            if (client != null && client.isOpen()) {
                try {
                    client.send(pingMsg);
                } catch (Exception e) {
                    log.debug("Failed to send GET_STATUS to host {}: {}", host, e.getMessage());
                }
            }
        }
    }

    private void connectHost(String host) {
        try {
            MssqlAgentClient old = clients.get(host);
            if (old != null && !old.isClosed()) {
                return;
            }
            URI uri = MssqlAgentClient.buildMssqlAgentUri(host, mssqlAgentPort);
            MssqlAgentClient client = new MssqlAgentClient(uri, host, this);
            clients.put(host, client);
            client.connect();
        } catch (Exception e) {
            log.debug("Could not initiate MSSQL agent connection to host {}: {}", host, e.getMessage());
        }
    }

    public void onAgentConnected(String host) {
        connectedMap.put(host, true);
        log.info("MSSQL Agent on host {} is now CONNECTED", host);
    }

    public void onAgentDisconnected(String host) {
        connectedMap.put(host, false);
        clients.remove(host);
        queueCountMap.put(host, 0);
        log.debug("MSSQL Agent on host {} is now DISCONNECTED", host);
    }

    public void onAgentMessage(String host, String rawMessage) {
        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            if (root.has("type") && "status".equalsIgnoreCase(root.get("type").asText())) {
                if (root.has("queueCount")) {
                    int count = root.get("queueCount").asInt(0);
                    queueCountMap.put(host, count);
                }
                return;
            }
        } catch (Exception ignored) {}

        orchestratorService.handleAgentMessage(host, rawMessage);
    }

    public boolean sendToHost(String host, String jsonMessage) {
        MssqlAgentClient client = clients.get(host);
        if (client != null && client.isOpen()) {
            client.send(jsonMessage);
            return true;
        }
        log.warn("Cannot send MSSQL message to host {}: client is not connected", host);
        return false;
    }

    public List<String> getConnectedHosts() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : connectedMap.entrySet()) {
            if (entry.getValue()) {
                list.add(entry.getKey());
            }
        }
        return Collections.unmodifiableList(list);
    }

    public int getHostQueueCount(String host) {
        return queueCountMap.getOrDefault(host, 0);
    }

    public Map<String, Integer> getQueueCountMap() {
        return Collections.unmodifiableMap(queueCountMap);
    }
}
