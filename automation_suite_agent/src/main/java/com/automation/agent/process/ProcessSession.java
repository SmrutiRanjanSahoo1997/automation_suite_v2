package com.automation.agent.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.terminal.TerminalModels.TerminalPlatform;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * One isolated console session: the real cmd.exe / powershell.exe plus its
 * OS identity. Either a Win32 ConPTY session (Windows) or a piped
 * {@link java.lang.Process} fallback. The rest of the agent only uses this
 * interface; the concrete implementation, the launcher, the Ctrl+C interrupt
 * and the streaming decoder all live in this single file.
 */
public interface ProcessSession {

    TerminalPlatform platform();

    long pid();

    /** Write one physical input line (without line break) to the shell. */
    void writeLine(String line) throws IOException;

    /** Raw byte write (used for Ctrl+C 0x03 on ConPTY). */
    void writeRaw(byte[] bytes) throws IOException;

    InputStream stdout();

    /** Null when stdout/stderr are merged (ConPTY). */
    InputStream stderr();

    boolean isAlive();

    /** Fully terminate the shell/process and release native resources. */
    void destroy();

    // ------------------------------------------------------------ launcher

    /**
     * Creates cmd.exe / powershell.exe as an isolated Windows ConPTY session — the
     * single supported mechanism. There is deliberately no piped-process fallback:
     * ConPTY and piped shells have different output/interrupt semantics, and V2
     * terminal behavior (per-terminal Ctrl+C, merged console stream) depends on a
     * real pseudo-console. When ConPTY cannot be established, terminal_start fails
     * with PROCESS_START_FAILED. Never forces a working directory and never
     * introduces runas/impersonation — shells inherit the agent's execution context.
     */
    static ProcessSession launch(TerminalPlatform platform, AgentConfig config) throws IOException {
        return launch(platform, null, config);
    }

    static ProcessSession launch(TerminalPlatform platform, String initialPath, AgentConfig config) throws IOException {
        Optional<ProcessSession> conpty =
                ConPty.tryCreate(platform, initialPath, config.conptyCols(), config.conptyRows());
        if (conpty.isPresent()) {
            return conpty.get(); // staged [CONPTY-DIAG] already logged by tryCreate
        }
        throw new IOException("ConPTY session could not be established for platform " + platform);
    }

    // ----------------------------------------------------------- interruption

    /** Ctrl+C through the session's own ConPTY. Never kills the shell. */
    static boolean interrupt(ProcessSession session) {
        try {
            session.writeRaw(new byte[]{0x03});
            return true;
        } catch (IOException e) {
            log().warn("Ctrl+C delivery failed: {}", e.toString());
            return false;
        }
    }

    static Logger log() {
        return LoggerFactory.getLogger(ProcessSession.class);
    }

    // ---------------------------------------------------------------- reader

    /**
     * Streams bytes from a process/console output handle to a chunk consumer.
     * Decoding is incremental UTF-8: a multibyte character split across two
     * {@code read()} calls is reassembled, never replaced with U+FFFD. VT escape
     * sequences from ConPTY pass through untouched — Spring Boot owns presentation.
     */
    final class OutputReader implements Runnable {

        private final InputStream source;
        private final Consumer<String> onText;
        private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        // Incomplete trailing bytes (at most 3 for UTF-8) carried to the next read.
        private byte[] carry = new byte[0];
        private volatile boolean stopped;

        public OutputReader(InputStream source, Consumer<String> onText) {
            this.source = source;
            this.onText = onText;
        }

        public void stop() {
            stopped = true;
        }

        @Override
        public void run() {
            byte[] buf = new byte[8192];
            try {
                int n;
                while (!stopped && (n = source.read(buf)) != -1) {
                    if (n == 0 && carry.length == 0) {
                        continue;
                    }
                    StringBuilder hex = new StringBuilder();
                    for (int i = 0; i < Math.min(n, 40); i++) {
                        hex.append(String.format("%02X ", buf[i]));
                    }
                    System.err.println("HEX BYTES n=" + n + ": [" + hex.toString().trim() + "]");
                    String text = decode(buf, n, false);
                    if (!text.isEmpty()) {
                        onText.accept(text);
                    }
                }
                if (!stopped) {
                    String tail = decode(new byte[0], 0, true);
                    if (!tail.isEmpty()) {
                        onText.accept(tail);
                    }
                }
            } catch (Exception ignored) {
                // Stream closed on terminal close — normal shutdown path.
            }
        }

        /**
         * Decodes {@code len} fresh bytes plus any carried-over trailing bytes.
         * With {@code endOfInput=false} the decoder leaves an incomplete trailing
         * character unconsumed; those bytes become the next carry.
         */
        private String decode(byte[] buf, int len, boolean endOfInput) {
            byte[] combined = new byte[carry.length + len];
            System.arraycopy(carry, 0, combined, 0, carry.length);
            System.arraycopy(buf, 0, combined, carry.length, len);
            ByteBuffer in = ByteBuffer.wrap(combined);
            CharBuffer out = CharBuffer.allocate(combined.length + 1);
            decoder.decode(in, out, endOfInput);
            if (endOfInput) {
                decoder.flush(out);
                carry = new byte[0];
            } else {
                byte[] rest = new byte[in.remaining()];
                in.get(rest);
                carry = rest;
            }
            out.flip();
            return out.toString();
        }
    }

    // ------------------------------------------------------------ conpty impl

    /** True Windows ConPTY session via JNA (CreatePseudoConsole + CreateProcess). */
    final class ConPty implements ProcessSession {

        private final Logger log = LoggerFactory.getLogger(ConPty.class);

        private final TerminalPlatform platform;
        private final int pid;
        private final Pointer hpc;
        private final Pointer hProcess;
        private final Pointer hThread;
        private final ConPtyNative.HandleInputStream out;
        private final ConPtyNative.HandleOutputStream in;
        private final Pointer inWriteHandle;
        private final Pointer outReadHandle;
        private volatile boolean closed;

        ConPty(TerminalPlatform platform, int pid, Pointer hpc, Pointer hProcess, Pointer hThread,
                Pointer inWriteHandle, Pointer outReadHandle) {
            this.platform = platform;
            this.pid = pid;
            this.hpc = hpc;
            this.hProcess = hProcess;
            this.hThread = hThread;
            this.inWriteHandle = inWriteHandle;
            this.outReadHandle = outReadHandle;
            this.out = new ConPtyNative.HandleInputStream(outReadHandle);
            this.in = new ConPtyNative.HandleOutputStream(inWriteHandle);
        }

        @Override
        public TerminalPlatform platform() {
            return platform;
        }

        @Override
        public long pid() {
            return pid;
        }

        @Override
        public synchronized void writeLine(String line) throws IOException {
            writeRaw((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public synchronized void writeRaw(byte[] bytes) throws IOException {
            if (closed) {
                throw new IOException("ConPTY session closed");
            }
            in.write(bytes);
            in.flush();
        }

        @Override
        public InputStream stdout() {
            return out;
        }

        @Override
        public InputStream stderr() {
            return null; // ConPTY merges console output into one VT stream.
        }

        @Override
        public boolean isAlive() {
            if (closed) {
                return false;
            }
            try {
                IntByReference code = new IntByReference();
                if (ConPtyNative.K32.INSTANCE.GetExitCodeProcess(hProcess, code)) {
                    return code.getValue() == 259; // STILL_ACTIVE
                }
            } catch (Throwable t) {
                log.debug("GetExitCodeProcess failed", t);
            }
            return ConPtyNative.K32.INSTANCE.WaitForSingleObject(hProcess, 0) != 0; // WAIT_OBJECT_0 == 0 means exited
        }

        /** Process exit code, or 259 (STILL_ACTIVE), or -1 when unknown. */
        int exitCode() {
            try {
                IntByReference code = new IntByReference(-1);
                if (ConPtyNative.K32.INSTANCE.GetExitCodeProcess(hProcess, code)) {
                    return code.getValue();
                }
            } catch (Throwable ignored) {
            }
            return -1;
        }

        @Override
        public synchronized void destroy() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                closePseudoConsoleBounded(hpc);
                ConPtyNative.K32.INSTANCE.TerminateProcess(hProcess, 1);
            } catch (Exception e) {
                log.debug("TerminateProcess failed", e);
            } finally {
                safeClose(inWriteHandle);
                safeClose(outReadHandle);
                safeClose(hThread);
                safeClose(hProcess);
            }
        }

        private static void closePseudoConsoleBounded(Pointer hpc) {
            if (hpc == null) {
                return;
            }
            Thread t = new Thread(() -> {
                try {
                    ConPtyNative.K32.INSTANCE.ClosePseudoConsole(hpc);
                } catch (Throwable th) {
                    log().debug("ClosePseudoConsole failed", th);
                }
            }, "conpty-close");
            t.setDaemon(true);
            t.start();
            try {
                t.join(500);
            } catch (InterruptedException e) {
                log().debug("Interrupted waiting for ClosePseudoConsole", e);
            }
            if (t.isAlive()) {
                log().warn("ClosePseudoConsole did not complete in 5s; abandoning HPCON to avoid hanging terminal close");
            }
        }

        private static void safeClose(Pointer h) {
            if (h != null) {
                try {
                    ConPtyNative.K32.INSTANCE.CloseHandle(h);
                } catch (Exception ignored) {
                }
            }
        }

        /** Try a real ConPTY session; empty when unavailable (non-Windows or native failure). */
        static Optional<ProcessSession> tryCreate(TerminalPlatform platform, String initialPath, int cols, int rows) {
            if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
                return Optional.empty();
            }
            StringBuilder diag = new StringBuilder();
            diag.append("[CONPTY-DIAG] platform=").append(platform)
                    .append(" os=").append(System.getProperty("os.name")).append(' ')
                    .append(System.getProperty("os.version"))
                    .append(" os.arch=").append(System.getProperty("os.arch"))
                    .append(" java=").append(System.getProperty("java.version"))
                    .append(" jvm.bits=").append(System.getProperty("sun.arch.data.model", "?"))
                    .append(" jna.ptrBytes=").append(Native.POINTER_SIZE)
                    .append(" conpty=").append(cols).append('x').append(rows);
            ConPty created = null;
            try {
                String cmd = platform == TerminalPlatform.CMD
                        ? cmdLine()
                        : psLine();
                logPreflight(cmd, diag);
                created = create(platform, cmd, initialPath, cols, rows, diag);
                // The native calls can succeed while the console host itself is broken
                // (e.g. headless/container Windows: child dies with 0xC0000142). Verify
                // the shell actually survives before handing the session out.
                Thread.sleep(500);
                if (!created.isAlive()) {
                    int exit = created.exitCode();
                    diag.append(" | aliveCheck=DEAD exit=").append(exit)
                            .append(" (0x").append(Integer.toHexString(exit)).append(')');
                    log().warn("ConPTY shell died immediately (exit={}); reporting failure",
                            created.exitCode());
                    log().warn("{}", diag);
                    created.destroy();
                    return Optional.empty();
                }
                diag.append(" | aliveCheck=ALIVE pid=").append(created.pid());
                log().info("Started {} ConPTY session pid={} | {}", platform, created.pid(), diag);
                return Optional.of(created);
            } catch (Throwable t) {
                diag.append(" | FAILED: ").append(t);
                log().warn("ConPTY creation failed: {}", t.toString());
                log().warn("{}", diag);
                if (created != null) {
                    try {
                        created.destroy();
                    } catch (Throwable ignored) {
                    }
                }
                return Optional.empty();
            }
        }

        /** Interactive CMD with quiet startup and prompt loop (kept alive). */
        private static String cmdLine() {
            return "C:\\Windows\\System32\\cmd.exe /q /k";
        }

        /** Interactive PowerShell, keep alive, bypass execution policy for scripts. */
        private static String psLine() {
            return "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe "
                    + "-NoLogo -NoExit -NoProfile -ExecutionPolicy Bypass";
        }

        private static void logPreflight(String cmd, StringBuilder diag) {
            try {
                String exe = cmd.split(" ")[0];
                java.nio.file.Path p = java.nio.file.Path.of(exe);
                boolean exists = java.nio.file.Files.isExecutable(p);
                long size = -1;
                try {
                    size = java.nio.file.Files.size(p);
                } catch (Exception ignored) {
                }
                boolean conhost = java.nio.file.Files.exists(
                        java.nio.file.Path.of("C:\\Windows\\System32\\conhost.exe"));
                boolean console = parentHasConsole();
                diag.append(" | preflight: exe=").append(exe)
                        .append(" exists=").append(exists)
                        .append(" size=").append(size)
                        .append(" conhostExists=").append(conhost)
                        .append(" parentConsole=").append(console);
                log().info("ConPTY preflight: exe={} exists={} size={} conhostExists={} parentConsole={} os={}",
                        exe, exists, size, conhost, console,
                        System.getProperty("os.name") + " " + System.getProperty("os.version"));
            } catch (Throwable t) {
                log().debug("ConPTY preflight failed", t);
            }
        }

        private static boolean parentHasConsole() {
            try {
                return ConPtyNative.K32.INSTANCE.GetConsoleWindow() != null;
            } catch (Throwable t) {
                return false;
            }
        }

        private static ConPty create(TerminalPlatform platform, String commandLine, String initialPath, int cols, int rows,
                StringBuilder diag) throws IOException {
            ConPtyNative.K32 k = ConPtyNative.K32.INSTANCE;
            ConPtyNative.SECURITY_ATTRIBUTES sa = new ConPtyNative.SECURITY_ATTRIBUTES();
            sa.write();

            PointerByReference inRead = new PointerByReference();
            PointerByReference inWrite = new PointerByReference();
            PointerByReference outRead = new PointerByReference();
            PointerByReference outWrite = new PointerByReference();
            if (!k.CreatePipe(inRead, inWrite, sa, 0) || !k.CreatePipe(outRead, outWrite, sa, 0)) {
                throw new IOException("CreatePipe failed err=" + Native.getLastError());
            }
            // Retained handles must NOT be inherited by child processes (HANDLE_FLAG_INHERIT = 1).
            k.SetHandleInformation(inWrite.getValue(), 1, 0);
            k.SetHandleInformation(outRead.getValue(), 1, 0);
            diag.append(" | pipes=OK");

            ConPtyNative.COORD.ByValue size = new ConPtyNative.COORD.ByValue();
            size.X = (short) cols;
            size.Y = (short) rows;
            size.write();
            PointerByReference phPC = new PointerByReference();
            int hr = k.CreatePseudoConsole(size, inRead.getValue(), outWrite.getValue(), 0, phPC);
            if (hr != 0) {
                k.CloseHandle(inWrite.getValue());
                k.CloseHandle(outRead.getValue());
                k.CloseHandle(inRead.getValue());
                k.CloseHandle(outWrite.getValue());
                throw new IOException("CreatePseudoConsole failed hr=" + hr);
            }
                Pointer hpc = phPC.getValue();
                diag.append(" | pseudoConsole: hr=0 hpc=").append(hpc);
                // ConPTY duplicates or takes ownership of inRead and outWrite.
                // Parent MUST close these handles so parent does not retain extra pipe references.
                k.CloseHandle(inRead.getValue());
                k.CloseHandle(outWrite.getValue());

                Pointer attrList = null;
                try {
                    Memory sizeMem = ConPtyNative.pointerWidthMemory(0);
                    k.InitializeProcThreadAttributeList(null, 1, 0, sizeMem);
                    long need = sizeMem.getLong(0);
                    if (need <= 0 || need > 1 << 20) {
                        throw new IOException("Bad proc-thread-attribute-list size: " + need);
                    }
                    attrList = new Memory(need);
                    diag.append(" | attrListBytes=").append(need);
                    Memory needMem = ConPtyNative.pointerWidthMemory(need);
                    if (!k.InitializeProcThreadAttributeList(attrList, 1, 0, needMem)) {
                        throw new IOException(
                                "InitializeProcThreadAttributeList failed err=" + Native.getLastError());
                    }
                    if (!k.UpdateProcThreadAttribute(attrList, 0, ConPtyNative.PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE,
                            hpc, Native.POINTER_SIZE, null, null)) {
                        throw new IOException(
                                "UpdateProcThreadAttribute PSEUDOCONSOLE failed err=" + Native.getLastError());
                    }
                    diag.append(" | attrAssigned=OK");

                    ConPtyNative.STARTUPINFOEX si = new ConPtyNative.STARTUPINFOEX();
                    si.lpAttributeList = attrList;
                    si.startupInfo.cb = si.size();
                    si.startupInfo.dwFlags = ConPtyNative.STARTF_USESTDHANDLES;
                    si.startupInfo.write();
                    si.write();
                    ConPtyNative.PROCESS_INFORMATION pi = new ConPtyNative.PROCESS_INFORMATION();

                    char[] cmdChars = (commandLine + "\0").toCharArray();
                    Memory cmdMem = new Memory((long) cmdChars.length * 2L);
                    cmdMem.write(0, cmdChars, 0, cmdChars.length);

                    String workDir = (initialPath != null && !initialPath.isBlank()) ? initialPath : "C:\\";
                    boolean ok = k.CreateProcessW(null, cmdMem, null, null, false,
                            ConPtyNative.EXTENDED_STARTUPINFO_PRESENT, null, new WString(workDir), si, pi);
                    if (!ok) {
                        throw new IOException("CreateProcess failed for " + commandLine + " err=" + Native.getLastError());
                    }
                    Pointer hProcess = pi.hProcess;
                    Pointer hThread = pi.hThread;
                    int pid = pi.dwProcessId;
                    diag.append(" | createProcess: ok=true pid=").append(pid);
                    k.CloseHandle(hThread); // thread handle not needed afterwards

                    try {
                        k.DeleteProcThreadAttributeList(attrList);
                    } catch (Exception e) {
                        log().debug("DeleteProcThreadAttributeList failed", e);
                    }
                    return new ConPty(platform, pid, hpc, hProcess, null, inWrite.getValue(),
                            outRead.getValue());
                } catch (Throwable t) {
                    if (attrList != null) {
                        try {
                            k.DeleteProcThreadAttributeList(attrList);
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        k.ClosePseudoConsole(hpc);
                    } catch (Exception ignored) {
                    }
                    try {
                        k.CloseHandle(inWrite.getValue());
                    } catch (Exception ignored) {
                    }
                    try {
                        k.CloseHandle(outRead.getValue());
                    } catch (Exception ignored) {
                    }
                    if (t instanceof IOException ioe) {
                        throw ioe;
                    }
                    throw new IOException("ConPTY setup failed: " + t, t);
                }
            }
        }
}