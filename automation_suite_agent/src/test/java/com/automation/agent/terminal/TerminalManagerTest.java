package com.automation.agent.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.protocol.TerminalProtocol.MetadataErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.MetadataResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalStatusEvent;

/**
 * Real {@link TerminalManager} lifecycle/concurrency tests driven through the
 * public protocol surface with deterministic {@link FakeProcessSession}s —
 * no OS processes, no real shells.
 */
class TerminalManagerTest {

    private final List<TerminalManager> managers = new CopyOnWriteArrayList<>();
    private final List<FakeProcessSession> fakes = new CopyOnWriteArrayList<>();

    @AfterEach
    void tearDown() {
        for (FakeProcessSession f : fakes) {
            try {
                f.destroy();
            } catch (Exception ignored) {
            }
        }
        for (TerminalManager m : managers) {
            m.shutdown();
        }
    }

    // ------------------------------------------------------------ harness

    private Properties baseProps() {
        Properties p = new Properties();
        p.setProperty("agent.key", "test-key");
        p.setProperty("agent.websocket.url", "ws://localhost:1/ws/agent");
        p.setProperty("terminal.stop-timeout-ms", "10000");
        return p;
    }

    private TerminalManager manager(TestEventSink sink, TerminalManager.SessionFactory factory) {
        TerminalManager m = new TerminalManager(AgentConfig.fromProperties(baseProps()), factory);
        m.setSink(sink);
        managers.add(m);
        return m;
    }

    private TerminalManager.SessionFactory collectingFactory() {
        return (platform, initialPath) -> {
            FakeProcessSession f = new FakeProcessSession(platform);
            fakes.add(f);
            return f;
        };
    }

    private List<TerminalStatusEvent> statuses(TestEventSink sink, String id) {
        return sink.ofType(TerminalStatusEvent.class).stream()
                .filter(e -> id.equals(e.terminalId))
                .toList();
    }

    private TerminalStatusEvent lastStatus(TestEventSink sink, String id) {
        List<TerminalStatusEvent> all = statuses(sink, id);
        assertFalse(all.isEmpty(), "expected at least one status for " + id);
        return all.get(all.size() - 1);
    }

    private List<TerminalErrorEvent> errors(TestEventSink sink, String id) {
        return sink.ofType(TerminalErrorEvent.class).stream()
                .filter(e -> id.equals(e.terminalId))
                .toList();
    }

    private boolean hasError(TestEventSink sink, String id, String code) {
        return errors(sink, id).stream().anyMatch(e -> code.equals(e.errorCode) && e.end);
    }

    private List<TerminalResultEvent> results(TestEventSink sink, String id) {
        return sink.ofType(TerminalResultEvent.class).stream()
                .filter(e -> id.equals(e.terminalId))
                .toList();
    }

    private boolean hasFinalResult(TestEventSink sink, String id) {
        return results(sink, id).stream().anyMatch(e -> e.end);
    }

    private static void await(String what, java.util.function.BooleanSupplier cond) throws Exception {
        long end = System.currentTimeMillis() + 5000;
        while (!cond.getAsBoolean()) {
            if (System.currentTimeMillis() > end) {
                throw new AssertionError("timed out waiting for: " + what);
            }
            Thread.sleep(50);
        }
    }

    // ------------------------------------------------------------ capacity

    @Test
    void tenStartsThenEleventhIsTerminalMax() {
        TestEventSink sink = new TestEventSink();
        List<FakeProcessSession> created = new CopyOnWriteArrayList<>();
        TerminalManager m = manager(sink, (platform, initialPath) -> {
            FakeProcessSession f = new FakeProcessSession(platform);
            created.add(f);
            fakes.add(f);
            return f;
        });
        for (int i = 0; i < 10; i++) {
            m.start("t-" + i, "PS");
        }
        assertEquals(10, created.size());
        assertEquals(10, m.activeCount());
        for (int i = 0; i < 10; i++) {
            assertEquals("IDLE", lastStatus(sink, "t-" + i).state);
            assertTrue(lastStatus(sink, "t-" + i).pid > 0);
        }
        m.start("t-11", "PS");
        assertEquals(10, created.size(), "11th start must not create a process");
        assertEquals(10, m.activeCount());
        List<Map<String, Object>> max = sink.withWireType("terminal_max");
        assertEquals(1, max.size());
        assertEquals("t-11", max.get(0).get("terminalid"));
    }

    @Test
    void duplicateStartKeepsOriginalPid() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "CMD");
        long pid = lastStatus(sink, "A").pid;
        m.start("A", "CMD");
        assertTrue(hasError(sink, "A", "TERMINAL_ALREADY_RUNNING"));
        assertEquals(pid, lastStatus(sink, "A").pid, "original shell must be untouched");
        assertEquals(1, m.activeCount());
    }

    @Test
    void failedCreationReleasesSlot() {
        TestEventSink sink = new TestEventSink();
        AtomicBoolean fail = new AtomicBoolean(true);
        TerminalManager m = manager(sink, (platform, initialPath) -> {
            if (fail.get()) {
                throw new IOException("boom");
            }
            FakeProcessSession f = new FakeProcessSession(platform);
            fakes.add(f);
            return f;
        });
        m.start("A", "PS");
        assertTrue(hasError(sink, "A", "PROCESS_START_FAILED"));
        assertEquals(0, m.activeCount(), "failed creation must release the reservation");
        fail.set(false);
        m.start("A", "PS");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals(1, m.activeCount());
    }

    // ------------------------------------------------------------ execute

    @Test
    void executeStreamsAndCompletesAtMarker() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertTrue(hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertNotNull(lastStatus(sink, "A").lastOutputTimestamp);

        m.metadataLastResult("A");
        List<MetadataResultEvent> chunks = sink.ofType(MetadataResultEvent.class);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.get(chunks.size() - 1).end);
        assertEquals(chunks.size(), chunks.get(chunks.size() - 1).count);
        String joined = chunks.stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertTrue(joined.contains("hi"), "cached lastResult must hold the output");
        // Live stream types must not be reused for metadata transfer.
        assertTrue(sink.withWireType("metadata_lastResult").size() >= 1);
    }

    @Test
    void secondExecuteWhileRunningIsBusy() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.execute("A", "echo other");
        assertTrue(hasError(sink, "A", "TERMINAL_BUSY"));
        assertEquals("RUNNING", lastStatus(sink, "A").state, "terminal stays RUNNING");
    }

    @Test
    void multilineNeverReachesShell() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        FakeProcessSession f = fakes.get(0);
        m.execute("A", "echo a\necho b");
        assertTrue(hasError(sink, "A", "MULTILINE_COMMAND_NOT_ALLOWED"));
        assertTrue(f.writtenLines().isEmpty(), "multiline input must never be written");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals("MULTILINE_COMMAND_NOT_ALLOWED", lastStatus(sink, "A").lastErrorCode);
        assertNotNull(lastStatus(sink, "A").lastError, "agent error must populate lastError");
    }

    @Test
    void compoundSingleLineIsAccepted() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "Get-Service | Where-Object {$_.Status -eq \"Running\"}");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
        assertTrue(errors(sink, "A").isEmpty());
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        // Marker is a second shell input line; the user command stays one line.
        assertEquals(2, fakes.get(0).writtenLines().size());
    }

    // ------------------------------------------------------------ stop

    @Test
    void stopKeepsShellAliveUntilBoundary() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.stop("A");
        assertEquals("RUNNING", lastStatus(sink, "A").state, "STOP must not complete early");
        assertFalse(hasFinalResult(sink, "A"), "no completion before the boundary");
        assertEquals(1, fakes.get(0).writtenRaw().size(), "Ctrl+C byte delivered once");
        assertEquals((byte) 0x03, fakes.get(0).writtenRaw().get(0)[0]);

        // Shell reaches the queued boundary afterwards: deterministic completion.
        m.handleStdout("A", "interrupted output\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertTrue(hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertTrue(fakes.get(0).isAlive(), "shell remains alive after STOP");

        // Terminal is usable again.
        m.execute("A", "echo after-stop");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
    }

    @Test
    void stopTimeoutForcesCompletion() throws Exception {
        Properties p = baseProps();
        p.setProperty("terminal.stop-timeout-ms", "300");
        TestEventSink sink = new TestEventSink();
        TerminalManager m = new TerminalManager(AgentConfig.fromProperties(p), collectingFactory());
        m.setSink(sink);
        managers.add(m);
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.handleStdout("A", "partial\r\n");
        m.stop("A");
        await("forced completion", () -> hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
    }

    // ------------------------------------------------------------ close

    @Test
    void closeTerminatesAndReleasesSlot() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo x");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        m.close("A");
        List<TerminalStatusEvent> st = statuses(sink, "A");
        assertTrue(st.stream().anyMatch(e -> "CLOSING".equals(e.state)));
        assertEquals("CLOSED", st.get(st.size() - 1).state);
        assertEquals(-1, st.get(st.size() - 1).pid);
        assertTrue(fakes.get(0).destroyCalled(), "shell must be terminated");
        assertEquals(0, m.activeCount(), "slot released on CLOSED");

        m.execute("A", "echo late");
        assertTrue(hasError(sink, "A", "TERMINAL_CLOSED"));
    }

    @Test
    void closeFailureRetainsSessionAndSlotForRetry() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        fakes.get(0).failFirstDestroy(true);
        m.close("A");
        assertTrue(hasError(sink, "A", "PROCESS_TERMINATION_FAILED"));
        assertEquals("CLOSING", lastStatus(sink, "A").state);
        assertEquals(1, m.activeCount(), "CLOSING retains the slot");

        m.close("A"); // retry must actually re-attempt termination
        assertEquals("CLOSED", lastStatus(sink, "A").state);
        assertEquals(0, m.activeCount());
    }

    @Test
    void restartKeepsIdAndMetadataWithNewPid() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo keep");
        m.handleStdout("A", "keep\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        long pid1 = lastStatus(sink, "A").pid;
        m.close("A");
        assertEquals("CLOSED", lastStatus(sink, "A").state);

        m.start("A", "PS");
        TerminalStatusEvent restarted = lastStatus(sink, "A");
        assertEquals("IDLE", restarted.state);
        assertTrue(restarted.pid > 0 && restarted.pid != pid1, "restart gets a new shell/PID");
        assertEquals("echo keep", restarted.lastCommand, "metadata survives restart");
        assertNotNull(restarted.lastResult);
        assertTrue(restarted.lastResult.contains("keep"));
    }

    // ------------------------------------------------------------ metadata + config

    @Test
    void statusPayloadCarriesBoundedCache() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        TerminalStatusEvent idle = lastStatus(sink, "A");
        assertEquals("IDLE", idle.state);
        assertNotNull(idle.lastResult);
        assertTrue(idle.lastResult.contains("hi"));
    }

    @Test
    void agentErrorIsCachedForMetadataError() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.execute("A", "echo other"); // TERMINAL_BUSY
        m.metadataLastError("A");
        List<MetadataErrorEvent> chunks = sink.ofType(MetadataErrorEvent.class);
        assertFalse(chunks.isEmpty());
        assertTrue(chunks.get(chunks.size() - 1).end);
        assertTrue(sink.withWireType("metadata_lastError").size() >= 1);
        String joined = chunks.stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertTrue(joined.contains("currently executing"));
    }

    @Test
    void maxCountIsAHardInvariant() {
        Properties p = baseProps();
        p.setProperty("terminal.max-count", "50");
        assertThrows(IllegalStateException.class, () -> AgentConfig.fromProperties(p));
    }

    // ------------------------------------------------------------ concurrency

    @Test
    void elevenConcurrentStartsAdmitExactlyTen() throws Exception {
        TestEventSink sink = new TestEventSink();
        List<FakeProcessSession> created = new CopyOnWriteArrayList<>();
        TerminalManager m = manager(sink, (platform, initialPath) -> {
            FakeProcessSession f = new FakeProcessSession(platform);
            created.add(f);
            fakes.add(f);
            return f;
        });
        int contenders = 11;
        ExecutorService pool = Executors.newFixedThreadPool(contenders);
        CountDownLatch go = new CountDownLatch(1);
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        for (int i = 0; i < contenders; i++) {
            final String id = "c-" + i;
            pool.submit(() -> {
                try {
                    go.await();
                    m.start(id, "PS");
                } catch (Throwable t) {
                    failures.add(t);
                }
                return null;
            });
        }
        go.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(failures.isEmpty(), "no worker may throw: " + failures);
        assertEquals(10, created.size(), "exactly 10 processes may exist");
        assertEquals(10, m.activeCount());
        assertEquals(1, sink.withWireType("terminal_max").size(), "exactly one terminal_max");
        // 10 terminals IDLE, 1 rejected without state.
        long idle = sink.ofType(TerminalStatusEvent.class).stream()
                .filter(e -> e.terminalId.startsWith("c-") && "IDLE".equals(e.state))
                .map(e -> e.terminalId)
                .distinct()
                .count();
        assertEquals(10, idle);
    }

    @Test
    void concurrentExecuteStopCloseStaysCoherent() throws Exception {
        Properties p = baseProps();
        p.setProperty("terminal.stop-timeout-ms", "500");
        TestEventSink sink = new TestEventSink();
        TerminalManager m = new TerminalManager(AgentConfig.fromProperties(p), collectingFactory());
        m.setSink(sink);
        managers.add(m);
        m.start("A", "PS");
        m.execute("A", "sleep 60");

        ExecutorService pool = Executors.newFixedThreadPool(3);
        CountDownLatch go = new CountDownLatch(1);
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        pool.submit(() -> { try { go.await(); m.execute("A", "echo x"); } catch (Throwable t) { failures.add(t); } return null; });
        pool.submit(() -> { try { go.await(); m.stop("A"); } catch (Throwable t) { failures.add(t); } return null; });
        pool.submit(() -> { try { go.await(); m.close("A"); } catch (Throwable t) { failures.add(t); } return null; });
        go.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(failures.isEmpty(), "lifecycle ops must never throw: " + failures);

        await("settle", () -> {
            String s = lastStatus(sink, "A").state;
            return "IDLE".equals(s) || "CLOSED".equals(s);
        });
        String fin = lastStatus(sink, "A").state;
        if ("CLOSED".equals(fin)) {
            assertEquals(0, m.activeCount());
        } else {
            assertEquals(1, m.activeCount());
        }
    }

    @Test
    void unknownTerminalAndBadPlatform() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.execute("ghost", "echo hi");
        assertEquals(1, sink.withWireType("terminal_error").size());
        Map<String, Object> e = sink.withWireType("terminal_error").get(0);
        assertEquals("TERMINAL_NOT_FOUND", e.get("errorCode"));

        m.start("B", "BOGUS");
        assertEquals("INVALID_PLATFORM",
                sink.withWireType("terminal_error").get(1).get("errorCode"));
        assertEquals(0, m.activeCount(), "invalid platform reserves nothing");
    }

    @Test
    void closeDuringRunningCommand() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "sleep 60");
        m.close("A");
        assertEquals("CLOSED", lastStatus(sink, "A").state);
        assertEquals(0, m.activeCount());
    }

    // ------------------------------------------------- framing (VT-safe)

    @Test
    void commandEchoIsFramingNotResult() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        // ConPTY echoes the typed line first; it must never surface as result.
        m.handleStdout("A", "echo hi\r\n");
        assertTrue(results(sink, "A").isEmpty(), "echo must not be streamed");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        String live = results(sink, "A").stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertEquals("hi\r\n", live);
        assertEquals("hi\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void promptPrefixedEchoIsConsumed() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        // Prompt chrome may share the echo's line; suffix match still consumes it.
        m.handleStdout("A", "PS C:\\Temp> echo hi\r\n");
        assertTrue(results(sink, "A").isEmpty(), "prompt+echo must not be streamed");
        m.handleStdout("A", "hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
    }

    @Test
    void ansiWrappedMarkerCompletes() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        // Real ConPTY output arrives wrapped in VT sequences, like the frozen evidence:
        // ESC[?25l ESC[2J ESC[m ... ESC[H ESC]0;... ESC[?25h
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "[?25lhi\r\n");
        m.handleStdout("A", "[2J[m\n__AGENT_DONE_1__[?25h\r\n");
        assertTrue(hasFinalResult(sink, "A"), "VT-wrapped marker must complete");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertTrue(lastStatus(sink, "A").lastResult.contains("hi"));
    }

    @Test
    void markerSplitAcrossChunksCompletes() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "hi\r\n__AGENT_DO");
        assertFalse(hasFinalResult(sink, "A"), "partial marker line must wait");
        assertEquals("RUNNING", lastStatus(sink, "A").state);
        m.handleStdout("A", "NE_1__\r\n");
        assertTrue(hasFinalResult(sink, "A"), "split marker must complete on the full line");
        assertEquals("IDLE", lastStatus(sink, "A").state);
    }

    @Test
    void outputIdenticalToCommandIsPreserved() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        // Only the FIRST matching line is framing; a repeated identical output line stays.
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals("echo hi\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void crlfDoublingProducesNoPhantomBlank() {
        // Real consoles emit \r\r\n; it must classify as one line, not line+blank.
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\r\n");
        m.handleStdout("A", "hi\r\r\n");
        m.handleStdout("A", "\n__AGENT_DONE_1__\n");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertEquals("hi\r\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void loneCarriageReturnIsABoundary() {
        // Prompt/progress rewrites use lone \r; a marker glued after one must complete.
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "echo hi");
        m.handleStdout("A", "echo hi\r\n");
        m.handleStdout("A", "hi\r__AGENT_DONE_1__\r\n");
        assertTrue(hasFinalResult(sink, "A"), "lone-\\r marker must complete");
        assertEquals("IDLE", lastStatus(sink, "A").state);
        assertTrue(lastStatus(sink, "A").lastResult.contains("hi"));
    }

    @Test
    void realisticConptyHelloFlow() {
        // End-to-end framing exactly as a real ConPTY delivers it: prompt-prefixed
        // echo, VT-wrapped output, marker-command echo, VT-wrapped marker, trailing
        // prompt without newline. Expected: one HELLO chunk, empty final, IDLE.
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.execute("A", "Write-Output \"HELLO\"");
        m.handleStdout("A", "PS C:\\Temp> Write-Output \"HELLO\"\r\n");
        assertTrue(results(sink, "A").isEmpty());
        m.handleStdout("A", "HELLO\r\n");
        m.handleStdout("A", " \u001b[?25lWrite-Output \"`n__AGENT_DONE_1__\"\r\n");
        assertTrue(results(sink, "A").stream().noneMatch(e -> e.chunk.contains("__AGENT_DONE_1__")),
                "marker echo must never stream");
        m.handleStdout("A", "\n__AGENT_DONE_1__\r\nPS C:\\Temp> ");
        assertTrue(hasFinalResult(sink, "A"));
        assertEquals("IDLE", lastStatus(sink, "A").state);
        String live = results(sink, "A").stream().map(e -> e.chunk).reduce("", (a, b) -> a + b);
        assertEquals("HELLO\r\n", live);
        assertEquals("HELLO\r\n", lastStatus(sink, "A").lastResult);
    }

    @Test
    void metadataListReturnsAllTerminalSnapshots() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "PS");
        m.start("B", "CMD");

        m.metadataList();

        List<com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent> listEvents =
                sink.ofType(com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent.class);
        assertFalse(listEvents.isEmpty(), "expected MetadataListResponseEvent");
        com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent resp = listEvents.get(0);
        assertNotNull(resp.terminals);
        assertEquals(2, resp.terminals.size());
        assertTrue(resp.terminals.stream().anyMatch(t -> "A".equals(t.terminalId) && "PS".equals(t.platform)));
        assertTrue(resp.terminals.stream().anyMatch(t -> "B".equals(t.terminalId) && "CMD".equals(t.platform)));
    }

    @Test
    void platformMismatchIsRejectedWhenTerminalIsActive() {
        TestEventSink sink = new TestEventSink();
        TerminalManager m = manager(sink, collectingFactory());
        m.start("A", "CMD");
        assertEquals("CMD", lastStatus(sink, "A").platform);

        // Request PowerShell for active CMD terminal -> should reject
        m.start("A", "PS");
        assertTrue(hasError(sink, "A", "INVALID_PLATFORM"), "Platform mismatch on active terminal must be rejected");
        assertEquals("CMD", lastStatus(sink, "A").platform);
    }

    @Test
    void flexiblePlatformParsingWorks() {
        assertEquals(TerminalModels.TerminalPlatform.PS, TerminalModels.TerminalPlatform.parse("PowerShell"));
        assertEquals(TerminalModels.TerminalPlatform.PS, TerminalModels.TerminalPlatform.parse("power_shell"));
        assertEquals(TerminalModels.TerminalPlatform.CMD, TerminalModels.TerminalPlatform.parse("Command Prompt"));
        assertEquals(TerminalModels.TerminalPlatform.CMD, TerminalModels.TerminalPlatform.parse("cmd.exe"));
    }
}
