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
