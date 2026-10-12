package com.automation.agent.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.process.ProcessSession;
import com.automation.agent.protocol.TerminalProtocol.MetadataErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.MetadataResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalErrorCode;
import com.automation.agent.protocol.TerminalProtocol.TerminalErrorEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalEventType;
import com.automation.agent.protocol.TerminalProtocol.TerminalResultEvent;
import com.automation.agent.protocol.TerminalProtocol.TerminalStatusEvent;
import com.automation.agent.terminal.TerminalModels.TerminalContext;
import com.automation.agent.terminal.TerminalModels.TerminalMetadata;
import com.automation.agent.terminal.TerminalModels.TerminalPlatform;
import com.automation.agent.terminal.TerminalModels.TerminalState;

/**
 * Owns max-N terminals: atomic slot reservation, lookup, lifecycle
 * coordination, creation/removal. Per-terminal mutations hold that terminal's
 * lock, so Terminal A never blocks Terminal B.
 *
 * <p>Invariants (V2 master plan section 37): at most one active command per
 * terminal; start never duplicates an active terminal; execute never overlaps;
 * stop never closes the shell; close fully terminates; CLOSING retains the
 * slot; failed creation releases the reservation; multiline input never reaches
 * the shell; the agent never auto-closes.
 */
public final class TerminalManager {

    private static final Logger log = LoggerFactory.getLogger(TerminalManager.class);

    private final AgentConfig config;
    private final SessionFactory sessions;
    private final ConcurrentHashMap<String, TerminalContext> terminals = new ConcurrentHashMap<>();
    private final Semaphore slots;
    private final AtomicLong markerSeq = new AtomicLong();
    private final ScheduledExecutorService stopTimeouts;
    private volatile Consumer<Object> sink;

    /**
     * Creates OS shell sessions. Swappable in tests for deterministic fakes;
     * production uses the real ConPTY-backed {@link ProcessSession#launch}.
     */
    @FunctionalInterface
    public interface SessionFactory {
        ProcessSession start(TerminalPlatform platform, String initialPath) throws Exception;
    }

    public TerminalManager(AgentConfig config) {
        this(config, (platform, initialPath) -> ProcessSession.launch(platform, initialPath, config));
    }

    TerminalManager(AgentConfig config, SessionFactory sessions) {
        this.config = config;
        this.sessions = sessions;
        this.slots = new Semaphore(config.terminalMaxCount(), true);
        this.stopTimeouts = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "term-stop-timeout");
            t.setDaemon(true);
            return t;
        });
    }

    public void setSink(Consumer<Object> sink) {
        this.sink = sink;
    }

    /** Releases the internal STOP-timeout scheduler. Terminals are untouched. */
    public void shutdown() {
        stopTimeouts.shutdownNow();
    }

    /** Closes every live terminal (graceful JVM shutdown). Slots are released. */
    public void closeAll() {
        for (String id : terminals.keySet()) {
            try {
                close(id);
            } catch (Exception e) {
                log.warn("closeAll: error closing {}: {}", id, e.toString());
            }
        }
    }

    // ------------------------------------------------------------------ start

    public void start(String terminalId, String platformRaw) {
        start(terminalId, platformRaw, null);
    }

    public void start(String terminalId, String platformRaw, String initialPath) {
        if (terminalId == null || terminalId.isBlank()) {
            sendDetachedError("", TerminalErrorCode.INVALID_TERMINAL_ID);
            return;
        }
        TerminalPlatform platform = TerminalPlatform.parse(platformRaw);
        if (platform == null) {
            TerminalContext existing = terminals.get(terminalId);
            if (existing != null) {
                existing.lock().lock();
                try {
                    agentError(existing, TerminalErrorCode.INVALID_PLATFORM);
                    emitStatusIfChanged(existing);
                } finally {
                    existing.lock().unlock();
                }
            } else {
                sendDetachedError(terminalId, TerminalErrorCode.INVALID_PLATFORM);
            }
            return;
        }

        TerminalContext existing = terminals.get(terminalId);
        if (existing != null) {
            existing.lock().lock();
            try {
                if (existing.metadata().state() != TerminalState.CLOSED) {
                    if (existing.metadata().platform() != null && existing.metadata().platform() != platform) {
                        agentError(existing, TerminalErrorCode.INVALID_PLATFORM,
                                "Terminal is already active with platform " + existing.metadata().platform() + ". Close terminal first to switch platform.");
                        emitStatusIfChanged(existing);
                        return;
                    }
                    agentError(existing, TerminalErrorCode.TERMINAL_ALREADY_RUNNING);
                    emitStatusIfChanged(existing);
                    return;
                }
                // CLOSED -> restart with the same terminalId: needs a fresh slot.
                if (!slots.tryAcquire()) {
                    sendMax(terminalId);
                    return;
                }
                try {
                    restartLocked(existing, platform, initialPath);
                } catch (Exception e) {
                    slots.release();
                    agentError(existing, TerminalErrorCode.PROCESS_START_FAILED, e.getMessage());
                    emitStatusIfChanged(existing);
                }
            } finally {
                existing.lock().unlock();
            }
            return;
        }

        // New terminal: reserve atomically BEFORE creating anything.
        TerminalContext fresh = new TerminalContext(terminalId);
        TerminalContext raced = terminals.putIfAbsent(terminalId, fresh);
        if (raced != null) {
            // Lost the race — delegate to the existing-context path.
            start(terminalId, platformRaw, initialPath);
            return;
        }
        if (!slots.tryAcquire()) {
            terminals.remove(terminalId, fresh);
            sendMax(terminalId);
            return;
        }
        fresh.lock().lock();
        try {
            try {
                restartLocked(fresh, platform, initialPath);
            } catch (Exception e) {
                terminals.remove(terminalId, fresh);
                slots.release();
                sendDetachedError(terminalId, TerminalErrorCode.PROCESS_START_FAILED, messageOf(e));
            }
        } finally {
            fresh.lock().unlock();
        }
    }

    /** Caller holds ctx.lock(). Creates a new shell/PID for the same terminalId. */
    private void restartLocked(TerminalContext ctx, TerminalPlatform platform, String initialPath) throws Exception {
        String targetPath = (initialPath != null && !initialPath.isBlank())
                ? initialPath
                : (ctx.metadata().currentPath() != null && !ctx.metadata().currentPath().isBlank()
                        ? ctx.metadata().currentPath()
                        : "C:\\");
        ProcessSession session = sessions.start(platform, targetPath);
        ctx.session(session);
        TerminalMetadata m = ctx.metadata();
        m.pid(session.pid());
        m.platform(platform);
        m.currentPath(targetPath);
        m.state(TerminalState.IDLE);
        // Cached metadata (lastCommand/lastResult/lastError/lastErrorCode/timestamps)
        // survives a restart until the next execute resets command-specific fields.
        ctx.activeMarker(null);
        ctx.interrupted(false);
        ctx.resetFraming(new ArrayList<>());
        ctx.resetResultCounter();
        ctx.resetErrorCounter();
        cancelStopTimeoutLocked(ctx);
        ctx.lastEmittedStatusSignature(null); // force status emit
        spawnReaders(ctx);
        emitStatusIfChanged(ctx);
        log.info("Terminal {} started platform={} pid={}", m.terminalId(), platform, m.pid());
    }

    // ---------------------------------------------------------------- execute

    public void execute(String terminalId, String command) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null || terminalId == null || terminalId.isBlank()) {
            sendDetachedError(terminalId == null ? "" : terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            TerminalMetadata m = ctx.metadata();
            if (m.state() == TerminalState.CLOSING || m.state() == TerminalState.CLOSED) {
                agentError(ctx, TerminalErrorCode.TERMINAL_CLOSED);
                emitStatusIfChanged(ctx);
                return;
            }
            if (m.state() == TerminalState.RUNNING) {
                agentError(ctx, TerminalErrorCode.TERMINAL_BUSY);
                emitStatusIfChanged(ctx);
                return;
            }
            if (command == null) {
                command = "";
            }
            if (command.contains("\r") || command.contains("\n")) {
                // Never written to the shell (plan section 20).
                agentError(ctx, TerminalErrorCode.MULTILINE_COMMAND_NOT_ALLOWED);
                emitStatusIfChanged(ctx);
                return;
            }
            ProcessSession session = ctx.session();
            if (session == null || !session.isAlive()) {
                agentError(ctx, TerminalErrorCode.PROCESS_START_FAILED, "Shell process is not alive.");
                emitStatusIfChanged(ctx);
                return;
            }
            String marker = "__AGENT_DONE_" + markerSeq.incrementAndGet() + "__";
            m.lastCommand(command);
            m.lastResult(null);
            m.lastError(null);
            m.lastErrorCode(null);
            m.lastInputTimestamp(System.currentTimeMillis());
            m.lastOutputTimestamp(null);
            m.state(TerminalState.RUNNING);
            ctx.activeMarker(marker);
            ctx.interrupted(false);
            String markerCmd = markerCommand(ctx.metadata().platform(), marker);
            // Fresh framing: raw accumulation + user-output classification restart;
            // the two sent input lines are consumed as console echo, never as result.
            ctx.resetFraming(new ArrayList<>(List.of(command, markerCmd)));
            ctx.resetResultCounter();
            ctx.resetErrorCounter();
            cancelStopTimeoutLocked(ctx);
            emitStatusIfChanged(ctx);
            try {
                // Compound single-line commands pass through untouched (plan section 19).
                session.writeLine(command);
                session.writeLine(markerCmd);
            } catch (Exception e) {
                m.state(TerminalState.IDLE);
                ctx.activeMarker(null);
                agentError(ctx, TerminalErrorCode.PROCESS_WRITE_FAILED, messageOf(e));
                emitStatusIfChanged(ctx);
            }
        } finally {
            ctx.lock().unlock();
        }
    }

    /**
     * Pure execution-boundary sentinel. The marker command carries no verdict about
     * the user command (no exit codes are interpreted): reaching it only means the
     * shell has finished everything queued before it. It deliberately starts its
     * output with a blank line so the marker always begins its own console line —
     * the preceding shell prompt ({@code PS ...> }) has no trailing newline, so
     * without this the marker would share a line with prompt chrome and line-start
     * detection would miss it.
     */
    private static String markerCommand(TerminalPlatform platform, String marker) {
        if (platform == TerminalPlatform.CMD) {
            return "echo. & echo " + marker;
        }
        return "Write-Output \"" + marker + "\"";
    }

    // ------------------------------------------------------------------- stop

    /**
     * Ctrl+C through the terminal's own ConPTY. The shell stays alive and the
     * terminal stays RUNNING until the shell actually reaches the queued command
     * boundary (the marker) or the stop timeout fires — Ctrl+C delivery and
     * command completion are distinct events, so a following EXECUTE can never
     * overlap the interrupted command.
     */
    public void stop(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            TerminalMetadata m = ctx.metadata();
            if (m.state() == TerminalState.CLOSED || m.state() == TerminalState.CLOSING) {
                agentError(ctx, TerminalErrorCode.TERMINAL_CLOSED);
                emitStatusIfChanged(ctx);
                return;
            }
            if (m.state() != TerminalState.RUNNING) {
                emitStatusIfChanged(ctx); // IDLE no-op
                return;
            }
            ProcessSession session = ctx.session();
            boolean delivered = session != null && ProcessSession.interrupt(session);
            if (!delivered) {
                agentError(ctx, TerminalErrorCode.PROCESS_INTERRUPTION_FAILED);
                emitStatusIfChanged(ctx);
                return;
            }
            ctx.interrupted(true);
            // Marker stays armed: late output (including the queued marker line)
            // still belongs to this command. IDLE happens at the boundary or timeout.
            scheduleStopTimeoutLocked(ctx, ctx.activeMarker());
        } finally {
            ctx.lock().unlock();
        }
    }

    /** Caller holds ctx.lock(). Arms the forced STOP-boundary completion. */
    private void scheduleStopTimeoutLocked(TerminalContext ctx, String marker) {
        cancelStopTimeoutLocked(ctx);
        ScheduledFuture<?> f = stopTimeouts.schedule(() -> {
            ctx.lock().lock();
            try {
                if (ctx.metadata().state() == TerminalState.RUNNING
                        && ctx.interrupted()
                        && marker != null
                        && marker.equals(ctx.activeMarker())) {
                    log.warn("Terminal {} STOP boundary timeout after {}ms; forcing completion",
                            ctx.metadata().terminalId(), config.stopTimeoutMs());
                    completeLocked(ctx, true);
                }
            } finally {
                ctx.lock().unlock();
            }
        }, config.stopTimeoutMs(), TimeUnit.MILLISECONDS);
        ctx.stopTimeout(f);
    }

    /** Caller holds ctx.lock(). */
    private void cancelStopTimeoutLocked(TerminalContext ctx) {
        ScheduledFuture<?> f = ctx.stopTimeout();
        ctx.stopTimeout(null);
        if (f != null) {
            f.cancel(false);
        }
    }

    // ------------------------------------------------------------------ close

    /** Final closure: terminate fully, verify, release resources + slot. */
    public void close(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            TerminalMetadata m = ctx.metadata();
            if (m.state() == TerminalState.CLOSED) {
                agentError(ctx, TerminalErrorCode.TERMINAL_CLOSED);
                return;
            }
            if (m.state() != TerminalState.CLOSING) {
                m.state(TerminalState.CLOSING);
                ctx.activeMarker(null);
                cancelStopTimeoutLocked(ctx);
                emitStatusIfChanged(ctx); // CLOSING retains the slot
            }
            // Same termination block serves first close and CLOSING retries.
            try {
                // Retain the session reference until termination is verified, so a
                // failed close can actually be retried instead of stranding CLOSING.
                ProcessSession session = ctx.session();
                if (session != null) {
                    session.destroy();
                    long end = System.currentTimeMillis() + 3000;
                    while (session.isAlive() && System.currentTimeMillis() < end) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                if (session != null && session.isAlive()) {
                    agentError(ctx, TerminalErrorCode.PROCESS_TERMINATION_FAILED,
                            "Process still alive after termination request.");
                    return; // stay CLOSING, session + slot retained
                }
                ctx.session(null);
                m.state(TerminalState.CLOSED);
                m.pid(-1);
                slots.release();
            } catch (Exception e) {
                agentError(ctx, TerminalErrorCode.PROCESS_TERMINATION_FAILED, messageOf(e));
                return; // stay CLOSING, session + slot retained
            }
            emitStatusIfChanged(ctx); // final observable notification: state=CLOSED
            log.info("Terminal {} closed", terminalId);
        } finally {
            ctx.lock().unlock();
        }
    }

    /** Purges a CLOSED terminal context from memory. Has no effect if terminal is active or running. */
    public void remove(String terminalId) {
        if (terminalId == null || terminalId.isBlank()) {
            return;
        }
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            return;
        }
        ctx.lock().lock();
        try {
            if (ctx.metadata().state() == TerminalState.CLOSED) {
                terminals.remove(terminalId);
                log.info("Terminal {} metadata purged from memory cache", terminalId);
            } else {
                log.warn("Cannot purge terminal {}: state is {}", terminalId, ctx.metadata().state());
            }
        } finally {
            ctx.lock().unlock();
        }
    }

    // ------------------------------------------------- metadata_lastResult/Error

    public void metadataLastResult(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            transferInChunks(ctx, terminalId, ctx.metadata().lastResult(), true);
        } finally {
            ctx.lock().unlock();
        }
    }

    public void metadataLastError(String terminalId) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            sendDetachedError(terminalId, TerminalErrorCode.TERMINAL_NOT_FOUND);
            return;
        }
        ctx.lock().lock();
        try {
            transferInChunks(ctx, terminalId, ctx.metadata().lastError(), false);
        } finally {
            ctx.lock().unlock();
        }
    }

    public void metadataList() {
        List<TerminalStatusEvent> list = new ArrayList<>();
        for (TerminalContext ctx : terminals.values()) {
            ctx.lock().lock();
            try {
                TerminalMetadata m = ctx.metadata();
                TerminalStatusEvent e = new TerminalStatusEvent();
                e.terminalId = m.terminalId();
                e.pid = m.pid();
                e.platform = m.platform() == null ? null : m.platform().name();
                e.currentPath = m.currentPath();
                e.lastCommand = m.lastCommand();
                e.lastErrorCode = m.lastErrorCode();
                e.lastResult = m.lastResult();
                e.lastError = m.lastError();
                e.state = m.state().name();
                e.lastInputTimestamp = m.lastInputTimestamp();
                e.lastOutputTimestamp = m.lastOutputTimestamp();
                e.elevated = m.elevated();
                list.add(e);
            } finally {
                ctx.lock().unlock();
            }
        }
        com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent response =
                new com.automation.agent.protocol.TerminalProtocol.MetadataListResponseEvent();
        response.terminals = list;
        emit(response);
    }

    private void transferInChunks(TerminalContext ctx, String terminalId, String value, boolean result) {
        String data = value == null ? "" : value;
        int size = Math.max(1, config.transferChunkChars());
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < data.length(); i += size) {
            parts.add(data.substring(i, Math.min(data.length(), i + size)));
        }
        if (parts.isEmpty()) {
            parts.add("");
        }
        for (int i = 0; i < parts.size(); i++) {
            boolean end = i == parts.size() - 1;
            int count = i + 1;
            if (result) {
                MetadataResultEvent e = new MetadataResultEvent();
                e.terminalId = terminalId;
                e.chunk = parts.get(i);
                e.count = count;
                e.end = end;
                emit(e);
            } else {
                MetadataErrorEvent e = new MetadataErrorEvent();
                e.terminalId = terminalId;
                e.chunk = parts.get(i);
                e.count = count;
                e.end = end;
                emit(e);
            }
        }
        log.debug("metadata transfer {} terminal={} chunks={}", result ? "lastResult" : "lastError",
                terminalId, parts.size());
    }

    // ------------------------------------------------------- output streaming

    private void spawnReaders(TerminalContext ctx) {
        String terminalId = ctx.metadata().terminalId();
        ProcessSession session = ctx.session();
        Thread out = new Thread(
                new ProcessSession.OutputReader(session.stdout(), text -> handleStdout(terminalId, text)),
                "term-" + terminalId + "-stdout");
        out.setDaemon(true);
        ctx.readerThread(out);
        out.start();
        if (session.stderr() != null) {
            Thread err = new Thread(
                    new ProcessSession.OutputReader(session.stderr(), text -> handleStderr(terminalId, text)),
                    "term-" + terminalId + "-stderr");
            err.setDaemon(true);
            err.start();
        }
    }

    /** Caller: reader thread. Locks the terminal. */
    void handleStdout(String terminalId, String text) {
        System.err.println("DEBUG STDOUT [" + terminalId + "]: " + text.replace("\r", "\\r").replace("\n", "\\n"));
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            return;
        }
        ctx.lock().lock();
        try {
            extractCurrentPath(ctx, text);
            if (ctx.metadata().state() != TerminalState.RUNNING || ctx.activeMarker() == null) {
                return; // stray output (e.g. post-completion prompt tail) — drop deterministically
            }
            String marker = ctx.activeMarker();
            // Raw accumulation: detection always runs on the whole buffer, so a marker
            // split across reader chunks is still found. Only complete lines are
            // classified; a trailing partial line waits for more bytes. Boundaries are
            // \n, \r\n, AND lone \r (console prompt/progress rewrites); a \r that is
            // followed by \r or \n belongs to a \r\r\n sequence, not a boundary.
            StringBuilder raw = ctx.liveResultBuffer();
            String cleanedText = text.replaceAll("\u001B\\[[0-9]+;[0-9]+[Hf]|\u001B\\[[0-9]*[Ed]", "\n");
            raw.append(cleanedText);
            log.info("handleStdout rawText: [{}]", text);
            while (true) {
                int pos = ctx.parseOffset();
                int j = pos;
                int lineEnd = -1;
                int termEnd = -1;
                while (j < raw.length()) {
                    char c = raw.charAt(j);
                    if (c == '\n') {
                        lineEnd = j;
                        termEnd = j + 1;
                        break;
                    }
                    if (c == '\r') {
                        if (j + 1 >= raw.length()) {
                            break; // trailing lone \r: may still become \r\n, wait
                        }
                        char next = raw.charAt(j + 1);
                        if (next == '\n') {
                            lineEnd = j;
                            termEnd = j + 2;
                            break;
                        }
                        if (next == '\r') {
                            j++; // part of a \r\r\n sequence, keep scanning
                            continue;
                        }
                        lineEnd = j; // lone \r rewrite boundary
                        termEnd = j + 1;
                        break;
                    }
                    j++;
                }
                if (lineEnd < 0) {
                    break;
                }
                String rawLine = raw.substring(pos, termEnd);
                ctx.parseOffset(termEnd);
                String norm = TerminalModels.normalizeLine(rawLine);
                log.info("handleStdout parsed: id={} rawLine=[{}] norm=[{}] expectedEcho={}", terminalId, rawLine.replace("\r", "\\r").replace("\n", "\\n"), norm, ctx.expectedEcho());
                if (norm.equals(">>") || norm.startsWith(">> ")) {
                    continue; // drop PowerShell multiline continuation prompt chrome
                }
                if (norm.matches("^(?:PS\\s+)?[A-Za-z]:\\\\[^<>\"|?*\\r\\n]*>$")) {
                    continue; // drop bare shell prompt lines emitted by ConPTY chrome
                }
                if (!marker.isEmpty() && (norm.contains("Write-Output") || norm.contains("echo."))
                        && (norm.contains(marker) || norm.contains("__AGENT_DONE_") || norm.contains("__AGENT_D"))) {
                    continue; // drop marker command input echo lines (single or wrapped)
                }
                if (consumeEchoLocked(ctx, norm)) {
                    continue; // console input echo is framing, never result
                }
                if (!marker.isEmpty() && norm.contains(marker)) {
                    // The marker command emits one leading blank line; a blank line held
                    // back from the previous step belongs to the marker, not the user.
                    ctx.heldBlankLine(null);
                    finishResultLocked(ctx, pendingUnsent(ctx), ctx.userBuffer().toString());
                    return;
                }
                if (norm.isEmpty()) {
                    // Hold one blank line back: it may be the marker command's leading
                    // blank. A previously held blank followed by real content was genuine.
                    String held = ctx.heldBlankLine();
                    if (held != null) {
                        emitUserLine(ctx, terminalId, held);
                    }
                    ctx.heldBlankLine(rawLine);
                } else {
                    String held = ctx.heldBlankLine();
                    if (held != null) {
                        ctx.heldBlankLine(null);
                        emitUserLine(ctx, terminalId, held);
                    }
                    emitUserLine(ctx, terminalId, rawLine);
                }
            }
        } finally {
            ctx.lock().unlock();
        }
    }

    /**
     * Consumes one expected console-echo line, at most once each, in send order.
     * Caller holds ctx.lock().
     */
    private boolean consumeEchoLocked(TerminalContext ctx, String normalizedLine) {
        List<String> expected = ctx.expectedEcho();
        for (int i = 0; i < expected.size(); i++) {
            if (TerminalModels.isEchoOf(normalizedLine,
                    TerminalModels.normalizeLine(expected.get(i)))) {
                for (int j = 0; j <= i; j++) {
                    expected.remove(0);
                }
                return true;
            }
        }
        return false;
    }

    /** Streams one classified user-output line as a live chunk. Caller holds lock. */
    private void emitUserLine(TerminalContext ctx, String terminalId, String rawLine) {
        ctx.userBuffer().append(rawLine);
        TerminalResultEvent e = new TerminalResultEvent();
        e.terminalId = terminalId;
        e.chunk = rawLine;
        e.count = ctx.nextResultCount();
        e.end = false;
        emit(e);
        ctx.streamedChars(ctx.userBuffer().length());
    }

    /** Classified-but-unemitted user text. Caller holds lock. */
    private String pendingUnsent(TerminalContext ctx) {
        return ctx.userBuffer().substring(Math.min(ctx.streamedChars(), ctx.userBuffer().length()));
    }

    /** Caller: reader thread. Locks the terminal. */
    void handleStderr(String terminalId, String text) {
        TerminalContext ctx = terminals.get(terminalId);
        if (ctx == null) {
            return;
        }
        ctx.lock().lock();
        try {
            if (ctx.metadata().state() != TerminalState.RUNNING || ctx.activeMarker() == null) {
                return;
            }
            ctx.liveErrorBuffer().append(text);
            TerminalErrorEvent e = new TerminalErrorEvent();
            e.terminalId = terminalId;
            e.errorCode = null; // process stderr: no agent error code
            e.chunk = text;
            e.count = ctx.nextErrorCount();
            e.end = false;
            emit(e);
        } finally {
            ctx.lock().unlock();
        }
    }

    /** Complete the active command. Caller holds ctx.lock(). */
    private void completeLocked(TerminalContext ctx, boolean interrupted) {
        cancelStopTimeoutLocked(ctx);
        // No marker boundary was seen (STOP/timeout path): any held-back blank line
        // is genuine output, not marker chrome.
        String held = ctx.heldBlankLine();
        if (held != null) {
            ctx.heldBlankLine(null);
            emitUserLine(ctx, ctx.metadata().terminalId(), held);
        }
        finishResultLocked(ctx, pendingUnsent(ctx), ctx.userBuffer().toString());
    }

    /** Emit final chunk (end=true), cache bounded metadata, go IDLE. Caller holds lock. */
    private void finishResultLocked(TerminalContext ctx, String unsent, String fullContent) {
        cancelStopTimeoutLocked(ctx);
        TerminalMetadata m = ctx.metadata();
        String terminalId = m.terminalId();
        TerminalResultEvent fin = new TerminalResultEvent();
        fin.terminalId = terminalId;
        fin.chunk = unsent;
        fin.count = ctx.nextResultCount();
        fin.end = true;
        emit(fin);
        ctx.resetResultCounter();
        // Flush any stderr remainder as its own end=true (only when stderr happened).
        if (ctx.liveErrorBuffer().length() > 0) {
            TerminalErrorEvent ferr = new TerminalErrorEvent();
            ferr.terminalId = terminalId;
            ferr.errorCode = null;
            ferr.chunk = "";
            ferr.count = ctx.nextErrorCount();
            ferr.end = true;
            emit(ferr);
            ctx.resetErrorCounter();
        }
        m.lastResult(TerminalMetadata.bound(fullContent, config.maxResultChars()));
        m.lastError(TerminalMetadata.bound(
                ctx.liveErrorBuffer().length() == 0 ? null : ctx.liveErrorBuffer().toString(),
                config.maxErrorChars()));
        m.lastOutputTimestamp(System.currentTimeMillis());
        m.state(TerminalState.IDLE);
        ctx.activeMarker(null);
        ctx.streamedChars(0);
        // Framing for this execution is over; stray late bytes are dropped while IDLE.
        ctx.userBuffer().setLength(0);
        ctx.parseOffset(0);
        ctx.expectedEcho().clear();
        emitStatusIfChanged(ctx);
        log.debug("Terminal {} command complete interrupted={}", terminalId, ctx.interrupted());
    }

    // --------------------------------------------------------------- observers

    private void emitStatusIfChanged(TerminalContext ctx) {
        TerminalMetadata m = ctx.metadata();
        String sig = m.statusSignature();
        if (sig.equals(ctx.lastEmittedStatusSignature())) {
            return;
        }
        ctx.lastEmittedStatusSignature(sig);
        TerminalStatusEvent e = new TerminalStatusEvent();
        e.terminalId = m.terminalId();
        e.pid = m.pid();
        e.platform = m.platform() == null ? null : m.platform().name();
        e.currentPath = m.currentPath();
        e.lastCommand = m.lastCommand();
        e.lastErrorCode = m.lastErrorCode();
        // Bounded cache values ride along but never trigger emission (see signature).
        e.lastResult = m.lastResult();
        e.lastError = m.lastError();
        e.state = m.state().name();
        e.lastInputTimestamp = m.lastInputTimestamp();
        e.lastOutputTimestamp = m.lastOutputTimestamp();
        e.elevated = m.elevated();
        emit(e);
    }

    private static final java.util.regex.Pattern PROMPT_PATH_PATTERN =
            java.util.regex.Pattern.compile("(?:^|[\\r\\n])(?:PS\\s+)?([A-Za-z]:\\\\[^<>\"|?*\\r\\n]*)>");

    private void extractCurrentPath(TerminalContext ctx, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String stripped = TerminalModels.stripAnsi(text);
        java.util.regex.Matcher matcher = PROMPT_PATH_PATTERN.matcher(stripped);
        String lastFound = null;
        while (matcher.find()) {
            String p = matcher.group(1).trim();
            if (!p.isEmpty()) {
                if (p.length() == 2 && p.charAt(1) == ':') {
                    p = p + "\\";
                } else if (p.endsWith("\\") && p.length() > 3) {
                    p = p.substring(0, p.length() - 1);
                }
                lastFound = p;
            }
        }
        if (lastFound != null && !lastFound.isEmpty()) {
            if (!lastFound.equals(ctx.metadata().currentPath())) {
                ctx.metadata().currentPath(lastFound);
                emitStatusIfChanged(ctx);
            }
        }
    }

    private void sendMax(String terminalId) {
        // 11th start: no terminal created, no metadata, no process (plan section 7).
        Map<String, String> evt = Map.of(
                "event-type", TerminalEventType.TERMINAL_MAX.wire(),
                "terminalid", terminalId == null ? "" : terminalId);
        emit(evt);
        log.info("Terminal capacity reached; rejected start for {}", terminalId);
    }

    private void agentError(TerminalContext ctx, TerminalErrorCode code) {
        agentError(ctx, code, null);
    }

    private void agentError(TerminalContext ctx, TerminalErrorCode code, String detail) {
        TerminalMetadata m = ctx.metadata();
        m.lastErrorCode(code.name());
        String msg = detail == null ? code.defaultMessage() : code.defaultMessage() + " " + detail;
        // Agent errors populate the cached metadata as well as the live stream.
        m.lastError(TerminalMetadata.bound(msg, config.maxErrorChars()));
        sendErrorChunk(ctx, code, msg, true);
    }

    private void sendErrorChunk(TerminalContext ctx, TerminalErrorCode code, boolean end) {
        sendErrorChunk(ctx, code, code.defaultMessage(), end);
    }

    private void sendErrorChunk(TerminalContext ctx, TerminalErrorCode code, String msg, boolean end) {
        TerminalErrorEvent e = new TerminalErrorEvent();
        e.terminalId = ctx.metadata().terminalId();
        e.errorCode = code.name();
        e.chunk = msg;
        e.count = ctx.nextErrorCount();
        e.end = end;
        emit(e);
        if (end) {
            ctx.resetErrorCounter();
        }
    }

    private void sendDetachedError(String terminalId, TerminalErrorCode code) {
        sendDetachedError(terminalId, code, code.defaultMessage());
    }

    private void sendDetachedError(String terminalId, TerminalErrorCode code, String msg) {
        TerminalErrorEvent e = new TerminalErrorEvent();
        e.terminalId = terminalId == null ? "" : terminalId;
        e.errorCode = code.name();
        e.chunk = msg;
        e.count = 1;
        e.end = true;
        emit(e);
    }

    private void emit(Object eventPojo) {
        Consumer<Object> s = sink;
        if (s == null) {
            log.debug("No event sink; dropping {}", eventPojo);
            return;
        }
        try {
            s.accept(eventPojo);
        } catch (Exception e) {
            log.warn("Event sink failed: {}", e.toString());
        }
    }

    // ---------------------------------------------------------------- helpers

    private static String messageOf(Throwable t) {
        String m = t.getMessage();
        return m == null ? t.toString() : m;
    }

    // Visible for tests.
    int activeCount() {
        return slots.availablePermits() >= 0 ? config.terminalMaxCount() - slots.availablePermits() : 0;
    }
}