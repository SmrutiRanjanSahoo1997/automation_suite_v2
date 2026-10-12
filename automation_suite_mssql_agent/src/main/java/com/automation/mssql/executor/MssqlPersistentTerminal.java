package com.automation.mssql.executor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MssqlPersistentTerminal {

    private static final Logger log = LoggerFactory.getLogger(MssqlPersistentTerminal.class);

    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Thread outputThread;
    private volatile boolean running = true;
    private volatile Consumer<String> currentOutputHandler = null;

    public synchronized void start() throws Exception {
        log.info("Starting persistent PowerShell terminal (elevated=false)...");
        ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoLogo",
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy",
                "Bypass"
        );
        pb.redirectErrorStream(true);
        this.process = pb.start();

        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

        this.outputThread = new Thread(this::readLoop, "mssql-pty-reader");
        this.outputThread.setDaemon(true);
        this.outputThread.start();

        log.info("Persistent PowerShell terminal started successfully (pid={})", process.pid());
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                Consumer<String> handler = this.currentOutputHandler;
                if (handler != null) {
                    try {
                        handler.accept(line);
                    } catch (Exception e) {
                        log.warn("Error in output handler: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                log.error("Exception in persistent terminal reader loop: {}", e.getMessage());
            }
        }
    }

    public synchronized void executeScript(String scriptContent, String taskMarker, Consumer<String> lineConsumer) throws Exception {
        if (process == null || !process.isAlive()) {
            throw new IllegalStateException("PowerShell persistent terminal process is not alive.");
        }

        CountDownLatch latch = new CountDownLatch(1);

        this.currentOutputHandler = line -> {
            if (line.contains("---END_" + taskMarker + "---")) {
                latch.countDown();
            } else {
                lineConsumer.accept(line);
            }
        };

        try {
            // Write payload script wrapped with marker and executed
            writer.write(scriptContent);
            writer.newLine();
            writer.write("[Console]::Out.WriteLine('---END_" + taskMarker + "---'); [Console]::Out.Flush();");
            writer.newLine();
            writer.flush();

            // Wait for script execution completion (or 10 minutes timeout)
            boolean completed = latch.await(10, TimeUnit.MINUTES);
            if (!completed) {
                log.warn("Task {} timed out waiting for EOF marker.", taskMarker);
            }
        } finally {
            this.currentOutputHandler = null;
        }
    }

    public synchronized void stop() {
        running = false;
        try {
            if (writer != null) {
                writer.write("exit\n");
                writer.flush();
            }
            if (process != null) {
                process.destroy();
                process.waitFor(3, TimeUnit.SECONDS);
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }
        } catch (Exception e) {
            log.warn("Error stopping persistent terminal: {}", e.getMessage());
        }
    }
}
