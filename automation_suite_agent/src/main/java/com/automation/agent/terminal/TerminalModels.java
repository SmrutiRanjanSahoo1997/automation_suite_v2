package com.automation.agent.terminal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Single home for the terminal domain models: state machine, platform catalog,
 * metadata, per-terminal context, and the pure console-framing helpers used by
 * {@link TerminalManager} to turn a raw ConPTY byte stream into classified
 * user output plus a reliable command-completion boundary.
 */
public final class TerminalModels {

    private TerminalModels() {
    }

    // ----------------------------------------------------------------- state

    /**
     * Final V2 terminal states.
     *
     * <pre>
     * IDLE -- execute --> RUNNING
     * RUNNING -- command complete --> IDLE
     * RUNNING -- stop --> IDLE
     * IDLE    -- close --> CLOSING
     * RUNNING -- close --> CLOSING
     * CLOSING -- process fully terminated --> CLOSED
     * CLOSED  -- start --> IDLE
     * </pre>
     * All transitions are atomic (guarded by the per-terminal lock in {@link TerminalContext}).
     */
    public enum TerminalState {
        IDLE,
        RUNNING,
        CLOSING,
        CLOSED
    }

    // --------------------------------------------------------------- platform

    /**
     * Windows shells the agent can host. The agent launches the real console
     * ({@code cmd.exe} / {@code powershell.exe}) — it never parses or emulates them.
     */
    public enum TerminalPlatform {
        CMD,
        PS;

        public static TerminalPlatform parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String norm = raw.trim().toUpperCase().replace("_", "").replace(" ", "").replace(".EXE", "");
            if ("PS".equals(norm) || "POWERSHELL".equals(norm)) {
                return PS;
            }
            if ("CMD".equals(norm) || "COMMAND".equals(norm) || "COMMANDPROMPT".equals(norm)) {
                return CMD;
            }
            return null;
        }
    }

    // --------------------------------------------------------------- metadata

    /**
     * Only the metadata model. No I/O, no threads.
     * Watched fields for terminal_status change detection:
     * terminalId, pid, platform, lastCommand, lastErrorCode, state,
     * lastInputTimestamp, lastOutputTimestamp.
     * lastResult / lastError are deliberately EXCLUDED from status-change detection.
     */
    public static final class TerminalMetadata {

        public static final String TRUNCATION_MARKER = "<-Truncated->";

        private final String terminalId;
        private volatile long pid = -1;
        private volatile TerminalPlatform platform;
        private volatile String currentPath = "C:\\";
        private volatile String lastCommand;
        private volatile String lastResult;
        private volatile String lastError;
        private volatile String lastErrorCode;
        private volatile TerminalState state = TerminalState.IDLE;
        private volatile Long lastInputTimestamp;
        private volatile Long lastOutputTimestamp;
        private volatile boolean elevated = isProcessElevated();

        public static boolean isProcessElevated() {
            try {
                if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                    return com.automation.agent.process.ConPtyNative.Shell32.INSTANCE.IsUserAnAdmin();
                }
            } catch (Throwable t) {
            }
            return false;
        }

        public TerminalMetadata(String terminalId) {
            this.terminalId = terminalId;
        }

        public boolean elevated() {
            return elevated;
        }

        public void elevated(boolean elevated) {
            this.elevated = elevated;
        }

        public String terminalId() {
            return terminalId;
        }

        public long pid() {
            return pid;
        }

        public void pid(long pid) {
            this.pid = pid;
        }

        public TerminalPlatform platform() {
            return platform;
        }

        public void platform(TerminalPlatform platform) {
            this.platform = platform;
        }

        public String currentPath() {
            return currentPath;
        }

        public void currentPath(String currentPath) {
            this.currentPath = currentPath;
        }

        public String lastCommand() {
            return lastCommand;
        }

        public void lastCommand(String lastCommand) {
            this.lastCommand = lastCommand;
        }

        public String lastResult() {
            return lastResult;
        }

        public void lastResult(String lastResult) {
            this.lastResult = lastResult;
        }

        public String lastError() {
            return lastError;
        }

        public void lastError(String lastError) {
            this.lastError = lastError;
        }

        public String lastErrorCode() {
            return lastErrorCode;
        }

        public void lastErrorCode(String lastErrorCode) {
            this.lastErrorCode = lastErrorCode;
        }

        public TerminalState state() {
            return state;
        }

        public void state(TerminalState state) {
            this.state = state;
        }

        public Long lastInputTimestamp() {
            return lastInputTimestamp;
        }

        public void lastInputTimestamp(Long lastInputTimestamp) {
            this.lastInputTimestamp = lastInputTimestamp;
        }

        public Long lastOutputTimestamp() {
            return lastOutputTimestamp;
        }

        public void lastOutputTimestamp(Long lastOutputTimestamp) {
            this.lastOutputTimestamp = lastOutputTimestamp;
        }

        /**
         * Bounds only the cached metadata. The live stream is never truncated.
         */
        public static String bound(String value, int maxChars) {
            if (value == null) {
                return null;
            }
            if (value.length() <= maxChars) {
                return value;
            }
            return value.substring(0, Math.max(0, maxChars)) + TRUNCATION_MARKER;
        }

        /**
         * Signature over watched fields only. Used to decide whether terminal_status must be emitted.
         */
        public String statusSignature() {
            return terminalId + "|" + pid + "|" + platform + "|" + currentPath + "|" + lastCommand + "|"
                    + lastErrorCode + "|" + state + "|" + lastInputTimestamp + "|" + lastOutputTimestamp + "|" + elevated;
        }
    }

    // --------------------------------------------------------------- context

    /**
     * Owns exactly one terminal: terminalId, PID, platform, process resources,
     * metadata, current execution state and chunk counters.
     * All lifecycle mutations must hold {@link #lock()}.
     */
    public static final class TerminalContext {

        private final ReentrantLock lock = new ReentrantLock();
        private final TerminalMetadata metadata;

        // Separate counters because stdout and stderr are separate streams.
        private int resultChunkCounter;
        private int errorChunkCounter;

        // Active execution tracking. Non-null only while state == RUNNING.
        private String activeMarker;
        private boolean interrupted;
        private final StringBuilder liveResultBuffer = new StringBuilder();
        private final StringBuilder liveErrorBuffer = new StringBuilder();
        // Framing state for the current execution (see TerminalModels framing helpers):
        // liveResultBuffer accumulates RAW console text; userBuffer accumulates only
        // classified user-visible output; parseOffset marks raw chars already classified;
        // streamedChars counts userBuffer chars already emitted as chunks; expectedEcho
        // holds the sent input lines whose console echo is consumed exactly once each.
        private final StringBuilder userBuffer = new StringBuilder();
        private int parseOffset;
        // At most one trailing blank line held back: it may be the marker command's
        // leading blank (dropped at the boundary) rather than genuine user output.
        private String heldBlankLine;
        private java.util.List<String> expectedEcho = java.util.List.of();
        // Live-stream accounting: chars of liveResultBuffer already emitted as chunks.
        private int streamedChars;
        // Pending STOP-boundary timeout. Non-null only between Ctrl+C and command completion.
        private ScheduledFuture<?> stopTimeout;

        // Last emitted status signature (watched fields). Null forces first emit.
        private String lastEmittedStatusSignature;

        // Process resources, set on start / restart.
        private volatile com.automation.agent.process.ProcessSession session;
        private volatile Thread readerThread;

        public TerminalContext(String terminalId) {
            this.metadata = new TerminalMetadata(terminalId);
        }

        public ReentrantLock lock() {
            return lock;
        }

        public TerminalMetadata metadata() {
            return metadata;
        }

        public int nextResultCount() {
            return ++resultChunkCounter;
        }

        public void resetResultCounter() {
            resultChunkCounter = 0;
        }

        public int nextErrorCount() {
            return ++errorChunkCounter;
        }

        public void resetErrorCounter() {
            errorChunkCounter = 0;
        }

        public String activeMarker() {
            return activeMarker;
        }

        public void activeMarker(String activeMarker) {
            this.activeMarker = activeMarker;
        }

        public boolean interrupted() {
            return interrupted;
        }

        public void interrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }

        public StringBuilder liveResultBuffer() {
            return liveResultBuffer;
        }

        public StringBuilder liveErrorBuffer() {
            return liveErrorBuffer;
        }

        public StringBuilder userBuffer() {
            return userBuffer;
        }

        public int parseOffset() {
            return parseOffset;
        }

        public void parseOffset(int parseOffset) {
            this.parseOffset = parseOffset;
        }

        public java.util.List<String> expectedEcho() {
            return expectedEcho;
        }

        public void expectedEcho(java.util.List<String> expectedEcho) {
            this.expectedEcho = expectedEcho;
        }

        public String heldBlankLine() {
            return heldBlankLine;
        }

        public void heldBlankLine(String heldBlankLine) {
            this.heldBlankLine = heldBlankLine;
        }

        /** Resets all per-execution framing state for a new command. */
        public void resetFraming(java.util.List<String> expectedEchoLines) {
            liveResultBuffer.setLength(0);
            liveErrorBuffer.setLength(0);
            userBuffer.setLength(0);
            parseOffset = 0;
            streamedChars = 0;
            heldBlankLine = null;
            expectedEcho = expectedEchoLines;
        }

        public int streamedChars() {
            return streamedChars;
        }

        public void streamedChars(int streamedChars) {
            this.streamedChars = streamedChars;
        }

        public ScheduledFuture<?> stopTimeout() {
            return stopTimeout;
        }

        public void stopTimeout(ScheduledFuture<?> stopTimeout) {
            this.stopTimeout = stopTimeout;
        }

        public String lastEmittedStatusSignature() {
            return lastEmittedStatusSignature;
        }

        public void lastEmittedStatusSignature(String sig) {
            this.lastEmittedStatusSignature = sig;
        }

        public com.automation.agent.process.ProcessSession session() {
            return session;
        }

        public void session(com.automation.agent.process.ProcessSession session) {
            this.session = session;
        }

        public Thread readerThread() {
            return readerThread;
        }

        public void readerThread(Thread readerThread) {
            this.readerThread = readerThread;
        }
    }

    // ------------------------------------------------------ framing utilities

    // CSI: ESC [ params intermediates final-byte. OSC: ESC ] ... BEL (or ESC \).
    private static final Pattern ANSI_CSI =
            Pattern.compile("\u001B\\[[0-9;?]*[ -/]*[@-~]");
    private static final Pattern ANSI_OSC =
            Pattern.compile("\u001B\\][^\u0007\u001B]*(?:\u0007|\u001B\\\\)");
    private static final Pattern ANSI_SINGLE =
            Pattern.compile("\u001B[@-_]");

    /** Removes VT/ANSI control sequences; plain text is untouched. */
    public static String stripAnsi(String s) {
        if (s == null || s.indexOf('\u001B') < 0) {
            return s;
        }
        // Convert VT row positioning / next-line sequences to \n so lines separated by VT cursor moves don't concatenate
        String r = s.replaceAll("\u001B\\[[0-9]+;[0-9]+[Hf]|\u001B\\[[0-9]*[Ed]", "\n");
        r = ANSI_CSI.matcher(r).replaceAll("");
        r = ANSI_OSC.matcher(r).replaceAll("");
        return ANSI_SINGLE.matcher(r).replaceAll("");
    }

    /** Normalized form used ONLY for detection: ANSI stripped, CR removed, trimmed. */
    public static String normalizeLine(String rawLine) {
        if (rawLine == null) {
            return "";
        }
        String r = stripAnsi(rawLine);
        r = r.replace("\r", "");
        return r.trim();
    }

    /**
     * Finds the first raw-buffer line (at or after {@code fromOffset}, which must sit
     * on a line start) whose normalized form starts with {@code marker}.
     *
     * @return {@code int[]{lineStart, lineEndExclusive}} in raw-buffer coordinates,
     *         or {@code null} when no complete marker line is present yet.
     */
    public static int[] findMarkerLine(String rawBuffer, int fromOffset, String marker) {
        if (rawBuffer == null || marker == null || marker.isEmpty()) {
            return null;
        }
        int pos = Math.max(0, Math.min(fromOffset, rawBuffer.length()));
        while (pos < rawBuffer.length()) {
            int nl = rawBuffer.indexOf('\n', pos);
            if (nl < 0) {
                return null; // trailing partial line: wait for more bytes
            }
            String rawLine = rawBuffer.substring(pos, nl);
            if (normalizeLine(rawLine).startsWith(marker)) {
                return new int[]{pos, nl};
            }
            pos = nl + 1;
        }
        return null;
    }

    /**
     * True when a normalized console line is the echo of a sent input line.
     * A shell prompt prefix is allowed ahead of the echo; nothing may follow it.
     */
    public static boolean isEchoOf(String normalizedLine, String normalizedExpected) {
        if (normalizedExpected == null || normalizedExpected.trim().isEmpty() || normalizedLine == null) {
            return false;
        }
        String line = normalizedLine.trim();
        String exp = normalizedExpected.trim();
        if (line.isEmpty()) {
            return false;
        }
        if ((line.contains("Write-Output") || line.contains("echo.")) && (line.contains("__AGENT_DONE_") || line.contains("__AGENT_D"))) {
            return true;
        }
        if (line.equals(exp) || line.endsWith(exp) || exp.startsWith(line)) {
            return true;
        }
        String promptStripped = line.replaceFirst("^(?:PS\\s+)?[A-Za-z]:\\[^>]*>\\s*", "").trim();
        if (promptStripped.isEmpty()) {
            return false;
        }
        return promptStripped.equals(exp) || promptStripped.endsWith(exp) || exp.startsWith(promptStripped);
    }

    /**
     * Removes every raw-buffer line that mentions the marker (the marker command's
     * own input echo and the marker output line). The marker is unique per command,
     * so this cannot touch legitimate output.
     */
    public static String removeMarkerLines(String raw, String marker) {
        if (raw == null || raw.isEmpty() || marker == null || marker.isEmpty()) {
            return raw == null ? "" : raw;
        }
        String[] lines = raw.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(marker)) {
                continue;
            }
            sb.append(lines[i]);
            if (i < lines.length - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}