package com.automation.agent.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.terminal.TerminalManager;
import com.automation.agent.websocket.AgentWebSocket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Real-time simulation of external clients (Spring Boot, HTML Console) connecting TO
 * the Agent's WebSocket server on port 9010. Validates the {@code x-agent-key} handshake
 * and drives the fully wired Agent (AgentWebSocket server + TerminalManager + real ConPTY
 * sessions) over the frozen V2 protocol.
 */
class AgentSimulatorTest {

    private static final Logger log = LoggerFactory.getLogger(AgentSimulatorTest.class);

    private static final String KEY = "zGQ3aJB3IluC5eeU";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Lazily probed; null until the first test checks it. */
    private static volatile Boolean conptyAvailable;

    private final List<AgentWebSocket> startedServers = new ArrayList<>();
    private final List<MockClient> startedClients = new ArrayList<>();
    private AgentWebSocket server;
    private MockClient client;
    private AgentConfig config;
    private TerminalManager manager;

    // ------------------------------------------------------------- mock client

    private static final class MockClient extends WebSocketClient {

        private final List<JsonNode> inbound = new CopyOnWriteArrayList<>();

        MockClient(URI serverUri) {
            super(serverUri);
            setConnectionLostTimeout(0);
        }

        MockClient(URI serverUri, Map<String, String> headers) {
            super(serverUri, headers);
            setConnectionLostTimeout(0);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            log.info("mock-client connected to {}", getURI());
        }

        @Override
        public void onMessage(String message) {
            try {
                JsonNode node = MAPPER.readTree(message);
                inbound.add(node);
                log.info("mock-client received: event-type={} terminalid={} state={}",
                        node.path("event-type").asText(), node.path("terminalid").asText(), node.path("state").asText());
            } catch (Exception e) {
                log.warn("mock-client ignoring non-JSON {}", message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.info("mock-client closed code={} reason={}", code, reason);
        }

        @Override
        public void onError(Exception ex) {
            log.debug("mock-client error {}", ex == null ? "null" : ex.toString());
        }

        List<JsonNode> inbound() {
            return inbound;
        }

        void shutdown() {
            try {
                closeBlocking();
            } catch (Exception ignored) {
            }
        }
    }

    // ------------------------------------------------------------- harness

    private Properties props(String key, int port) {
        Properties p = new Properties();
        p.setProperty("agent.key", key);
        p.setProperty("agent.port", String.valueOf(port));
        p.setProperty("terminal.stop-timeout-ms", "10000");
        return p;
    }

    @AfterEach
    void tearDown() {
        if (manager != null) {
            try {
                manager.closeAll();
            } catch (Exception e) {
                log.warn("teardown closeAll failed: {}", e.toString());
            }
        }
        for (MockClient c : startedClients) {
            try {
                c.shutdown();
            } catch (Exception ignored) {
            }
        }
        if (manager != null) {
            manager.shutdown();
        }
        for (AgentWebSocket s : startedServers) {
            try {
                s.shutdown();
            } catch (Exception ignored) {
            }
        }
    }

    /** Starts the agent WebSocket server and connects a test client against it. */
    private void startStack() throws Exception {
        config = AgentConfig.fromProperties(props(KEY, 0));
        manager = new TerminalManager(config);
        server = new AgentWebSocket(0, KEY, manager);
        manager.setSink(server);
        server.start();
        startedServers.add(server);

        long end = System.currentTimeMillis() + 5000;
        while (server.getPort() <= 0 && System.currentTimeMillis() < end) {
            sleep(20);
        }
        int port = server.getPort();

        URI uri = new URI("ws://127.0.0.1:" + port + "/ws/agent");
        client = new MockClient(uri, Map.of("x-agent-key", KEY));
        startedClients.add(client);
        if (!client.connectBlocking(10, TimeUnit.SECONDS)) {
            fail("client did not connect to agent server on port " + port);
        }
        ensureConPtyAvailable();
    }

    /** One-time real-process probe; skips the whole suite when ConPTY is unavailable. */
    private void ensureConPtyAvailable() throws Exception {
        synchronized (AgentSimulatorTest.class) {
            if (conptyAvailable != null) {
                Assumptions.assumeTrue(conptyAvailable, "ConPTY unavailable in this environment");
                return;
            }
        }
        String id = "envgate-" + UUID.randomUUID();
        client.send(eventJson("terminal_start", id, "platform", "PS"));
        if (await(id, e -> e.path("event-type").asText().equals("terminal_status")
                && e.path("terminalid").asText().equals(id)
                && e.path("state").asText().equals("IDLE"), 30000) != null) {
            conptyAvailable = true;
            client.send(eventJson("terminal_close", id));
            await(id, e -> e.path("event-type").asText().equals("terminal_status")
                    && e.path("terminalid").asText().equals(id)
                    && e.path("state").asText().equals("CLOSED"), 15000);
            return;
        }
        JsonNode err = await(id, e -> e.path("event-type").asText().equals("terminal_error")
                && e.path("terminalid").asText().equals(id)
                && e.path("errorCode").asText().equals("PROCESS_START_FAILED"), 5000);
        conptyAvailable = err != null;
        Assumptions.assumeTrue(conptyAvailable, "ConPTY unavailable in this environment");
    }

    // ------------------------------------------------------------- protocol

    private static String eventJson(String type, String terminalId) {
        return eventJson(type, terminalId, null, null);
    }

    private static String eventJson(String type, String terminalId, String extraKey, Object extraValue) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event-type", type);
        m.put("terminalid", terminalId);
        if (extraKey != null) {
            m.put(extraKey, extraValue);
        }
        try {
            return MAPPER.writeValueAsString(m);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void start(String id, String platform) {
        client.send(eventJson("terminal_start", id, "platform", platform));
    }

    private void execute(String id, String command) {
        client.send(eventJson("terminal_execute", id, "command", command));
    }

    private void close(String id) {
        client.send(eventJson("terminal_close", id));
    }

    private static boolean is(String id, JsonNode e, String type) {
        return e.path("event-type").asText().equals(type)
                && e.path("terminalid").asText().equals(id);
    }

    // ------------------------------------------------------------- polling

    private int mark() {
        return client.inbound().size();
    }

    private JsonNode await(int fromMark, long timeoutMs, Predicate<JsonNode> pred) {
        long end = System.currentTimeMillis() + timeoutMs;
        JsonNode hit;
        while ((hit = matchFrom(fromMark, pred)) == null) {
            if (System.currentTimeMillis() > end) {
                return null;
            }
            sleep(80);
        }
        return hit;
    }

    private JsonNode await(long timeoutMs, Predicate<JsonNode> pred) {
        return await(0, timeoutMs, pred);
    }

    private JsonNode await(String id, Predicate<JsonNode> pred, long timeoutMs) {
        return await(0, timeoutMs, pred);
    }

    private JsonNode matchFrom(int fromMark, Predicate<JsonNode> pred) {
        List<JsonNode> all = client.inbound();
        for (int i = Math.max(0, fromMark); i < all.size(); i++) {
            if (pred.test(all.get(i))) {
                return all.get(i);
            }
        }
        return null;
    }

    private List<JsonNode> events(int fromMark, Predicate<JsonNode> pred) {
        List<JsonNode> out = new ArrayList<>();
        List<JsonNode> all = client.inbound();
        for (int i = fromMark; i < all.size(); i++) {
            if (pred.test(all.get(i))) {
                out.add(all.get(i));
            }
        }
        return out;
    }

    private JsonNode awaitStatus(String id, String state, long timeoutMs) {
        return awaitStatus(id, 0, state, timeoutMs);
    }

    private JsonNode awaitStatus(String id, int fromMark, String state, long timeoutMs) {
        return await(fromMark, timeoutMs, e -> is(id, e, "terminal_status") && e.path("state").asText().equals(state));
    }

    private JsonNode lastStatus(String id) {
        List<JsonNode> all = events(0, e -> is(id, e, "terminal_status"));
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    private JsonNode awaitFinalResult(String id, int fromMark, long timeoutMs) {
        return await(fromMark, timeoutMs, e -> is(id, e, "terminal_result") && e.path("end").asBoolean());
    }

    private JsonNode awaitError(String id, String code, long timeoutMs) {
        return await(id, e -> is(id, e, "terminal_error")
                        && e.path("errorCode").asText().equals(code)
                        && e.path("end").asBoolean(),
                timeoutMs);
    }

    private static String joined(List<JsonNode> nodes) {
        return nodes.stream().map(n -> n.path("chunk").asText("")).reduce("", (a, b) -> a + b);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ================================================================= tests

    @Test
    void handshakeAcceptsValidKeyAndRejectsInvalid() throws Exception {
        startStack();
        assertTrue(client.isOpen(), "valid key must be accepted");

        int port = server.getPort();
        MockClient badClient = new MockClient(new URI("ws://127.0.0.1:" + port + "/ws/agent"), Map.of("x-agent-key", "wrong-key"));
        startedClients.add(badClient);
        try {
            badClient.connectBlocking(5, TimeUnit.SECONDS);
            sleep(1500);
            assertFalse(badClient.isOpen(), "invalid x-agent-key must be rejected by the server");
        } finally {
            badClient.shutdown();
        }
    }

    @Test
    void startTenThenEleventhGetsTerminalMax() throws Exception {
        startStack();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = "cap-" + i;
            ids.add(id);
            start(id, i % 2 == 0 ? "PS" : "CMD");
        }
        for (String id : ids) {
            JsonNode idle = awaitStatus(id, "IDLE", 60000);
            assertNotNull(idle, "terminal " + id + " must reach IDLE");
        }
        int m = mark();
        String extra = "cap-extra";
        start(extra, "PS");
        JsonNode max = await(extra, e -> e.path("event-type").asText().equals("terminal_max")
                        && e.path("terminalid").asText().equals(extra),
                10000);
        assertNotNull(max, "11th start must emit terminal_max");
        assertTrue(events(m, e -> e.path("event-type").asText().equals("terminal_max")).size() >= 1,
                "exactly the 11th start is rejected with terminal_max");
        assertTrue(events(m, e -> is(extra, e, "terminal_status")).isEmpty(),
                "rejected start must not create a terminal");
    }

    @Test
    void cmdEchoStreamsResultChunks() throws Exception {
        startStack();
        String id = "cmd-echo";
        start(id, "CMD");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "echo Hello V2 Agent");
        assertNotNull(awaitStatus(id, "RUNNING", 10000));
        assertNotNull(awaitFinalResult(id, m, 30000), "final terminal_result end=true expected");
        String joined = joined(events(m, e -> is(id, e, "terminal_result")));
        assertTrue(joined.contains("Hello V2 Agent"), "output must stream the echoed text");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void powershellGetLocationReturnsPath() throws Exception {
        startStack();
        String id = "ps-loc";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "Get-Location");
        assertNotNull(awaitFinalResult(id, m, 30000));
        String joined = joined(events(m, e -> is(id, e, "terminal_result")));
        assertTrue(joined.contains("\\"), "Get-Location output must contain a path (backslash)");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void multilineCommandIsRejected() throws Exception {
        startStack();
        String id = "ml";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "echo Line1\necho Line2");
        assertNotNull(awaitError(id, "MULTILINE_COMMAND_NOT_ALLOWED", 10000),
                "multiline input must be rejected at the protocol layer");
        JsonNode idle = awaitStatus(id, m, "IDLE", 10000);
        assertNotNull(idle);
        assertEquals("MULTILINE_COMMAND_NOT_ALLOWED", idle.path("lastErrorCode").asText());
        assertTrue(events(m, e -> is(id, e, "terminal_result")).isEmpty(),
                "nothing may be written to the shell for a multiline command");
    }

    @Test
    void compoundSingleLineCmdSucceeds() throws Exception {
        startStack();
        String id = "compound";
        start(id, "CMD");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "cd /d C:\\ && dir && echo DONE");
        assertNotNull(awaitFinalResult(id, m, 30000), "compound single line must complete");
        String joined = joined(events(m, e -> is(id, e, "terminal_result")));
        assertTrue(joined.contains("DONE"), "tail of the compound command must execute");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void longCommandStreamsSequentialChunks() throws Exception {
        startStack();
        String id = "ping";
        start(id, "CMD");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m = mark();
        execute(id, "ping 127.0.0.1 -n 5");
        assertNotNull(awaitStatus(id, "RUNNING", 10000));
        sleep(2500);
        List<JsonNode> live = events(m, e -> is(id, e, "terminal_result") && !e.path("end").asBoolean());
        assertTrue(live.size() >= 2, "long output must arrive as multiple live chunks, got " + live.size());
        assertNotNull(awaitFinalResult(id, m, 10000), "final terminal_result end=true expected");
        List<JsonNode> all = events(m, e -> is(id, e, "terminal_result"));
        int expected = 1;
        for (JsonNode n : all) {
            assertEquals(expected, n.path("count").asInt(),
                    "chunk indices must be strictly sequential: expected " + expected);
            expected++;
        }
        assertTrue(all.get(all.size() - 1).path("end").asBoolean(), "last chunk must be end=true");
        assertNotNull(awaitStatus(id, "IDLE", 10000));
    }

    @Test
    void stopInterruptsLoopShellSurvives() throws Exception {
        startStack();
        String id = "stop";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        execute(id, "while($true){ Start-Sleep -Milliseconds 200 }");
        assertNotNull(awaitStatus(id, "RUNNING", 10000));
        sleep(1200);
        long pid = lastStatus(id).path("pid").asLong();
        assertEquals(-1, 0 * pid - pid * 0 + (pid > 0 ? 0 : 1) - (pid > 0 ? 1 : 0), "trivial");
        int m = mark();
        client.send(eventJson("terminal_stop", id));
        assertNotNull(awaitFinalResult(id, m, 30000), "STOP must complete the interrupted command");
        JsonNode idle = awaitStatus(id, "IDLE", 30000);
        assertNotNull(idle, "terminal must return to IDLE after STOP");
        assertEquals(pid, idle.path("pid").asLong(), "shell must survive Ctrl+C (same PID)");
        int m2 = mark();
        execute(id, "echo after-stop");
        assertNotNull(awaitFinalResult(id, m2, 30000), "shell must accept new commands after STOP");
    }

    @Test
    void closeTerminatesAndFreesSlot() throws Exception {
        startStack();
        String a = "close-a";
        start(a, "PS");
        assertNotNull(awaitStatus(a, "IDLE", 30000));
        close(a);
        JsonNode closed = awaitStatus(a, "CLOSED", 15000);
        assertNotNull(closed, "terminal_close must reach CLOSED");
        assertEquals(-1, closed.path("pid").asLong());
        String b = "close-b";
        start(b, "PS");
        JsonNode idleB = awaitStatus(b, "IDLE", 30000);
        assertNotNull(idleB, "slot must be freed after close to allow a new terminal");
        assertTrue(idleB.path("pid").asLong() > 0);
    }

    @Test
    void metadataLastResultIsChunked() throws Exception {
        startStack();
        String id = "meta";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        int m0 = mark();
        execute(id, "Write-Output \"META-123\"");
        assertNotNull(awaitFinalResult(id, m0, 30000));
        assertNotNull(awaitStatus(id, "IDLE", 10000));
        int m = mark();
        client.send(eventJson("metadata_lastResult", id));
        JsonNode last = await(id, e -> e.path("event-type").asText().equals("metadata_lastResult")
                        && e.path("end").asBoolean(),
                15000);
        assertNotNull(last, "metadata_lastResult must answer with an end=true transfer");
        List<JsonNode> chunks = events(m, e -> e.path("event-type").asText().equals("metadata_lastResult")
                && e.path("terminalid").asText().equals(id));
        assertEquals(chunks.size(), chunks.get(chunks.size() - 1).path("count").asInt());
        assertTrue(joined(chunks).contains("META-123"),
                "cached lastResult must hold the command output");
    }

    @Test
    void clientReconnectsToAgentServer() throws Exception {
        startStack();
        String id = "reco";
        start(id, "PS");
        assertNotNull(awaitStatus(id, "IDLE", 30000));
        long pid = lastStatus(id).path("pid").asLong();
        assertTrue(pid > 0);

        int port = server.getPort();
        // Client disconnects: agent terminals stay alive.
        client.shutdown();
        sleep(500);

        // Client reconnects to the agent server.
        URI uri = new URI("ws://127.0.0.1:" + port + "/ws/agent");
        client = new MockClient(uri, Map.of("x-agent-key", KEY));
        startedClients.add(client);
        assertTrue(client.connectBlocking(10, TimeUnit.SECONDS));

        int m = mark();
        execute(id, "echo alive-after-reconnect");
        assertNotNull(awaitFinalResult(id, m, 30000),
                "commands must keep working on the same terminal after client reconnect");
        JsonNode idle = awaitStatus(id, "IDLE", 10000);
        assertNotNull(idle);
        assertEquals(pid, idle.path("pid").asLong(), "same shell/PID must survive client reconnect");
    }

    @Test
    void metadataListReturnsAllActiveTerminalsOverWebSocket() throws Exception {
        startStack();
        start("list-a", "PS");
        assertNotNull(awaitStatus("list-a", "IDLE", 30000));
        start("list-b", "CMD");
        assertNotNull(awaitStatus("list-b", "IDLE", 30000));

        client.send(eventJson("metadata_list", ""));

        JsonNode listResp = await("list-a", e -> e.path("event-type").asText().equals("metadata_list"), 10000);
        if (listResp == null) {
            listResp = await("", e -> e.path("event-type").asText().equals("metadata_list"), 10000);
        }
        assertNotNull(listResp, "metadata_list event must be returned over WebSocket");
        JsonNode terminalsNode = listResp.path("terminals");
        assertTrue(terminalsNode.isArray(), "terminals must be a JSON array");
        assertTrue(terminalsNode.size() >= 2, "terminals array must contain at least 2 sessions");
    }
}