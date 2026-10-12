package com.automation.agent.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.automation.agent.process.ProcessSession;
import com.automation.agent.terminal.TerminalModels.TerminalPlatform;

/**
 * Deterministic in-memory {@link ProcessSession} for {@link TerminalManager} tests.
 * Behaves like a ConPTY session (merged output stream, {@code stderr() == null}):
 * tests drive output by calling {@code manager.handleStdout(...)} directly, while
 * the manager's own reader threads park harmlessly on the unread pipes.
 */
final class FakeProcessSession implements ProcessSession {

    private static final AtomicLong NEXT_PID = new AtomicLong(40000);

    private final TerminalPlatform platform;
    private final long pid;
    private final PipedInputStream stdoutRead;
    private final PipedOutputStream stdoutWrite;
    private final List<String> writtenLines = new CopyOnWriteArrayList<>();
    private final List<byte[]> writtenRaw = new CopyOnWriteArrayList<>();
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final AtomicBoolean destroyCalled = new AtomicBoolean(false);
    /** When true, the first destroy() leaves the session alive (close-failure test). */
    private volatile boolean failFirstDestroy;

    FakeProcessSession(TerminalPlatform platform) throws IOException {
        this.platform = platform;
        this.pid = NEXT_PID.incrementAndGet();
        this.stdoutWrite = new PipedOutputStream();
        this.stdoutRead = new PipedInputStream(stdoutWrite, 65536);
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
        if (!alive.get()) {
            throw new IOException("session dead");
        }
        writtenLines.add(line);
    }

    @Override
    public synchronized void writeRaw(byte[] bytes) throws IOException {
        if (!alive.get()) {
            throw new IOException("session dead");
        }
        writtenRaw.add(bytes.clone());
    }

    @Override
    public InputStream stdout() {
        return stdoutRead;
    }

    @Override
    public InputStream stderr() {
        return null; // ConPTY-like merged console stream
    }

    @Override
    public boolean isAlive() {
        return alive.get();
    }

    @Override
    public void destroy() {
        destroyCalled.set(true);
        if (failFirstDestroy) {
            failFirstDestroy = false; // fail once, succeed on retry
            return;
        }
        alive.set(false);
        try {
            stdoutWrite.close();
        } catch (Exception ignored) {
        }
        try {
            stdoutRead.close();
        } catch (Exception ignored) {
        }
    }

    void failFirstDestroy(boolean fail) {
        this.failFirstDestroy = fail;
    }

    void kill() {
        alive.set(false);
    }

    List<String> writtenLines() {
        return writtenLines;
    }

    List<byte[]> writtenRaw() {
        return writtenRaw;
    }

    boolean destroyCalled() {
        return destroyCalled.get();
    }

    /** Feeds console output as if the shell had printed it (goes through the pipes). */
    void print(String text) throws IOException {
        stdoutWrite.write(text.getBytes(StandardCharsets.UTF_8));
        stdoutWrite.flush();
    }
}
