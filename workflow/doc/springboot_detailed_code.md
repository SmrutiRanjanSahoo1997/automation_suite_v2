# 🚀 Automation Suite SpringBoot Orchestrator - Detailed Source Code

> 📅 **Generated On:** `2026-09-28 22:17:35`  
> 📂 **Source Root:** `C:/Ranjan/AutomationSuite_v2/automation_suite_springboot`  
> 📊 **Total Source Files:** `28`  
> 📝 **Total Lines of Code:** `2,929`  
> 💾 **Total Size:** `124.22 KB`  

---

## 📌 Executive Summary

Spring Boot 3.2 Orchestrator service. Connects outbound WebSocket clients to 34+ target agents on port 9010 with auto-reconnect loops, exposes global WebSocket hub at /ws/app for browser clients, persists session logs and command audit snapshots in SQLite (C:/Ranjan/Automation Suite/Database/automation.db), and enforces 7-day session log and 25-day command log retention schedules.

---

## 📂 Project Directory Structure

```text
automation_suite_springboot/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── automation/
    │   │           ├── AutomationSpringBootApplication.java
    │   │           ├── agent/
    │   │           │   ├── AgentClientManager.java
    │   │           │   ├── AgentConnectionStatus.java
    │   │           │   └── AgentWebSocketClient.java
    │   │           ├── ai/
    │   │           │   ├── client/
    │   │           │   │   └── AiAgentWebSocketClient.java
    │   │           │   ├── protocol/
    │   │           │   │   ├── AiEventConstants.java
    │   │           │   │   └── AiProtocol.java
    │   │           │   └── service/
    │   │           │       ├── AiOrchestratorService.java
    │   │           │       └── PromptTemplateService.java
    │   │           ├── config/
    │   │           │   ├── AppProperties.java
    │   │           │   ├── GlobalWebSocketConfig.java
    │   │           │   └── ServerInventory.java
    │   │           ├── controller/
    │   │           │   ├── GlobalWebSocketHandler.java
    │   │           │   └── ServerInventoryController.java
    │   │           ├── domain/
    │   │           │   ├── TerminalCommandLog.java
    │   │           │   └── TerminalSessionLog.java
    │   │           ├── protocol/
    │   │           │   └── TerminalProtocol.java
    │   │           ├── repository/
    │   │           │   ├── TerminalCommandLogRepository.java
    │   │           │   └── TerminalSessionRepository.java
    │   │           └── service/
    │   │               ├── SessionLogCleanupScheduler.java
    │   │               └── TerminalOrchestratorService.java
    │   └── resources/
    │       ├── application.properties
    │       └── db/
    │           └── migration/
    │               ├── V1__init_terminal_schema.sql
    │               └── V2__add_result_column_to_command_log.sql
    └── test/
        └── java/
            └── com/
                └── automation/
                    └── simulator/
                        ├── AiExecuteTaskFlowTest.java
                        ├── OrchestratorSimulatorTest.java
                        └── RealtimeFlowTest.java
```

---

## 📑 File Index

1. [`pom.xml`](#file-1)
2. [`src/main/java/com/automation/AutomationSpringBootApplication.java`](#file-2)
3. [`src/main/java/com/automation/agent/AgentClientManager.java`](#file-3)
4. [`src/main/java/com/automation/agent/AgentConnectionStatus.java`](#file-4)
5. [`src/main/java/com/automation/agent/AgentWebSocketClient.java`](#file-5)
6. [`src/main/java/com/automation/ai/client/AiAgentWebSocketClient.java`](#file-6)
7. [`src/main/java/com/automation/ai/protocol/AiEventConstants.java`](#file-7)
8. [`src/main/java/com/automation/ai/protocol/AiProtocol.java`](#file-8)
9. [`src/main/java/com/automation/ai/service/AiOrchestratorService.java`](#file-9)
10. [`src/main/java/com/automation/ai/service/PromptTemplateService.java`](#file-10)
11. [`src/main/java/com/automation/config/AppProperties.java`](#file-11)
12. [`src/main/java/com/automation/config/GlobalWebSocketConfig.java`](#file-12)
13. [`src/main/java/com/automation/config/ServerInventory.java`](#file-13)
14. [`src/main/java/com/automation/controller/GlobalWebSocketHandler.java`](#file-14)
15. [`src/main/java/com/automation/controller/ServerInventoryController.java`](#file-15)
16. [`src/main/java/com/automation/domain/TerminalCommandLog.java`](#file-16)
17. [`src/main/java/com/automation/domain/TerminalSessionLog.java`](#file-17)
18. [`src/main/java/com/automation/protocol/TerminalProtocol.java`](#file-18)
19. [`src/main/java/com/automation/repository/TerminalCommandLogRepository.java`](#file-19)
20. [`src/main/java/com/automation/repository/TerminalSessionRepository.java`](#file-20)
21. [`src/main/java/com/automation/service/SessionLogCleanupScheduler.java`](#file-21)
22. [`src/main/java/com/automation/service/TerminalOrchestratorService.java`](#file-22)
23. [`src/main/resources/application.properties`](#file-23)
24. [`src/main/resources/db/migration/V1__init_terminal_schema.sql`](#file-24)
25. [`src/main/resources/db/migration/V2__add_result_column_to_command_log.sql`](#file-25)
26. [`src/test/java/com/automation/simulator/AiExecuteTaskFlowTest.java`](#file-26)
27. [`src/test/java/com/automation/simulator/OrchestratorSimulatorTest.java`](#file-27)
28. [`src/test/java/com/automation/simulator/RealtimeFlowTest.java`](#file-28)

---

## 💻 Comprehensive Source Code

<a id="file-1"></a>
### 1. `pom.xml`

**Path:** `pom.xml` | **Size:** `3.52 KB` | **Lines:** `91` | **Language:** `xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>
    <groupId>com.automation</groupId>
    <artifactId>automation_suite_springboot</artifactId>
    <version>2.0.0</version>
    <name>automation_suite_springboot</name>
    <description>Spring Boot Orchestrator for Automation Suite V2</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.45.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.java-websocket</groupId>
            <artifactId>Java-WebSocket</artifactId>
            <version>1.5.6</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <version>3.3.1</version>
                <executions>
                    <execution>
                        <id>copy-frontend-dist</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${project.build.outputDirectory}/static</outputDirectory>
                            <resources>
                                <resource>
                                    <directory>../automation_suite_frontend/dist</directory>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

```

---

<a id="file-2"></a>
### 2. `src/main/java/com/automation/AutomationSpringBootApplication.java`

**Path:** `src/main/java/com/automation/AutomationSpringBootApplication.java` | **Size:** `0.42 KB` | **Lines:** `14` | **Language:** `java`

```java
package com.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutomationSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomationSpringBootApplication.class, args);
    }
}

```

---

<a id="file-3"></a>
### 3. `src/main/java/com/automation/agent/AgentClientManager.java`

**Path:** `src/main/java/com/automation/agent/AgentClientManager.java` | **Size:** `4.87 KB` | **Lines:** `135` | **Language:** `java`

```java
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

```

---

<a id="file-4"></a>
### 4. `src/main/java/com/automation/agent/AgentConnectionStatus.java`

**Path:** `src/main/java/com/automation/agent/AgentConnectionStatus.java` | **Size:** `1.08 KB` | **Lines:** `30` | **Language:** `java`

```java
package com.automation.agent;

public class AgentConnectionStatus {
    private String host;
    private boolean connected;
    private Long lastConnectedAt;
    private int reconnectAttempts;

    public AgentConnectionStatus() {
    }

    public AgentConnectionStatus(String host, boolean connected, Long lastConnectedAt, int reconnectAttempts) {
        this.host = host;
        this.connected = connected;
        this.lastConnectedAt = lastConnectedAt;
        this.reconnectAttempts = reconnectAttempts;
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public Long getLastConnectedAt() { return lastConnectedAt; }
    public void setLastConnectedAt(Long lastConnectedAt) { this.lastConnectedAt = lastConnectedAt; }

    public int getReconnectAttempts() { return reconnectAttempts; }
    public void setReconnectAttempts(int reconnectAttempts) { this.reconnectAttempts = reconnectAttempts; }
}

```

---

<a id="file-5"></a>
### 5. `src/main/java/com/automation/agent/AgentWebSocketClient.java`

**Path:** `src/main/java/com/automation/agent/AgentWebSocketClient.java` | **Size:** `1.86 KB` | **Lines:** `55` | **Language:** `java`

```java
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

```

---

<a id="file-6"></a>
### 6. `src/main/java/com/automation/ai/client/AiAgentWebSocketClient.java`

**Path:** `src/main/java/com/automation/ai/client/AiAgentWebSocketClient.java` | **Size:** `10.75 KB` | **Lines:** `254` | **Language:** `java`

```java
package com.automation.ai.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.automation.ai.service.AiOrchestratorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class AiAgentWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(AiAgentWebSocketClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${ai.agent.ws-url:ws://127.0.0.1:9020/ws/ai_agent}")
    private String aiAgentWsUrl;

    private final AiOrchestratorService orchestratorService;
    private final ConcurrentHashMap<String, CompletableFuture<String>> pendingTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();

    private WebSocket webSocket;
    private volatile boolean connected = false;

    public AiAgentWebSocketClient(@Lazy AiOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostConstruct
    public void init() {
        connect();
        reconnectScheduler.scheduleAtFixedRate(this::checkConnection, 5, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        reconnectScheduler.shutdownNow();
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown").join();
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void connect() {
        if (connected && webSocket != null) {
            return;
        }
        try {
            log.info("Connecting to AI Agent Server at {}", aiAgentWsUrl);
            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                    .buildAsync(URI.create(aiAgentWsUrl), new AiWebSocketListener())
                    .whenComplete((ws, err) -> {
                        if (err != null) {
                            log.warn("Failed to connect to AI Agent Server: {}", err.getMessage());
                            connected = false;
                            webSocket = null;
                        } else {
                            log.info("Connected to AI Agent Server at {}", aiAgentWsUrl);
                            webSocket = ws;
                            connected = true;
                        }
                    });
        } catch (Exception e) {
            log.warn("Error initiating AI Agent WebSocket connection: {}", e.getMessage());
            connected = false;
        }
    }

    private void checkConnection() {
        if (!connected || webSocket == null) {
            connect();
        }
    }

    public CompletableFuture<String> executeTask(String taskId, String conversationId, String prompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (!connected || webSocket == null) {
            future.completeExceptionally(new IllegalStateException("AI Agent Server is disconnected"));
            return future;
        }

        pendingTasks.put(taskId, future);
        try {
            Map<String, Object> payloadMap = new java.util.HashMap<>();
            payloadMap.put("event", "EXECUTE_TASK");
            payloadMap.put("taskId", taskId);
            payloadMap.put("conversationId", conversationId);
            payloadMap.put("prompt", prompt != null ? prompt : "");
            payloadMap.put("files_path", java.util.Collections.emptyList());

            String jsonPayload = MAPPER.writeValueAsString(payloadMap);
            log.info("Sending EXECUTE_TASK to AI Agent (taskId: {}, convId: {})", taskId, conversationId);
            webSocket.sendText(jsonPayload, true);

            // Timeout safety after 120 seconds
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                CompletableFuture<String> f = pendingTasks.remove(taskId);
                if (f != null && !f.isDone()) {
                    f.completeExceptionally(new java.util.concurrent.TimeoutException("AI task timed out after 120s"));
                }
            }, 120, TimeUnit.SECONDS);

        } catch (Exception e) {
            pendingTasks.remove(taskId);
            future.completeExceptionally(e);
        }
        return future;
    }

    public CompletableFuture<String> executeDelete(String conversationId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (!connected || webSocket == null) {
            future.completeExceptionally(new IllegalStateException("AI Agent Server is disconnected"));
            return future;
        }

        String taskId = "del_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 6);
        pendingTasks.put(taskId, future);
        try {
            Map<String, Object> payloadMap = new java.util.HashMap<>();
            payloadMap.put("event", "EXECUTE_DELETE");
            payloadMap.put("conversationId", conversationId);

            String jsonPayload = MAPPER.writeValueAsString(payloadMap);
            log.info("Sending EXECUTE_DELETE to AI Agent (convId: {})", conversationId);
            webSocket.sendText(jsonPayload, true);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                CompletableFuture<String> f = pendingTasks.remove(taskId);
                if (f != null && !f.isDone()) {
                    f.completeExceptionally(new java.util.concurrent.TimeoutException("AI delete task timed out after 60s"));
                }
            }, 60, TimeUnit.SECONDS);

        } catch (Exception e) {
            pendingTasks.remove(taskId);
            future.completeExceptionally(e);
        }
        return future;
    }

    public boolean isConnected() {
        return connected;
    }

    private class AiWebSocketListener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String fullMessage = buffer.toString();
                buffer.setLength(0);
                handleIncomingMessage(fullMessage);
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.warn("AI Agent WebSocket error: {}", error.getMessage());
            connected = false;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("AI Agent WebSocket connection closed (status={}, reason={})", statusCode, reason);
            connected = false;
            AiAgentWebSocketClient.this.webSocket = null;
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }

    private void handleIncomingMessage(String messageJson) {
        try {
            JsonNode root = MAPPER.readTree(messageJson);
            String event = root.path("event").asText(null);

            if ("AI_HEALTH".equals(event)) {
                boolean extsnConnected = root.path("extsnConnected").asBoolean(false);
                String status = root.path("status").asText("NOT_OK");
                int active = root.path("activeTasks").asInt(0);
                int queued = root.path("queuedTasks").asInt(0);

                orchestratorService.handleHealthUpdate(status, extsnConnected, active, queued);

            } else if ("DELETE_RESPONSE".equals(event)) {
                String convId = root.path("conversationId").asText(null);
                log.info("Received DELETE_RESPONSE from AI Agent for convId: {}", convId);
                try {
                    com.fasterxml.jackson.databind.node.ObjectNode resp = MAPPER.createObjectNode();
                    resp.put("event-type", "AI_DELETE_RESP");
                    resp.put("conversationId", convId);
                    resp.put("status", root.path("status").asText("RESPONSE"));
                    resp.put("message", root.path("message").asText("The conversation deleted"));
                    orchestratorService.broadcastToUi(MAPPER.writeValueAsString(resp));
                } catch (Exception ignored) {
                }

            } else if ("TASK_RESULT".equals(event)) {
                String taskId = root.path("taskId").asText(null);
                String convId = root.path("conversationId").asText(null);
                String responseStr = root.path("response").asText(null);
                String errorStr = root.path("error").asText(null);

                if (taskId != null) {
                    CompletableFuture<String> future = pendingTasks.remove(taskId);
                    if (future != null) {
                        if (errorStr != null && !errorStr.isBlank() && (responseStr == null || responseStr.isBlank())) {
                            future.completeExceptionally(new RuntimeException(errorStr));
                        } else {
                            String payloadToReturn = responseStr != null ? responseStr : messageJson;
                            if (convId != null && !convId.isBlank() && !"null".equalsIgnoreCase(convId)) {
                                try {
                                    JsonNode respNode = MAPPER.readTree(payloadToReturn);
                                    if (respNode.isObject() && (!respNode.has("conversationId") || respNode.path("conversationId").isNull())) {
                                        ((com.fasterxml.jackson.databind.node.ObjectNode) respNode).put("conversationId", convId);
                                        payloadToReturn = MAPPER.writeValueAsString(respNode);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            future.complete(payloadToReturn);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing incoming message from AI Agent Server: {}", e.getMessage());
        }
    }
}

```

---

<a id="file-7"></a>
### 7. `src/main/java/com/automation/ai/protocol/AiEventConstants.java`

**Path:** `src/main/java/com/automation/ai/protocol/AiEventConstants.java` | **Size:** `0.93 KB` | **Lines:** `24` | **Language:** `java`

```java
package com.automation.ai.protocol;

public final class AiEventConstants {

    private AiEventConstants() {
    }

    // Health / Liveness
    public static final String AI_HEALTH_STATUS = "ai_health_status";

    // Generic 2-Event Task Protocol (Frontend is Prompt Brain)
    public static final String AI_EXECUTE_TASK = "AI_EXECUTE_TASK";
    public static final String AI_TASK_RESULT = "AI_TASK_RESULT";

    // Legacy event mappings for backward compatibility
    public static final String AI_HOST_RESOLVE_REQ = "ai_host_resolve_req";
    public static final String AI_HOST_RESOLVE_RESP = "ai_host_resolve_resp";
    public static final String AI_REMEDIATE_REQ = "ai_remediate_req";
    public static final String AI_REMEDIATE_RESP = "ai_remediate_resp";

    // Reserved AI Terminal Execution
    public static final String AI_EXECUTE_CMD_REQ = "ai_execute_cmd_req";
    public static final String AI_EXECUTE_CMD_RESP = "ai_execute_cmd_resp";
}

```

---

<a id="file-8"></a>
### 8. `src/main/java/com/automation/ai/protocol/AiProtocol.java`

**Path:** `src/main/java/com/automation/ai/protocol/AiProtocol.java` | **Size:** `5.99 KB` | **Lines:** `178` | **Language:** `java`

```java
package com.automation.ai.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public final class AiProtocol {

    private AiProtocol() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiHealthStatusEvent {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_HEALTH_STATUS;
        @JsonProperty("status")
        public String status; // "OK" or "NOT_OK"
        @JsonProperty("extsnConnected")
        public boolean extsnConnected;
        @JsonProperty("activeTasks")
        public int activeTasks;
        @JsonProperty("queuedTasks")
        public int queuedTasks;
        @JsonProperty("timestamp")
        public long timestamp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteTaskRequest {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_EXECUTE_TASK;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("prompt")
        public String prompt;
        @JsonProperty("filesPath")
        public List<String> filesPath;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiTaskResultResponse {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_TASK_RESULT;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("status")
        public String status; // "RESPONSE" or "FAILED"
        @JsonProperty("rawResponse")
        public String rawResponse;
        @JsonProperty("error")
        public String error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiHostResolveRequest {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_HOST_RESOLVE_REQ;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("query")
        public String query;
        @JsonProperty("knownHosts")
        public List<String> knownHosts;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiHostResolveResponse {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_HOST_RESOLVE_RESP;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("pre_response")
        public String preResponse;
        @JsonProperty("target_hosts")
        public List<String> targetHosts;
        @JsonProperty("review_required")
        public boolean reviewRequired;
        @JsonProperty("review_title")
        public String reviewTitle;
        @JsonProperty("review_description")
        public String reviewDescription;
        @JsonProperty("final_outcome")
        public String finalOutcome;
        @JsonProperty("error")
        public String error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiRemediateRequest {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_REMEDIATE_REQ;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("query")
        public String query;
        @JsonProperty("host")
        public String host;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiRemediateResponse {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_REMEDIATE_RESP;
        @JsonProperty("taskId")
        public String taskId;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("host")
        public String host;
        @JsonProperty("pre_response")
        public String preResponse;
        @JsonProperty("command")
        public String command;
        @JsonProperty("platform")
        public String platform;
        @JsonProperty("review_needed")
        public boolean reviewNeeded;
        @JsonProperty("review_title")
        public String reviewTitle;
        @JsonProperty("review_description")
        public String reviewDescription;
        @JsonProperty("criticality_level")
        public int criticalityLevel;
        @JsonProperty("file_needed")
        public List<String> fileNeeded;
        @JsonProperty("final_outcome")
        public String finalOutcome;
        @JsonProperty("error")
        public String error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteCmdRequest {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_EXECUTE_CMD_REQ;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("host")
        public String host;
        @JsonProperty("command")
        public String command;
        @JsonProperty("platform")
        public String platform;
        @JsonProperty("userId")
        public String userId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiExecuteCmdResponse {
        @JsonProperty("event-type")
        public String eventType = AiEventConstants.AI_EXECUTE_CMD_RESP;
        @JsonProperty("conversationId")
        public String conversationId;
        @JsonProperty("terminalId")
        public String terminalId;
        @JsonProperty("host")
        public String host;
        @JsonProperty("status")
        public String status;
        @JsonProperty("error")
        public String error;
    }
}

```

---

<a id="file-9"></a>
### 9. `src/main/java/com/automation/ai/service/AiOrchestratorService.java`

**Path:** `src/main/java/com/automation/ai/service/AiOrchestratorService.java` | **Size:** `17.11 KB` | **Lines:** `350` | **Language:** `java`

````java
package com.automation.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.automation.ai.client.AiAgentWebSocketClient;
import com.automation.ai.protocol.AiEventConstants;
import com.automation.ai.protocol.AiProtocol.AiExecuteCmdResponse;
import com.automation.ai.protocol.AiProtocol.AiHealthStatusEvent;
import com.automation.ai.protocol.AiProtocol.AiHostResolveResponse;
import com.automation.ai.protocol.AiProtocol.AiRemediateResponse;
import com.automation.protocol.TerminalProtocol.TerminalStartEvent;
import com.automation.service.TerminalOrchestratorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AiOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AiOrchestratorService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PromptTemplateService promptTemplateService;
    private final AiAgentWebSocketClient aiClient;
    private final TerminalOrchestratorService terminalOrchestratorService;

    public AiOrchestratorService(PromptTemplateService promptTemplateService,
                                 AiAgentWebSocketClient aiClient,
                                 @Lazy TerminalOrchestratorService terminalOrchestratorService) {
        this.promptTemplateService = promptTemplateService;
        this.aiClient = aiClient;
        this.terminalOrchestratorService = terminalOrchestratorService;
    }

    public void handleHealthUpdate(String status, boolean extsnConnected, int activeTasks, int queuedTasks) {
        AiHealthStatusEvent health = new AiHealthStatusEvent();
        health.status = status;
        health.extsnConnected = extsnConnected;
        health.activeTasks = activeTasks;
        health.queuedTasks = queuedTasks;
        health.timestamp = System.currentTimeMillis();

        try {
            terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(health));
        } catch (Exception e) {
            log.warn("Failed to broadcast AI_HEALTH_STATUS: {}", e.getMessage());
        }
    }

    public void broadcastToUi(String jsonMessage) {
        terminalOrchestratorService.broadcastToUi(jsonMessage);
    }

    public void handleUiAiMessage(String wireEvent, JsonNode root) {
        if (wireEvent == null) return;
        String eventUpper = wireEvent.toUpperCase();
        try {
            switch (eventUpper) {
                case "AI_EXECUTE_TASK" -> processGenericExecuteTask(root);
                case "AI_HOST_RESOLVE_REQ" -> processHostResolution(root);
                case "AI_REMEDIATE_REQ" -> processRemediation(root);
                case "AI_EXECUTE_CMD_REQ" -> processExecutionOnAiTerminal(root);
                case "AI_DELETE_REQ", "AI_DELETE_CONVERSATION_REQ" -> processDeleteTask(root);
                default -> {
                    if ("AI_HOST_RESOLVE_RESP".equals(eventUpper) || "AI_REMEDIATE_RESP".equals(eventUpper) || "AI_TASK_RESULT".equals(eventUpper) || "AI_EXECUTE_CMD_RESP".equals(eventUpper) || "AI_DELETE_RESP".equals(eventUpper) || "AI_HEALTH".equals(eventUpper) || "AI_HEALTH_STATUS".equals(eventUpper)) {
                        return;
                    }
                    log.warn("Unknown AI event-type: {}", wireEvent);
                }
            }
        } catch (Exception e) {
            log.error("Error handling UI AI message {}: {}", wireEvent, e.getMessage(), e);
        }
    }

    private void processDeleteTask(JsonNode root) {
        String rawConvId = root.path("conversationId").asText(null);
        if (rawConvId == null || rawConvId.isBlank() || "null".equalsIgnoreCase(rawConvId)) {
            return;
        }
        log.info("Processing AI_DELETE_REQ for convId: {}", rawConvId);
        aiClient.executeDelete(rawConvId.trim());
    }

    private void processGenericExecuteTask(JsonNode root) {
        String rawConvId = root.path("conversationId").asText(null);
        String conversationId = (rawConvId == null || rawConvId.isBlank() || "null".equalsIgnoreCase(rawConvId)) ? null : rawConvId.trim();
        String taskId = root.path("taskId").asText("task_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));
        String prompt = root.path("prompt").asText("");

        log.info("Proxying AI_EXECUTE_TASK for taskId: {}, convId: {}", taskId, conversationId);

        aiClient.executeTask(taskId, conversationId, prompt)
                .thenAccept(rawResponse -> {
                    String cleanJson = cleanJsonFormatting(rawResponse);
                    String returnedConvId = extractConversationIdFromRawResponse(rawResponse, conversationId);

                    com.automation.ai.protocol.AiProtocol.AiTaskResultResponse resp = new com.automation.ai.protocol.AiProtocol.AiTaskResultResponse();
                    resp.taskId = taskId;
                    resp.conversationId = returnedConvId;
                    resp.status = "RESPONSE";
                    resp.rawResponse = cleanJson;

                    try {
                        terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(resp));
                    } catch (Exception e) {
                        log.error("Failed to broadcast AI_TASK_RESULT: {}", e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    log.error("AI generic task failed for taskId {}: {}", taskId, ex.getMessage());
                    com.automation.ai.protocol.AiProtocol.AiTaskResultResponse errResp = new com.automation.ai.protocol.AiProtocol.AiTaskResultResponse();
                    errResp.taskId = taskId;
                    errResp.conversationId = conversationId;
                    errResp.status = "FAILED";
                    errResp.error = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    try {
                        terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(errResp));
                    } catch (Exception ignored) {
                    }
                    return null;
                });
    }

    private void processHostResolution(JsonNode root) {
        String rawConvId = root.path("conversationId").asText(null);
        String conversationId = (rawConvId == null || rawConvId.isBlank() || "null".equalsIgnoreCase(rawConvId)) ? null : rawConvId.trim();
        String taskId = root.path("taskId").asText("task_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));
        String query = root.path("query").asText("");

        log.info("Processing AI_HOST_RESOLVE_REQ for taskId: {}, convId: {}", taskId, conversationId);

        String prompt = promptTemplateService.buildHostSelectionPrompt(query);

        aiClient.executeTask(taskId, conversationId, prompt)
                .thenAccept(rawResponse -> {
                    String cleanJson = cleanJsonFormatting(rawResponse);
                    String returnedConvId = extractConversationIdFromRawResponse(rawResponse, conversationId);

                    AiHostResolveResponse resp = new AiHostResolveResponse();
                    resp.taskId = taskId;
                    resp.conversationId = returnedConvId;

                    try {
                        JsonNode jsonNode = MAPPER.readTree(cleanJson);
                        resp.preResponse = jsonNode.path("pre_response").asText("Analyzing target server inventory...");

                        List<String> hosts = new ArrayList<>();
                        JsonNode hostsNode = jsonNode.path("target_hosts");
                        if (hostsNode.isArray()) {
                            for (JsonNode h : hostsNode) {
                                hosts.add(h.asText());
                            }
                        }
                        resp.targetHosts = hosts;
                        resp.reviewRequired = jsonNode.path("review_required").asBoolean(hosts.isEmpty());
                        resp.reviewTitle = jsonNode.path("review_title").asText(null);
                        resp.reviewDescription = jsonNode.path("review_description").asText(null);
                        resp.finalOutcome = jsonNode.path("final_outcome").asText(null);

                    } catch (Exception e) {
                        log.error("Failed to parse AI host resolution response: {}", e.getMessage());
                        resp.error = "AI Agent error / invalid response: " + e.getMessage();
                        resp.reviewRequired = false;
                        resp.reviewTitle = null;
                        resp.reviewDescription = null;
                    }

                    try {
                        terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(resp));
                    } catch (Exception e) {
                        log.error("Failed to broadcast AI_HOST_RESOLVE_RESP: {}", e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    log.error("AI host resolution task failed: {}", ex.getMessage());
                    AiHostResolveResponse errResp = new AiHostResolveResponse();
                    errResp.taskId = taskId;
                    errResp.conversationId = conversationId;
                    errResp.error = "AI Agent error: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                    errResp.reviewRequired = false;
                    try {
                        terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(errResp));
                    } catch (Exception ignored) {
                    }
                    return null;
                });
    }

    private void processRemediation(JsonNode root) {
        String rawConvId = root.path("conversationId").asText(null);
        String conversationId = (rawConvId == null || rawConvId.isBlank() || "null".equalsIgnoreCase(rawConvId)) ? null : rawConvId.trim();
        String taskId = root.path("taskId").asText("task_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));
        String query = root.path("query").asText("");
        String host = root.path("host").asText("localhost");

        log.info("Processing AI_REMEDIATE_REQ for taskId: {}, host: {}, convId: {}", taskId, host, conversationId);

        String prompt = promptTemplateService.buildDiagnosticsRemediationPrompt(query, host);

        aiClient.executeTask(taskId, conversationId, prompt)
                .thenAccept(rawResponse -> {
                    String cleanJson = cleanJsonFormatting(rawResponse);
                    String returnedConvId = extractConversationIdFromRawResponse(rawResponse, conversationId);

                    AiRemediateResponse resp = new AiRemediateResponse();
                    resp.taskId = taskId;
                    resp.conversationId = returnedConvId;
                    resp.host = host;

                    try {
                        JsonNode jsonNode = MAPPER.readTree(cleanJson);
                        resp.preResponse = jsonNode.path("pre_response").asText("Executing system diagnostic analysis...");
                        resp.command = jsonNode.path("Command").isNull() ? null : jsonNode.path("Command").asText(null);
                        resp.platform = jsonNode.path("platform").asText("PS");
                        resp.reviewNeeded = jsonNode.path("review_needed").asBoolean(false);
                        resp.reviewTitle = jsonNode.path("review_title").asText(null);
                        resp.reviewDescription = jsonNode.path("review_description").asText(null);
                        resp.criticalityLevel = jsonNode.path("CriticalityLevele").asInt(0);

                        JsonNode filesNode = jsonNode.path("file_needed");
                        if (filesNode.isArray()) {
                            List<String> files = new ArrayList<>();
                            for (JsonNode f : filesNode) {
                                files.add(f.asText());
                            }
                            resp.fileNeeded = files;
                        }
                        resp.finalOutcome = jsonNode.path("final_outcome").asText(null);

                    } catch (Exception e) {
                        log.error("Failed to parse AI remediation response: {}", e.getMessage());
                        resp.error = "Failed to parse remediation output: " + e.getMessage();
                        resp.reviewNeeded = false;
                    }

                    try {
                        terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(resp));
                    } catch (Exception e) {
                        log.error("Failed to broadcast AI_REMEDIATE_RESP: {}", e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    log.error("AI remediation task failed: {}", ex.getMessage());
                    AiRemediateResponse errResp = new AiRemediateResponse();
                    errResp.taskId = taskId;
                    errResp.conversationId = conversationId;
                    errResp.host = host;
                    errResp.error = "AI Agent error: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                    errResp.reviewNeeded = false;
                    try {
                        terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(errResp));
                    } catch (Exception ignored) {
                    }
                    return null;
                });
    }

    private String extractConversationIdFromRawResponse(String rawResponse, String fallbackConvId) {
        if (rawResponse == null) return fallbackConvId;
        try {
            JsonNode node = MAPPER.readTree(rawResponse);
            if (node.has("conversationId") && !node.path("conversationId").isNull()) {
                String cId = node.path("conversationId").asText(null);
                if (cId != null && !cId.isBlank() && !"null".equalsIgnoreCase(cId)) {
                    return cId;
                }
            }
        } catch (Exception ignored) {
        }
        return fallbackConvId;
    }



    private void processExecutionOnAiTerminal(JsonNode root) {
        String conversationId = root.path("conversationId").asText("");
        String host = root.path("host").asText("localhost");
        String command = root.path("command").asText("");
        String platform = root.path("platform").asText("PS").toUpperCase();
        String userId = root.path("userId").asText("user");

        String platformSuffix = "CMD".equalsIgnoreCase(platform) ? "CMD" : "PS";
        String reservedTerminalId = userId + "_ASR_AI_" + platformSuffix;

        log.info("Executing AI command on reserved terminal {} at host {}", reservedTerminalId, host);

        AiExecuteCmdResponse resp = new AiExecuteCmdResponse();
        resp.conversationId = conversationId;
        resp.host = host;
        resp.terminalId = reservedTerminalId;

        try {
            // 1. Ensure reserved terminal is started on target host
            String startPayload = String.format(
                    "{\"event-type\":\"terminal_start\",\"terminalid\":\"%s\",\"host\":\"%s\",\"userId\":\"%s\",\"platform\":\"%s\",\"name\":\"ASR AI (%s)\"}",
                    reservedTerminalId, host, userId, platformSuffix, platformSuffix
            );
            terminalOrchestratorService.handleUiMessageDirect(startPayload);

            // Short delay to ensure session registration if fresh
            Thread.sleep(200);

            // 2. Dispatch execute frame
            String execPayload = String.format(
                    "{\"event-type\":\"terminal_execute\",\"terminalid\":\"%s\",\"host\":\"%s\",\"userId\":\"%s\",\"command\":\"%s\"}",
                    reservedTerminalId, host, userId, escapeJson(command)
            );
            terminalOrchestratorService.handleUiMessageDirect(execPayload);

            resp.status = "SUCCESS";

        } catch (Exception e) {
            log.error("Failed to execute command on AI reserved terminal {}: {}", reservedTerminalId, e.getMessage());
            resp.status = "FAILED";
            resp.error = e.getMessage();
        }

        try {
            terminalOrchestratorService.broadcastToUi(MAPPER.writeValueAsString(resp));
        } catch (Exception e) {
            log.warn("Failed to broadcast AI_EXECUTE_CMD_RESP: {}", e.getMessage());
        }
    }

    private String cleanJsonFormatting(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }
}

````

---

<a id="file-10"></a>
### 10. `src/main/java/com/automation/ai/service/PromptTemplateService.java`

**Path:** `src/main/java/com/automation/ai/service/PromptTemplateService.java` | **Size:** `4.40 KB` | **Lines:** `114` | **Language:** `java`

```java
package com.automation.ai.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.automation.config.ServerInventory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PromptTemplateService {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${ai.prompt.path:C:/Ranjan/AutomationSuite_v2/PromptTesting/final_prompts}")
    private String promptDirPath;

    public String buildHostSelectionPrompt(String userQuery) {
        String template = loadTemplate("TargetHostSelectionPrompt.txt");
        String inventoryJson = buildServerInventoryJson();
        return template.replace("{USER_QUERY}", escapePromptValue(userQuery))
                       .replace("{SERVER_INVENTORY}", inventoryJson);
    }

    public String buildDiagnosticsRemediationPrompt(String userQuery, String hostname) {
        String template = loadTemplate("SystemDiagnosticsRemediationPrompt.txt");
        if (template.contains("{HOSTNAME}")) {
            template = template.replace("{HOSTNAME}", hostname);
        }
        if (template.contains("{USER_QUERY}")) {
            template = template.replace("{USER_QUERY}", escapePromptValue(userQuery));
        }
        return template;
    }

    private String loadTemplate(String filename) {
        try {
            Path path = Path.of(promptDirPath, filename);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
            // Fallback to alternate folder if missing
            Path altPath = Path.of("C:\\Ranjan\\AutomationSuite_v2\\PromptTesting\\Finalised Prompts", filename);
            if (Files.exists(altPath)) {
                return Files.readString(altPath);
            }
        } catch (Exception e) {
            log.error("Failed to read prompt file {}: {}", filename, e.getMessage());
        }
        log.warn("Using fallback internal template for {}", filename);
        return getFallbackTemplate(filename);
    }

    private String buildServerInventoryJson() {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(ServerInventory.SERVERS);
        } catch (Exception e) {
            log.error("Error serializing ServerInventory: {}", e.getMessage());
            return "[]";
        }
    }

    private String escapePromptValue(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
    }

    private String getFallbackTemplate(String filename) {
        if ("TargetHostSelectionPrompt.txt".equalsIgnoreCase(filename)) {
            return """
                You are an AI Server Infrastructure & Target Resolution Agent. You MUST respond ONLY with a single valid raw JSON object matching the schema below.
                User Input Query: "{USER_QUERY}"
                Server Inventory:
                {SERVER_INVENTORY}
                Output JSON Schema:
                {
                  "pre_response": "<short status message>",
                  "target_hosts": ["H1187"],
                  "review_required": false,
                  "review_title": null,
                  "review_description": null,
                  "final_outcome": "<explanation>"
                }
                Return ONLY the JSON object.
                """;
        } else {
            return """
                You are an AI System Automation Agent. You MUST respond ONLY with a single valid raw JSON object matching the schema below.
                User Query: "{USER_QUERY}"
                Target Hostname: "{HOSTNAME}"
                Output JSON Schema:
                {
                  "host": "{HOSTNAME}",
                  "pre_response": "<short status message>",
                  "Command": "<command or null>",
                  "platform": "PS",
                  "review_needed": false,
                  "review_title": null,
                  "review_description": null,
                  "CriticalityLevele": 0,
                  "file_needed": null,
                  "final_outcome": "<summary>"
                }
                Return ONLY the JSON object.
                """;
        }
    }
}

```

---

<a id="file-11"></a>
### 11. `src/main/java/com/automation/config/AppProperties.java`

**Path:** `src/main/java/com/automation/config/AppProperties.java` | **Size:** `0.84 KB` | **Lines:** `22` | **Language:** `java`

```java
package com.automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agent")
public class AppProperties {

    private int defaultPort = 9010;
    private String secretKey = "zGQ3aJB3IluC5eeU";
    private long reconnectIntervalMs = 5000;

    public int getDefaultPort() { return defaultPort; }
    public void setDefaultPort(int defaultPort) { this.defaultPort = defaultPort; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public long getReconnectIntervalMs() { return reconnectIntervalMs; }
    public void setReconnectIntervalMs(long reconnectIntervalMs) { this.reconnectIntervalMs = reconnectIntervalMs; }
}

```

---

<a id="file-12"></a>
### 12. `src/main/java/com/automation/config/GlobalWebSocketConfig.java`

**Path:** `src/main/java/com/automation/config/GlobalWebSocketConfig.java` | **Size:** `0.89 KB` | **Lines:** `25` | **Language:** `java`

```java
package com.automation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.automation.controller.GlobalWebSocketHandler;

@Configuration
@EnableWebSocket
public class GlobalWebSocketConfig implements WebSocketConfigurer {

    private final GlobalWebSocketHandler globalWebSocketHandler;

    public GlobalWebSocketConfig(GlobalWebSocketHandler globalWebSocketHandler) {
        this.globalWebSocketHandler = globalWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(globalWebSocketHandler, "/ws/app")
                .setAllowedOrigins("*");
    }
}

```

---

<a id="file-13"></a>
### 13. `src/main/java/com/automation/config/ServerInventory.java`

**Path:** `src/main/java/com/automation/config/ServerInventory.java` | **Size:** `9.37 KB` | **Lines:** `140` | **Language:** `java`

```java
package com.automation.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth Java constant class defining all managed target servers
 * copied directly from V1 (com.sunrise.automation_suite.config.ServerInventory).
 */
public final class ServerInventory {

    public enum ServerType {
        INSPIRE_AUTOMATION,
        INSPIRE_SCALER,
        IPS_POOL,
        INTERACTIVE
    }

    public enum Environment {
        PROD,
        T01,
        T02,
        T03,
        T04,
        DEV
    }

    public enum ServiceEnum {
        IPS,
        IPS1,
        IPS2,
        IPS_II,
        INSPIRE_PRODUCTION_SERVER,
        IA,
        INSPIRE_AUTOMATION,
        ICM,
        INSPIRE_CONTENT_MANAGER,
        INSPIRE_LICENCE_MANAGER,
        INSPIRE_SCALER,
        SCALER,
        INTERACTIVE,
        INSPIRE_INTERACTIVE
    }

    public static class ServerConfig {
        public final String host;
        public final ServerType serverType;
        public final Environment env;
        public final List<ServerType> workerTypes;
        public final List<ServiceEnum> expectedServices;
        public final String displayName;

        public ServerConfig(String host, ServerType serverType, Environment env,
                            List<ServerType> workerTypes, List<ServiceEnum> expectedServices,
                            String displayName) {
            this.host = host;
            this.serverType = serverType;
            this.env = env;
            this.workerTypes = workerTypes;
            this.expectedServices = expectedServices;
            this.displayName = displayName;
        }

        // Getters for Jackson JSON serialization
        public String getHost() { return host; }
        public ServerType getServerType() { return serverType; }
        public ServerType getCategory() { return serverType; }
        public Environment getEnv() { return env; }
        public Environment getEnvironment() { return env; }
        public List<ServerType> getWorkerTypes() { return workerTypes; }
        public List<ServiceEnum> getExpectedServices() { return expectedServices; }
        public String getDisplayName() { return displayName; }
        public int getPort() { return 9010; }
    }

    public static final List<ServerConfig> SERVERS;

    static {
        List<ServerConfig> list = new ArrayList<>();

        // Localhost dev agent for personal laptop testing
        list.add(new ServerConfig("localhost", ServerType.INSPIRE_AUTOMATION, Environment.DEV,
                List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "Localhost Dev Agent"));

        // -----------------------------------------------------------------
        // 1. INSPIRE AUTOMATION (IA) - 8 Servers
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1191", ServerType.INSPIRE_AUTOMATION, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA, ServiceEnum.INSPIRE_AUTOMATION), "IA Prod 01"));
        list.add(new ServerConfig("H1375", ServerType.INSPIRE_AUTOMATION, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA, ServiceEnum.INSPIRE_AUTOMATION), "IA Prod 02"));
        list.add(new ServerConfig("H1190", ServerType.INSPIRE_AUTOMATION, Environment.T01, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T01"));
        list.add(new ServerConfig("H1376", ServerType.INSPIRE_AUTOMATION, Environment.T01, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T01 Worker"));
        list.add(new ServerConfig("H1189", ServerType.INSPIRE_AUTOMATION, Environment.T02, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T02"));
        list.add(new ServerConfig("H1188", ServerType.INSPIRE_AUTOMATION, Environment.T03, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T03"));
        list.add(new ServerConfig("H1749", ServerType.INSPIRE_AUTOMATION, Environment.T04, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA T04"));
        list.add(new ServerConfig("H1187", ServerType.INSPIRE_AUTOMATION, Environment.DEV, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IA), "IA DEV 01"));

        // -----------------------------------------------------------------
        // 2. IPS POOL (Dedicated for IA) - 9 Servers
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1628", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 03"));
        list.add(new ServerConfig("H1622", ServerType.INTERACTIVE, Environment.PROD, List.of(ServerType.INTERACTIVE, ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.INTERACTIVE, ServiceEnum.IPS), "Interactive Prod && IA Prod IPS Pool"));
        list.add(new ServerConfig("H1623", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 04"));
        list.add(new ServerConfig("H2154", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 05"));
        list.add(new ServerConfig("H2155", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 06"));
        list.add(new ServerConfig("H2176", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 07"));
        list.add(new ServerConfig("H2177", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 08"));
        list.add(new ServerConfig("H2178", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 09"));
        list.add(new ServerConfig("H2179", ServerType.IPS_POOL, Environment.PROD, List.of(ServerType.INSPIRE_AUTOMATION), List.of(ServiceEnum.IPS), "IA IPS Pool Prod 10"));

        // -----------------------------------------------------------------
        // 3. INSPIRE SCALER - 12 Servers
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1351", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 01"));
        list.add(new ServerConfig("H1352", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 02"));
        list.add(new ServerConfig("H1758", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 03"));
        list.add(new ServerConfig("H1748", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 04"));
        list.add(new ServerConfig("H1750", ServerType.INSPIRE_SCALER, Environment.PROD, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler Prod 05"));
        list.add(new ServerConfig("H1350", ServerType.INSPIRE_SCALER, Environment.T01, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T01"));
        list.add(new ServerConfig("H1630", ServerType.INSPIRE_SCALER, Environment.T01, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T01 Worker A"));
        list.add(new ServerConfig("H1629", ServerType.INSPIRE_SCALER, Environment.T01, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T01 Worker B"));
        list.add(new ServerConfig("H1349", ServerType.INSPIRE_SCALER, Environment.T02, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T02"));
        list.add(new ServerConfig("H1348", ServerType.INSPIRE_SCALER, Environment.T03, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T03"));
        list.add(new ServerConfig("H1751", ServerType.INSPIRE_SCALER, Environment.T04, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler T04"));
        list.add(new ServerConfig("H1347", ServerType.INSPIRE_SCALER, Environment.DEV, List.of(ServerType.INSPIRE_SCALER), List.of(ServiceEnum.INSPIRE_SCALER), "Inspire Scaler DEV"));

        // -----------------------------------------------------------------
        // 4. INTERACTIVE - 5 Dedicated/Worker Hosts
        // -----------------------------------------------------------------
        list.add(new ServerConfig("H1626", ServerType.INTERACTIVE, Environment.T01, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive T01"));
        list.add(new ServerConfig("H1625", ServerType.INTERACTIVE, Environment.T02, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive T02"));
        list.add(new ServerConfig("H1624", ServerType.INTERACTIVE, Environment.T03, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive T03"));
        list.add(new ServerConfig("H1627", ServerType.INTERACTIVE, Environment.DEV, List.of(ServerType.INTERACTIVE), List.of(ServiceEnum.INTERACTIVE), "Interactive DEV"));

        SERVERS = Collections.unmodifiableList(list);
    }

    private ServerInventory() {
    }
}

```

---

<a id="file-14"></a>
### 14. `src/main/java/com/automation/controller/GlobalWebSocketHandler.java`

**Path:** `src/main/java/com/automation/controller/GlobalWebSocketHandler.java` | **Size:** `1.89 KB` | **Lines:** `47` | **Language:** `java`

```java
package com.automation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.automation.service.TerminalOrchestratorService;

@Component
public class GlobalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalWebSocketHandler.class);

    private final TerminalOrchestratorService orchestratorService;

    public GlobalWebSocketHandler(TerminalOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Browser client connected to Global WebSocket (/ws/app): {}", session.getId());
        orchestratorService.registerUiSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.trace("Received payload from browser UI session {}: {}", session.getId(), payload);
        orchestratorService.handleUiMessage(session, payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Browser client disconnected from Global WebSocket: {} (status={})", session.getId(), status);
        orchestratorService.unregisterUiSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("Transport error for UI WebSocket session {}: {}", session.getId(), exception.getMessage());
    }
}

```

---

<a id="file-15"></a>
### 15. `src/main/java/com/automation/controller/ServerInventoryController.java`

**Path:** `src/main/java/com/automation/controller/ServerInventoryController.java` | **Size:** `2.40 KB` | **Lines:** `60` | **Language:** `java`

```java
package com.automation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.automation.agent.AgentClientManager;
import com.automation.agent.AgentConnectionStatus;
import com.automation.config.ServerInventory;
import com.automation.config.ServerInventory.ServerConfig;
import com.automation.service.TerminalOrchestratorService;

@RestController
@RequestMapping("/api/v2/servers")
@CrossOrigin(origins = "*")
public class ServerInventoryController {

    private final AgentClientManager agentClientManager;
    private final TerminalOrchestratorService terminalOrchestratorService;

    public ServerInventoryController(AgentClientManager agentClientManager,
                                     TerminalOrchestratorService terminalOrchestratorService) {
        this.agentClientManager = agentClientManager;
        this.terminalOrchestratorService = terminalOrchestratorService;
    }

    @GetMapping("/inventory")
    public List<ServerConfig> getInventory() {
        return ServerInventory.SERVERS;
    }

    @GetMapping("/status")
    public Map<String, AgentConnectionStatus> getStatus() {
        return agentClientManager.getStatusMap();
    }

    @PutMapping("/terminals/{terminalId}/rename")
    public ResponseEntity<?> renameTerminal(@PathVariable String terminalId, @RequestBody Map<String, String> body) {
        String newName = body.get("name");
        if (newName == null || newName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name cannot be empty"));
        }
        boolean success = terminalOrchestratorService.renameTerminalSession(terminalId, newName.trim());
        if (success) {
            return ResponseEntity.ok(Map.of("status", "success", "terminalId", terminalId, "name", newName.trim()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Terminal session not found"));
        }
    }
}

```

---

<a id="file-16"></a>
### 16. `src/main/java/com/automation/domain/TerminalCommandLog.java`

**Path:** `src/main/java/com/automation/domain/TerminalCommandLog.java` | **Size:** `2.19 KB` | **Lines:** `71` | **Language:** `java`

```java
package com.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "terminal_command_log")
public class TerminalCommandLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "command", nullable = false, columnDefinition = "TEXT")
    private String command;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    public TerminalCommandLog() {
    }

    public TerminalCommandLog(String id, String terminalId, String userId, String host, String command, long timestamp) {
        this(id, terminalId, userId, host, command, null, timestamp);
    }

    public TerminalCommandLog(String id, String terminalId, String userId, String host, String command, String result, long timestamp) {
        this.id = id;
        this.terminalId = terminalId;
        this.userId = userId;
        this.host = host;
        this.command = command;
        this.result = result;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

```

---

<a id="file-17"></a>
### 17. `src/main/java/com/automation/domain/TerminalSessionLog.java`

**Path:** `src/main/java/com/automation/domain/TerminalSessionLog.java` | **Size:** `2.78 KB` | **Lines:** `89` | **Language:** `java`

```java
package com.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "terminal_session_log")
public class TerminalSessionLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "current_path", length = 1024)
    private String currentPath;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private long lastActivityAt;

    public TerminalSessionLog() {
    }

    public TerminalSessionLog(String id, String terminalId, String userId, String name, String host,
                              String platform, String currentPath, String state, long createdAt, long lastActivityAt) {
        this.id = id;
        this.terminalId = terminalId;
        this.userId = userId;
        this.name = name;
        this.host = host;
        this.platform = platform;
        this.currentPath = currentPath;
        this.state = state;
        this.createdAt = createdAt;
        this.lastActivityAt = lastActivityAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getCurrentPath() { return currentPath; }
    public void setCurrentPath(String currentPath) { this.currentPath = currentPath; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(long lastActivityAt) { this.lastActivityAt = lastActivityAt; }
}

```

---

<a id="file-18"></a>
### 18. `src/main/java/com/automation/protocol/TerminalProtocol.java`

**Path:** `src/main/java/com/automation/protocol/TerminalProtocol.java` | **Size:** `5.15 KB` | **Lines:** `163` | **Language:** `java`

```java
package com.automation.protocol;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TerminalProtocol {

    private TerminalProtocol() {
    }

    public enum TerminalEventType {
        TERMINAL_START("terminal_start"),
        TERMINAL_EXECUTE("terminal_execute"),
        TERMINAL_STOP("terminal_stop"),
        TERMINAL_CLOSE("terminal_close"),
        TERMINAL_REMOVE("terminal_remove"),
        TERMINAL_RESULT("terminal_result"),
        TERMINAL_ERROR("terminal_error"),
        TERMINAL_STATUS("terminal_status"),
        TERMINAL_MAX("terminal_max"),
        METADATA_LAST_RESULT("metadata_lastResult"),
        METADATA_LAST_ERROR("metadata_lastError"),
        METADATA_LIST("metadata_list"),
        TERMINAL_HISTORY("terminal_history");

        private final String wire;

        TerminalEventType(String wire) {
            this.wire = wire;
        }

        public String wire() {
            return wire;
        }

        public static TerminalEventType fromWire(String wire) {
            if (wire == null) return null;
            for (TerminalEventType t : values()) {
                if (t.wire.equalsIgnoreCase(wire)) return t;
            }
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStartEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_START.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("platform")
        public String platform;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalExecuteEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_EXECUTE.wire();
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("command")
        public String command;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStopEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_STOP.wire();
        @JsonProperty("terminalid")
        public String terminalId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalCloseEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_CLOSE.wire();
        @JsonProperty("terminalid")
        public String terminalId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalRemoveEvent {
        @JsonProperty("event-type")
        public String eventType = TerminalEventType.TERMINAL_REMOVE.wire();
        @JsonProperty("terminalid")
        public String terminalId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalResultEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("chunk")
        public String chunk;
        @JsonProperty("count")
        public int count;
        @JsonProperty("end")
        public boolean end;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalErrorEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("errorCode")
        public String errorCode;
        @JsonProperty("chunk")
        public String chunk;
        @JsonProperty("count")
        public int count;
        @JsonProperty("end")
        public boolean end;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TerminalStatusEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminalid")
        public String terminalId;
        @JsonProperty("pid")
        public long pid;
        @JsonProperty("platform")
        public String platform;
        @JsonProperty("currentPath")
        public String currentPath;
        @JsonProperty("lastCommand")
        public String lastCommand;
        @JsonProperty("lastErrorCode")
        public String lastErrorCode;
        @JsonProperty("lastResult")
        public String lastResult;
        @JsonProperty("lastError")
        public String lastError;
        @JsonProperty("state")
        public String state;
        @JsonProperty("lastInputTimestamp")
        public Long lastInputTimestamp;
        @JsonProperty("lastOutputTimestamp")
        public Long lastOutputTimestamp;
        @JsonProperty("elevated")
        public Boolean elevated;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataListResponseEvent {
        @JsonProperty("event-type")
        public String eventType;
        @JsonProperty("terminals")
        public List<TerminalStatusEvent> terminals;
    }
}

```

---

<a id="file-19"></a>
### 19. `src/main/java/com/automation/repository/TerminalCommandLogRepository.java`

**Path:** `src/main/java/com/automation/repository/TerminalCommandLogRepository.java` | **Size:** `1.17 KB` | **Lines:** `31` | **Language:** `java`

```java
package com.automation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.automation.domain.TerminalCommandLog;

@Repository
public interface TerminalCommandLogRepository extends JpaRepository<TerminalCommandLog, String> {

    List<TerminalCommandLog> findByTerminalIdOrderByTimestampAsc(String terminalId);

    java.util.Optional<TerminalCommandLog> findTopByTerminalIdOrderByTimestampDesc(String terminalId);

    @Modifying
    @Query("UPDATE TerminalCommandLog c SET c.result = NULL WHERE c.timestamp < :cutoff AND c.result IS NOT NULL")
    int nullifyResultsOlderThan(@Param("cutoff") long cutoff);

    @Modifying
    @Query("DELETE FROM TerminalCommandLog c WHERE c.timestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") long cutoff);

    @Modifying
    @Query("DELETE FROM TerminalCommandLog c WHERE c.terminalId = :terminalId")
    void deleteByTerminalId(@Param("terminalId") String terminalId);
}

```

---

<a id="file-20"></a>
### 20. `src/main/java/com/automation/repository/TerminalSessionRepository.java`

**Path:** `src/main/java/com/automation/repository/TerminalSessionRepository.java` | **Size:** `1.02 KB` | **Lines:** `29` | **Language:** `java`

```java
package com.automation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.automation.domain.TerminalSessionLog;

@Repository
public interface TerminalSessionRepository extends JpaRepository<TerminalSessionLog, String> {

    Optional<TerminalSessionLog> findByTerminalId(String terminalId);

    List<TerminalSessionLog> findByUserId(String userId);

    List<TerminalSessionLog> findByHost(String host);

    @Query("SELECT t FROM TerminalSessionLog t WHERE t.state = 'CLOSED' AND t.lastActivityAt < :cutoff")
    List<TerminalSessionLog> findExpiredClosedSessions(@Param("cutoff") long cutoff);

    @Modifying
    @Query("DELETE FROM TerminalSessionLog t WHERE t.id IN :ids")
    void deleteAllByIds(@Param("ids") List<String> ids);
}

```

---

<a id="file-21"></a>
### 21. `src/main/java/com/automation/service/SessionLogCleanupScheduler.java`

**Path:** `src/main/java/com/automation/service/SessionLogCleanupScheduler.java` | **Size:** `3.56 KB` | **Lines:** `75` | **Language:** `java`

```java
package com.automation.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.automation.agent.AgentClientManager;
import com.automation.domain.TerminalSessionLog;
import com.automation.protocol.TerminalProtocol;
import com.automation.repository.TerminalCommandLogRepository;
import com.automation.repository.TerminalSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SessionLogCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(SessionLogCleanupScheduler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final long SEVEN_DAYS_MS = TimeUnit.DAYS.toMillis(7);
    private static final long TWENTY_FIVE_DAYS_MS = TimeUnit.DAYS.toMillis(25);

    private final TerminalSessionRepository sessionRepository;
    private final TerminalCommandLogRepository commandLogRepository;
    private final AgentClientManager agentClientManager;

    public SessionLogCleanupScheduler(TerminalSessionRepository sessionRepository,
                                       TerminalCommandLogRepository commandLogRepository,
                                       AgentClientManager agentClientManager) {
        this.sessionRepository = sessionRepository;
        this.commandLogRepository = commandLogRepository;
        this.agentClientManager = agentClientManager;
    }

    /** Runs every day at 02:00 AM (or 1 hour initial delay in dev) to enforce 7-day session and 25-day command retention policies. */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredLogs() {
        long now = System.currentTimeMillis();
        long sessionCutoff = now - SEVEN_DAYS_MS;
        long commandCutoff = now - TWENTY_FIVE_DAYS_MS;

        log.info("Starting background retention cleanup: sessionCutoff={} (7d), commandCutoff={} (25d)", sessionCutoff, commandCutoff);

        // 1. Clean up CLOSED sessions > 7 days old
        List<TerminalSessionLog> expiredSessions = sessionRepository.findExpiredClosedSessions(sessionCutoff);
        if (!expiredSessions.isEmpty()) {
            for (TerminalSessionLog s : expiredSessions) {
                try {
                    // Send terminal_remove frame to host agent to purge metadata from memory
                    TerminalProtocol.TerminalRemoveEvent removeEvent = new TerminalProtocol.TerminalRemoveEvent();
                    removeEvent.terminalId = s.getTerminalId();
                    String json = MAPPER.writeValueAsString(removeEvent);
                    agentClientManager.sendToAgent(s.getHost(), json);
                } catch (Exception e) {
                    log.warn("Failed to notify agent of terminal_remove for terminal {}: {}", s.getTerminalId(), e.getMessage());
                }
            }

            List<String> idsToDelete = expiredSessions.stream().map(TerminalSessionLog::getId).collect(Collectors.toList());
            sessionRepository.deleteAllByIds(idsToDelete);
            log.info("Purged {} expired CLOSED terminal session logs (>7 days)", idsToDelete.size());
        }

        // 2. Clean up command execution snapshots > 25 days old
        int deletedCommands = commandLogRepository.deleteOlderThan(commandCutoff);
        log.info("Purged {} expired command execution log snapshots (>25 days)", deletedCommands);
    }
}

```

---

<a id="file-22"></a>
### 22. `src/main/java/com/automation/service/TerminalOrchestratorService.java`

**Path:** `src/main/java/com/automation/service/TerminalOrchestratorService.java` | **Size:** `18.91 KB` | **Lines:** `400` | **Language:** `java`

```java
package com.automation.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.automation.agent.AgentClientManager;
import com.automation.domain.TerminalCommandLog;
import com.automation.domain.TerminalSessionLog;
import com.automation.protocol.TerminalProtocol.TerminalCloseEvent;
import com.automation.protocol.TerminalProtocol.TerminalEventType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.automation.repository.TerminalCommandLogRepository;
import com.automation.repository.TerminalSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.automation.ai.service.AiOrchestratorService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.automation.repository.TerminalCommandLogRepository;
import com.automation.repository.TerminalSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TerminalOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(TerminalOrchestratorService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AgentClientManager agentClientManager;
    private final TerminalSessionRepository sessionRepository;
    private final TerminalCommandLogRepository commandLogRepository;
    private final AiOrchestratorService aiOrchestratorService;

    private final Map<String, WebSocketSession> uiSessions = new ConcurrentHashMap<>();

    public TerminalOrchestratorService(AgentClientManager agentClientManager,
                                       TerminalSessionRepository sessionRepository,
                                       TerminalCommandLogRepository commandLogRepository,
                                       @Lazy AiOrchestratorService aiOrchestratorService) {
        this.agentClientManager = agentClientManager;
        this.sessionRepository = sessionRepository;
        this.commandLogRepository = commandLogRepository;
        this.aiOrchestratorService = aiOrchestratorService;
    }

    public void registerUiSession(WebSocketSession session) {
        uiSessions.put(session.getId(), session);
        log.info("Registered UI WebSocket session: {}", session.getId());
    }

    public void unregisterUiSession(WebSocketSession session) {
        uiSessions.remove(session.getId());
        log.info("Unregistered UI WebSocket session: {}", session.getId());
    }

    @Transactional
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

            if (wire != null && wire.toLowerCase().startsWith("ai_")) {
                aiOrchestratorService.handleUiAiMessage(wire, root);
                return;
            }

            TerminalEventType type = TerminalEventType.fromWire(wire);
            if (type == null) {
                log.warn("Unknown event-type from UI: {}", wire);
                return;
            }

            String terminalId = root.path("terminalid").asText(null);
            String host = root.path("host").asText("localhost");
            String userId = root.path("userId").asText("user");

            // Security Lock: Reject manual UI execution or stop requests targeting reserved AI terminals
            if (terminalId != null && terminalId.contains("_ASR_AI_") && (type == TerminalEventType.TERMINAL_EXECUTE || type == TerminalEventType.TERMINAL_STOP)) {
                log.warn("Rejected manual UI action on reserved AI terminal: {}", terminalId);
                return;
            }

            long now = System.currentTimeMillis();


            switch (type) {
                case TERMINAL_START -> {
                    String name = root.path("name").asText(host + " Terminal");
                    String platform = root.path("platform").asText("PS");

                    TerminalSessionLog sessionLog = sessionRepository.findByTerminalId(terminalId)
                            .orElseGet(() -> new TerminalSessionLog(
                                    UUID.randomUUID().toString(),
                                    terminalId,
                                    userId,
                                    name,
                                    host,
                                    platform,
                                    "C:\\",
                                    "IDLE",
                                    now,
                                    now
                            ));
                    sessionLog.setState("IDLE");
                    sessionLog.setLastActivityAt(now);
                    sessionRepository.save(sessionLog);

                    agentClientManager.sendToAgent(host, payload);
                }
                case TERMINAL_EXECUTE -> {
                    String command = root.path("command").asText("");
                    sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                        s.setState("RUNNING");
                        s.setLastActivityAt(now);
                        sessionRepository.save(s);
                    });

                    // Save command audit log snapshot
                    TerminalCommandLog cmdLog = new TerminalCommandLog(
                            UUID.randomUUID().toString(),
                            terminalId,
                            userId,
                            host,
                            command,
                            now
                    );
                    commandLogRepository.saveAndFlush(cmdLog);

                    agentClientManager.sendToAgent(host, payload);
                }
                case TERMINAL_STOP -> {
                    sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                        s.setLastActivityAt(now);
                        sessionRepository.saveAndFlush(s);
                    });
                    agentClientManager.sendToAgent(host, payload);
                }
                case TERMINAL_CLOSE -> {
                    sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                        s.setState("CLOSED");
                        s.setLastActivityAt(now);
                        sessionRepository.saveAndFlush(s);
                    });
                    agentClientManager.sendToAgent(host, payload);
                }
                case TERMINAL_HISTORY -> {
                    if (terminalId != null) {
                        sendTerminalHistory(session, terminalId);
                    }
                }
                default -> agentClientManager.sendToAgent(host, payload);
            }
        } catch (Exception e) {
            log.error("Error handling UI message: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void handleAgentMessage(String host, String messageJson) {
        try {
            JsonNode root = MAPPER.readTree(messageJson);
            String wire = root.path("event-type").asText(null);
            TerminalEventType type = TerminalEventType.fromWire(wire);

            if (type == TerminalEventType.TERMINAL_STATUS) {
                String terminalId = root.path("terminalid").asText(root.path("terminalId").asText(null));
                String state = root.path("state").asText(null);
                String currentPath = root.path("currentPath").asText(null);
                long now = System.currentTimeMillis();
                long lastOutput = root.path("lastOutputTimestamp").asLong(0);
                long lastInput = root.path("lastInputTimestamp").asLong(0);
                long lastAct = lastOutput > 0 ? lastOutput : (lastInput > 0 ? lastInput : now);

                if (terminalId != null && state != null) {
                    sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                        s.setState(state);
                        if (currentPath != null) {
                            s.setCurrentPath(currentPath);
                        }
                        s.setLastActivityAt(lastAct);
                        sessionRepository.saveAndFlush(s);
                    });
                }
            } else if (type == TerminalEventType.TERMINAL_RESULT) {
                String terminalId = root.path("terminalid").asText(root.path("terminalId").asText(null));
                String chunk = root.path("chunk").asText(null);
                if (terminalId != null && chunk != null && !chunk.isEmpty()) {
                    commandLogRepository.findTopByTerminalIdOrderByTimestampDesc(terminalId).ifPresent(cmd -> {
                        String current = cmd.getResult() == null ? "" : cmd.getResult();
                        cmd.setResult(current + chunk);
                        commandLogRepository.saveAndFlush(cmd);
                    });
                }
            } else if (type == TerminalEventType.METADATA_LIST) {
                if (root instanceof com.fasterxml.jackson.databind.node.ObjectNode rootNode) {
                    rootNode.put("host", host);
                    JsonNode termArray = rootNode.path("terminals");
                    com.fasterxml.jackson.databind.node.ArrayNode arrayNode;
                    if (termArray.isArray() && termArray instanceof com.fasterxml.jackson.databind.node.ArrayNode an) {
                        arrayNode = an;
                    } else {
                        arrayNode = MAPPER.createArrayNode();
                        rootNode.set("terminals", arrayNode);
                    }

                    long now = System.currentTimeMillis();
                    java.util.Set<String> seenIds = new java.util.HashSet<>();

                    for (JsonNode t : arrayNode) {
                        if (t instanceof com.fasterxml.jackson.databind.node.ObjectNode termNode) {
                            termNode.put("host", host);
                            String terminalId = termNode.path("terminalId").asText(termNode.path("terminalid").asText(null));
                            if (terminalId != null) {
                                seenIds.add(terminalId);
                                String name = termNode.path("name").asText(null);
                                String platform = termNode.path("platform").asText("PS");
                                String state = termNode.path("state").asText("IDLE");
                                String currentPath = termNode.path("currentPath").asText("C:\\");
                                long lastOutput = termNode.path("lastOutputTimestamp").asLong(0);
                                long lastInput = termNode.path("lastInputTimestamp").asLong(0);
                                long lastAct = lastOutput > 0 ? lastOutput : (lastInput > 0 ? lastInput : now);

                                TerminalSessionLog sessionLog = sessionRepository.findByTerminalId(terminalId)
                                        .orElseGet(() -> new TerminalSessionLog(
                                                UUID.randomUUID().toString(),
                                                terminalId,
                                                "user",
                                                name != null ? name : terminalId,
                                                host,
                                                platform,
                                                currentPath,
                                                state,
                                                now,
                                                lastAct
                                        ));
                                sessionLog.setState(state);
                                sessionLog.setCurrentPath(currentPath);
                                sessionLog.setLastActivityAt(lastAct);
                                sessionRepository.save(sessionLog);

                                termNode.put("name", sessionLog.getName());
                                // History lines are fetched separately via terminal_history event
                            }
                        }
                    }

                    // Merge historical DB terminal sessions for this host (e.g. CLOSED sessions)
                    List<TerminalSessionLog> dbSessions = sessionRepository.findByHost(host);
                    for (TerminalSessionLog dbSess : dbSessions) {
                        if (!seenIds.contains(dbSess.getTerminalId())) {
                            com.fasterxml.jackson.databind.node.ObjectNode dbNode = MAPPER.createObjectNode();
                            dbNode.put("terminalId", dbSess.getTerminalId());
                            dbNode.put("host", host);
                            dbNode.put("name", dbSess.getName());
                            dbNode.put("platform", dbSess.getPlatform());
                            dbNode.put("state", dbSess.getState());
                            dbNode.put("currentPath", dbSess.getCurrentPath());
                            dbNode.put("lastActivityAt", dbSess.getLastActivityAt());
                            dbNode.put("elevated", false);
                            arrayNode.add(dbNode);
                        }
                    }

                    messageJson = MAPPER.writeValueAsString(rootNode);
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing agent message for status sync: {}", e.getMessage());
        }

        // Broadcast to all connected browser UI clients
        broadcastToUi(messageJson);
    }

    public void sendTerminalHistory(WebSocketSession uiSession, String terminalId) {
        List<TerminalCommandLog> cmdLogs = commandLogRepository.findByTerminalIdOrderByTimestampAsc(terminalId);
        Optional<TerminalSessionLog> sess = sessionRepository.findByTerminalId(terminalId);
        String platform = sess.map(TerminalSessionLog::getPlatform).orElse("PS");
        String currentPath = sess.map(TerminalSessionLog::getCurrentPath).orElse("C:\\");

        com.fasterxml.jackson.databind.node.ArrayNode linesArr = MAPPER.createArrayNode();
        for (TerminalCommandLog cmd : cmdLogs) {
            String path = currentPath != null && !currentPath.isBlank() ? currentPath : "C:\\";
            String promptPrefix = "PS".equalsIgnoreCase(platform) ? "PS " + path + ">" : path + ">";
            if (cmd.getCommand() != null && !cmd.getCommand().isBlank()) {
                linesArr.add(promptPrefix + " " + cmd.getCommand());
            }
            if (cmd.getResult() != null && !cmd.getResult().isBlank()) {
                String[] parts = cmd.getResult().replace("\r\n", "\n").replace("\r", "\n").split("\n");
                for (String part : parts) {
                    linesArr.add(part);
                }
            } else if (cmd.getResult() == null && cmd.getCommand() != null && !cmd.getCommand().isBlank()) {
                long now = System.currentTimeMillis();
                if (now - cmd.getTimestamp() >= 4L * 24 * 60 * 60 * 1000) {
                    linesArr.add("[Output pruned (>4 days)]");
                }
            }
        }

        com.fasterxml.jackson.databind.node.ObjectNode resp = MAPPER.createObjectNode();
        resp.put("event-type", "terminal_history");
        resp.put("terminalid", terminalId);
        resp.set("lines", linesArr);

        try {
            uiSession.sendMessage(new TextMessage(MAPPER.writeValueAsString(resp)));
        } catch (Exception e) {
            log.warn("Error sending terminal_history to UI: {}", e.getMessage());
        }
    }

    /**
     * Periodic Retention Cleanup:
     * 1. 4-day rule: Nullify heavy output text in command log table to reduce DB size.
     * 2. 15-day rule: Delete all command log transactions older than 15 days.
     * 3. 15-day rule: Delete closed terminal sessions inactive for >= 15 days & purge Agent memory cache.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3600000) // Hourly retention check
    @Transactional
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        long fourDaysAgo = now - (4L * 24 * 60 * 60 * 1000);
        long fifteenDaysAgo = now - (15L * 24 * 60 * 60 * 1000);

        int nullified = commandLogRepository.nullifyResultsOlderThan(fourDaysAgo);
        if (nullified > 0) {
            log.info("Nullified execution results for {} command logs older than 4 days", nullified);
        }

        int deletedCmds = commandLogRepository.deleteOlderThan(fifteenDaysAgo);
        if (deletedCmds > 0) {
            log.info("Purged {} command log transaction rows older than 15 days", deletedCmds);
        }

        List<TerminalSessionLog> expired = sessionRepository.findExpiredClosedSessions(fifteenDaysAgo);
        if (!expired.isEmpty()) {
            for (TerminalSessionLog s : expired) {
                commandLogRepository.deleteByTerminalId(s.getTerminalId());
                String removeMsg = String.format("{\"event-type\":\"terminal_remove\",\"terminalid\":\"%s\",\"host\":\"%s\"}", s.getTerminalId(), s.getHost());
                agentClientManager.sendToAgent(s.getHost(), removeMsg);
            }
            List<String> ids = expired.stream().map(TerminalSessionLog::getId).toList();
            sessionRepository.deleteAllByIds(ids);
            log.info("Cleaned up {} expired closed terminal sessions older than 15 days", ids.size());
        }
    }

    public void broadcastToUi(String jsonMessage) {
        TextMessage message = new TextMessage(jsonMessage);
        for (WebSocketSession s : uiSessions.values()) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(message);
                } catch (Exception e) {
                    log.warn("Error sending message to UI session {}: {}", s.getId(), e.getMessage());
                }
            }
        }
    }

    @Transactional
    public boolean renameTerminalSession(String terminalId, String newName) {
        return sessionRepository.findByTerminalId(terminalId).map(s -> {
            s.setName(newName);
            s.setLastActivityAt(System.currentTimeMillis());
            sessionRepository.save(s);
            log.info("Renamed terminal session {} to {}", terminalId, newName);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void handleUiMessageDirect(String payload) {
        handleUiMessage(null, payload);
    }
}


```

---

<a id="file-23"></a>
### 23. `src/main/resources/application.properties`

**Path:** `src/main/resources/application.properties` | **Size:** `0.71 KB` | **Lines:** `23` | **Language:** `properties`

```properties
spring.application.name=automation_suite_springboot
server.port=9011

BASE_PATH=C:/Ranjan/Automation Suite
DATABASE_PATH=${BASE_PATH}/Database

spring.datasource.url=jdbc:sqlite:${DATABASE_PATH}/automation.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

agent.default-port=9010
agent.secret-key=zGQ3aJB3IluC5eeU
agent.reconnect-interval-ms=5000

ai.agent.ws-url=ws://127.0.0.1:9020/ws/ai_agent
ai.prompt.path=C:/Ranjan/AutomationSuite_v2/PromptTesting/final_prompts


```

---

<a id="file-24"></a>
### 24. `src/main/resources/db/migration/V1__init_terminal_schema.sql`

**Path:** `src/main/resources/db/migration/V1__init_terminal_schema.sql` | **Size:** `0.62 KB` | **Lines:** `21` | **Language:** `sql`

```sql
CREATE TABLE IF NOT EXISTS terminal_session_log (
    id VARCHAR(36) PRIMARY KEY,
    terminal_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    host VARCHAR(255) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    current_path VARCHAR(1024),
    state VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    last_activity_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS terminal_command_log (
    id VARCHAR(36) PRIMARY KEY,
    terminal_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    host VARCHAR(255) NOT NULL,
    command TEXT NOT NULL,
    timestamp BIGINT NOT NULL
);

```

---

<a id="file-25"></a>
### 25. `src/main/resources/db/migration/V2__add_result_column_to_command_log.sql`

**Path:** `src/main/resources/db/migration/V2__add_result_column_to_command_log.sql` | **Size:** `0.06 KB` | **Lines:** `1` | **Language:** `sql`

```sql
ALTER TABLE terminal_command_log ADD COLUMN result TEXT;

```

---

<a id="file-26"></a>
### 26. `src/test/java/com/automation/simulator/AiExecuteTaskFlowTest.java`

**Path:** `src/test/java/com/automation/simulator/AiExecuteTaskFlowTest.java` | **Size:** `4.82 KB` | **Lines:** `109` | **Language:** `java`

```java
package com.automation.simulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.automation.ai.client.AiAgentWebSocketClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AiExecuteTaskFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AiAgentWebSocketClient aiAgentClient;

    @Test
    public void testAiExecuteTaskFlowFromSpringBootToAiAgent() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws/app";
        System.out.println("==================================================");
        System.out.println(" Running AiExecuteTaskFlowTest on SpringBoot port: " + port);
        System.out.println(" UI WebSocket URL: " + wsUrl);
        System.out.println(" AI Agent Client Connected to 9020? " + aiAgentClient.isConnected());
        System.out.println("==================================================");

        // Wait up to 5 seconds for SpringBoot WebSocket client to establish connection to port 9020 if needed
        int waitMs = 0;
        while (!aiAgentClient.isConnected() && waitMs < 5000) {
            Thread.sleep(200);
            waitMs += 200;
        }
        System.out.println(" Connection status after wait: " + aiAgentClient.isConnected());

        CompletableFuture<String> responseFuture = new CompletableFuture<>();

        HttpClient client = HttpClient.newHttpClient();
        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                    private final StringBuilder buffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket ws) {
                        System.out.println("[TestUiClient] Connected to SpringBoot UI WebSocket at " + wsUrl);
                        ws.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        buffer.append(data);
                        if (last) {
                            String msg = buffer.toString();
                            buffer.setLength(0);
                            System.out.println("[TestUiClient] Received message from SpringBoot: " + msg);
                            if (msg.contains("AI_TASK_RESULT") || msg.contains("AI_HOST_RESOLVE_RESP") || msg.contains("AI_REMEDIATE_RESP")) {
                                responseFuture.complete(msg);
                            }
                        }
                        ws.request(1);
                        return null;
                    }

                    @Override
                    public void onError(WebSocket ws, Throwable error) {
                        System.err.println("[TestUiClient] Error: " + error.getMessage());
                        responseFuture.completeExceptionally(error);
                    }
                })
                .get(10, TimeUnit.SECONDS);

        // Prepare exact UI payload sent by Frontend
        String taskId = "test_task_sb_" + System.currentTimeMillis();
        String jsonPayload = String.format(
                "{\"event-type\":\"AI_EXECUTE_TASK\",\"taskId\":\"%s\",\"conversationId\":null,\"prompt\":\"Hello Copilot, reply with exactly the word: BANANA\",\"filesPath\":[]}",
                taskId
        );

        System.out.println("[TestUiClient] Sending AI_EXECUTE_TASK to SpringBoot: " + jsonPayload);
        webSocket.sendText(jsonPayload, true);

        // Wait up to 30 seconds for result
        try {
            String result = responseFuture.get(30, TimeUnit.SECONDS);
            System.out.println("==================================================");
            System.out.println(" TEST SUCCESS! Received AI Task Result from SpringBoot:");
            System.out.println(" " + result);
            System.out.println("==================================================");
            assertTrue(result.contains("AI_TASK_RESULT") || result.contains("RESPONSE"));
        } catch (Exception e) {
            System.err.println("[TestUiClient] Flow test failed or timed out: " + e.getMessage());
            fail("AI_EXECUTE_TASK flow test failed: " + e.getMessage());
        } finally {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Test Complete").get();
            } catch (Exception ignored) {
            }
        }
    }
}

```

---

<a id="file-27"></a>
### 27. `src/test/java/com/automation/simulator/OrchestratorSimulatorTest.java`

**Path:** `src/test/java/com/automation/simulator/OrchestratorSimulatorTest.java` | **Size:** `12.10 KB` | **Lines:** `283` | **Language:** `java`

```java
package com.automation.simulator;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.automation.agent.AgentClientManager;
import com.automation.domain.TerminalCommandLog;
import com.automation.domain.TerminalSessionLog;
import com.automation.repository.TerminalCommandLogRepository;
import com.automation.repository.TerminalSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrchestratorSimulatorTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AgentClientManager agentClientManager;

    @Autowired
    private TerminalSessionRepository sessionRepository;

    @Autowired
    private TerminalCommandLogRepository commandLogRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MockAgentServer mockAgentServer;
    private MockUiClient mockUiClient;

    static class MockAgentServer extends WebSocketServer {
        final BlockingQueue<String> receivedFromSpringBoot = new ArrayBlockingQueue<>(100);
        volatile WebSocket currentConnection;

        public MockAgentServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            this.currentConnection = conn;
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            receivedFromSpringBoot.offer(message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
        }

        @Override
        public void onStart() {
        }

        public void sendToSpringBoot(String json) {
            if (currentConnection != null && currentConnection.isOpen()) {
                currentConnection.send(json);
            }
        }
    }

    static class MockUiClient extends WebSocketClient {
        final BlockingQueue<String> receivedFromSpringBoot = new ArrayBlockingQueue<>(100);

        public MockUiClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
        }

        @Override
        public void onMessage(String message) {
            receivedFromSpringBoot.offer(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // Start Mock Agent Server on port 9010 (or ephemeral)
        mockAgentServer = new MockAgentServer(0);
        mockAgentServer.start();

        int agentPort = mockAgentServer.getPort();
        while (agentPort == 0) {
            Thread.sleep(50);
            agentPort = mockAgentServer.getPort();
        }

        // Register localhost mock agent connection in SpringBoot
        URI agentUri = new URI("ws://127.0.0.1:" + agentPort + "/ws/agent");
        com.automation.agent.AgentWebSocketClient agentClient = new com.automation.agent.AgentWebSocketClient(agentUri, "localhost", agentClientManager);
        agentClient.connectBlocking(5, TimeUnit.SECONDS);
        agentClientManager.registerClient("localhost", agentClient);

        // Connect Mock UI Client to SpringBoot /ws/app
        URI uiUri = new URI("ws://127.0.0.1:" + port + "/ws/app");
        mockUiClient = new MockUiClient(uiUri);
        mockUiClient.connectBlocking(5, TimeUnit.SECONDS);
        assertTrue(mockUiClient.isOpen(), "UI WebSocket client should connect to SpringBoot /ws/app");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockUiClient != null && mockUiClient.isOpen()) {
            mockUiClient.close();
        }
        if (mockAgentServer != null) {
            mockAgentServer.stop();
        }
    }

    @Test
    void testFullTerminalOrchestrationLifecycle() throws Exception {
        String terminalId = "sim_" + UUID.randomUUID().toString().substring(0, 8);

        // 1. Send terminal_start from UI
        String startEvent = String.format(
                "{\"event-type\":\"terminal_start\",\"terminalid\":\"%s\",\"host\":\"localhost\",\"userId\":\"test-user\",\"platform\":\"PS\",\"name\":\"Simulated PS\"}",
                terminalId);
        mockUiClient.send(startEvent);

        // Verify SpringBoot forwarded start payload to Mock Agent
        String agentPayload = mockAgentServer.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(agentPayload, "SpringBoot should forward terminal_start to Agent");
        assertTrue(agentPayload.contains("terminal_start"));

        // Verify Database SessionLog entry with retry for async WS handling
        TerminalSessionLog logEntry = null;
        for (int i = 0; i < 20; i++) {
            logEntry = sessionRepository.findByTerminalId(terminalId).orElse(null);
            if (logEntry != null) break;
            Thread.sleep(50);
        }
        assertNotNull(logEntry, "SpringBoot should create TerminalSessionLog entry in SQLite");
        assertEquals("IDLE", logEntry.getState());
        assertEquals("localhost", logEntry.getHost());

        // 2. Send terminal_execute from UI
        String execEvent = String.format(
                "{\"event-type\":\"terminal_execute\",\"terminalid\":\"%s\",\"host\":\"localhost\",\"userId\":\"test-user\",\"command\":\"dir\"}",
                terminalId);
        mockUiClient.send(execEvent);

        agentPayload = mockAgentServer.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(agentPayload, "SpringBoot should forward terminal_execute to Agent");
        assertTrue(agentPayload.contains("dir"));

        // Verify Audit Command Log created in DB (with retry for transaction commit)
        java.util.List<TerminalCommandLog> cmdLogs = java.util.Collections.emptyList();
        for (int i = 0; i < 20; i++) {
            cmdLogs = commandLogRepository.findByTerminalIdOrderByTimestampAsc(terminalId);
            if (!cmdLogs.isEmpty()) break;
            Thread.sleep(50);
        }
        assertFalse(cmdLogs.isEmpty(), "SpringBoot should record command audit log in SQLite");
        assertEquals("dir", cmdLogs.get(0).getCommand());

        // 3. Agent emits status update back to SpringBoot
        String statusEvent = String.format(
                "{\"event-type\":\"terminal_status\",\"terminalid\":\"%s\",\"state\":\"RUNNING\",\"currentPath\":\"C:\\\\\",\"elevated\":true}",
                terminalId);
        mockAgentServer.sendToSpringBoot(statusEvent);

        // Verify UI client receives status update broadcast from SpringBoot
        String uiReceived = mockUiClient.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(uiReceived, "SpringBoot should broadcast status event to UI");
        assertTrue(uiReceived.contains("RUNNING"));

        // 3b. Agent streams live output chunks (terminal_result)
        String resChunk1 = String.format(
                "{\"event-type\":\"terminal_result\",\"terminalid\":\"%s\",\"chunk\":\"Directory: C:\\\\Users\\\\happy\\r\\n\"}",
                terminalId);
        mockAgentServer.sendToSpringBoot(resChunk1);
        String uiChunk1 = mockUiClient.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(uiChunk1, "UI should receive streaming chunk 1");
        assertTrue(uiChunk1.contains("Directory: C:\\\\Users\\\\happy"));

        String resChunk2 = String.format(
                "{\"event-type\":\"terminal_result\",\"terminalid\":\"%s\",\"chunk\":\"-a---- 23-11-2025 23:52 5161 .bash_history\\r\\n\"}",
                terminalId);
        mockAgentServer.sendToSpringBoot(resChunk2);
        String uiChunk2 = mockUiClient.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(uiChunk2, "UI should receive streaming chunk 2");
        assertTrue(uiChunk2.contains(".bash_history"));

        // 3c. UI requests execution history via terminal_history event
        String historyReq = String.format(
                "{\"event-type\":\"terminal_history\",\"terminalid\":\"%s\"}",
                terminalId);
        mockUiClient.send(historyReq);

        String uiHistoryResp = mockUiClient.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(uiHistoryResp, "SpringBoot should respond with terminal_history");
        JsonNode histNode = MAPPER.readTree(uiHistoryResp);
        assertEquals("terminal_history", histNode.path("event-type").asText());
        assertEquals(terminalId, histNode.path("terminalid").asText());
        assertTrue(histNode.path("lines").isArray());
        assertTrue(histNode.path("lines").size() >= 3);
        assertEquals("PS C:\\> dir", histNode.path("lines").get(0).asText());
        assertEquals("Directory: C:\\Users\\happy", histNode.path("lines").get(1).asText());
        assertEquals("-a---- 23-11-2025 23:52 5161 .bash_history", histNode.path("lines").get(2).asText());

        // 4. Send metadata_list event from UI
        String metadataListReq = "{\"event-type\":\"metadata_list\",\"host\":\"localhost\"}";
        mockUiClient.send(metadataListReq);

        agentPayload = mockAgentServer.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(agentPayload, "SpringBoot should forward metadata_list request to Agent");

        // Agent responds with metadata_list array
        String agentMetadataListResp = String.format(
                "{\"event-type\":\"metadata_list\",\"terminals\":[{\"terminalId\":\"%s\",\"state\":\"IDLE\",\"currentPath\":\"C:\\\\\",\"elevated\":true}]}",
                terminalId);
        mockAgentServer.sendToSpringBoot(agentMetadataListResp);

        uiReceived = mockUiClient.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(uiReceived, "SpringBoot should broadcast enriched metadata_list to UI");
        JsonNode root = MAPPER.readTree(uiReceived);
        assertEquals("metadata_list", root.path("event-type").asText());
        assertEquals("localhost", root.path("host").asText());
        assertEquals("localhost", root.path("terminals").get(0).path("host").asText());
        assertEquals("Simulated PS", root.path("terminals").get(0).path("name").asText());
        // Verify historyLines is NOT embedded in metadata_list
        assertTrue(root.path("terminals").get(0).path("historyLines").isMissingNode(), "metadata_list must NOT contain heavy historyLines");

        // 5. Send terminal_close and terminal_remove from UI
        String closeEvent = String.format(
                "{\"event-type\":\"terminal_close\",\"terminalid\":\"%s\",\"host\":\"localhost\"}",
                terminalId);
        mockUiClient.send(closeEvent);

        agentPayload = mockAgentServer.receivedFromSpringBoot.poll(5, TimeUnit.SECONDS);
        assertNotNull(agentPayload, "SpringBoot should forward terminal_close to Agent");

        // Verify DB session updated to CLOSED with retry loop for async transaction commit
        for (int i = 0; i < 20; i++) {
            logEntry = sessionRepository.findByTerminalId(terminalId).orElse(null);
            if (logEntry != null && "CLOSED".equals(logEntry.getState())) break;
            Thread.sleep(50);
        }
        assertNotNull(logEntry);
        assertEquals("CLOSED", logEntry.getState());
    }
}

```

---

<a id="file-28"></a>
### 28. `src/test/java/com/automation/simulator/RealtimeFlowTest.java`

**Path:** `src/test/java/com/automation/simulator/RealtimeFlowTest.java` | **Size:** `4.84 KB` | **Lines:** `95` | **Language:** `java`

```java
package com.automation.simulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class RealtimeFlowTest {

    public static void main(String[] args) {
        String[] targetPorts = {"9011", "8089", "8080"};
        boolean testExecuted = false;

        for (String port : targetPorts) {
            String wsUrl = "ws://localhost:" + port + "/ws/app";
            System.out.println("==================================================");
            System.out.println(" Attempting connection to running SpringBoot at: " + wsUrl);
            System.out.println("==================================================");

            try {
                CompletableFuture<String> responseFuture = new CompletableFuture<>();
                HttpClient client = HttpClient.newHttpClient();

                WebSocket webSocket = client.newWebSocketBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(3))
                        .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                            private final StringBuilder buffer = new StringBuilder();

                            @Override
                            public void onOpen(WebSocket ws) {
                                System.out.println("[REALTIME-TEST] Connected to SpringBoot UI WebSocket at " + wsUrl);
                                ws.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                                buffer.append(data);
                                if (last) {
                                    String msg = buffer.toString();
                                    buffer.setLength(0);
                                    System.out.println("[REALTIME-TEST] Incoming message from SpringBoot: " + msg);
                                    if (msg.contains("AI_TASK_RESULT") || msg.contains("AI_HOST_RESOLVE_RESP") || msg.contains("AI_REMEDIATE_RESP")) {
                                        responseFuture.complete(msg);
                                    }
                                }
                                ws.request(1);
                                return null;
                            }

                            @Override
                            public void onError(WebSocket ws, Throwable error) {
                                System.err.println("[REALTIME-TEST] WebSocket Error on " + wsUrl + ": " + error.getMessage());
                                responseFuture.completeExceptionally(error);
                            }

                            @Override
                            public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                                System.out.println("[REALTIME-TEST] Connection closed on " + wsUrl + " (status: " + statusCode + ", reason: " + reason + ")");
                                return null;
                            }
                        })
                        .get(5, TimeUnit.SECONDS);

                testExecuted = true;
                String taskId = "realtime_task_" + System.currentTimeMillis();
                String jsonPayload = String.format(
                        "{\"event-type\":\"AI_EXECUTE_TASK\",\"taskId\":\"%s\",\"conversationId\":null,\"prompt\":\"You are an AI Server Infrastructure & Target Resolution Agent. User Input Query: \\\"find me the latest resume inside document folder.\\\" Server Inventory: []\",\"filesPath\":[]}",
                        taskId
                );

                System.out.println("[REALTIME-TEST] Sending AI_EXECUTE_TASK payload to SpringBoot...");
                System.out.println(" Payload: " + jsonPayload);
                webSocket.sendText(jsonPayload, true);

                System.out.println("[REALTIME-TEST] Awaiting response from SpringBoot (timeout 30s)...");
                String response = responseFuture.get(30, TimeUnit.SECONDS);
                System.out.println("==================================================");
                System.out.println(" REALTIME TEST RESULT:");
                System.out.println(" " + response);
                System.out.println("==================================================");
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Test Finished").get();
                break;

            } catch (Exception e) {
                System.err.println("[REALTIME-TEST] Could not test port " + port + ": " + e.getMessage());
            }
        }

        if (!testExecuted) {
            System.err.println("[REALTIME-TEST] FAILED: Could not connect to SpringBoot on ports 9011, 8089, or 8080.");
        }
    }
}

```

---
