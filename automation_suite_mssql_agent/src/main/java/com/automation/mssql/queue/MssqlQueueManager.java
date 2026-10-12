package com.automation.mssql.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.mssql.executor.MssqlPersistentTerminal;
import com.automation.mssql.executor.MssqlScriptGenerator;
import com.automation.mssql.model.MssqlTask;

public class MssqlQueueManager {

    private static final Logger log = LoggerFactory.getLogger(MssqlQueueManager.class);

    private final BlockingQueue<MssqlTask> queue = new LinkedBlockingQueue<>();
    private final MssqlPersistentTerminal terminal;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile MssqlTask activeTask = null;
    private Thread workerThread;

    public MssqlQueueManager(MssqlPersistentTerminal terminal) {
        this.terminal = terminal;
    }

    public void start() {
        this.workerThread = new Thread(this::workerLoop, "mssql-queue-worker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
        log.info("MSSQL Queue Manager started. Ready for sequential query processing.");
    }

    public void enqueue(MssqlTask task) {
        if (task != null) {
            queue.offer(task);
            log.info("Enqueued MSSQL task: id={} type={} queueSize={}", task.getTaskId(), task.getEventType(), queue.size());
        }
    }

    public int getQueueCount() {
        return queue.size() + (activeTask != null ? 1 : 0);
    }

    public String getActiveTaskId() {
        MssqlTask curr = this.activeTask;
        return curr != null ? curr.getTaskId() : null;
    }

    private void workerLoop() {
        while (running.get()) {
            try {
                MssqlTask task = queue.take();
                this.activeTask = task;

                try {
                    processTask(task);
                } catch (Exception e) {
                    log.error("Error executing task {}: {}", task.getTaskId(), e.getMessage(), e);
                    sendErrorToClient(task, e.getMessage());
                } finally {
                    this.activeTask = null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Unexpected error in worker loop: {}", e.getMessage(), e);
            }
        }
    }

    private void processTask(MssqlTask task) throws Exception {
        log.info("Processing MSSQL task: id={} type={}", task.getTaskId(), task.getEventType());
        String script;

        switch (task.getEventType().toUpperCase()) {
            case "EXECUTE_QUERY" -> script = MssqlScriptGenerator.buildExecuteQueryScript(task.getEnvironment(), task.getSql());
            case "GET_DATABASES" -> script = MssqlScriptGenerator.buildGetDatabasesScript(task.getEnvironment());
            case "GET_TABLES" -> script = MssqlScriptGenerator.buildGetTablesScript(task.getEnvironment(), task.getDatabase());
            case "GET_GENERAL_QUERY" -> {
                String safeDb = task.getDatabase() != null ? task.getDatabase() : "dbo";
                String safeTable = task.getTable() != null ? task.getTable() : "Table";
                String genSql = "SELECT TOP (1000) * FROM [" + safeDb + "].[dbo].[" + safeTable + "];";
                String json = String.format("{\"type\":\"generalQuery\",\"env\":\"%s\",\"database\":\"%s\",\"table\":\"%s\",\"sql\":\"%s\"}",
                        task.getEnvironment(), safeDb, safeTable, genSql.replace("\"", "\\\""));
                sendToClient(task, json);
                return;
            }
            default -> {
                sendErrorToClient(task, "Unknown event type: " + task.getEventType());
                return;
            }
        }

        terminal.executeScript(script, task.getTaskId(), line -> {
            sendToClient(task, line);
        });
    }

    private void sendToClient(MssqlTask task, String message) {
        if (task.getClient() != null && task.getClient().isOpen()) {
            try {
                task.getClient().send(message);
            } catch (Exception e) {
                log.warn("Failed to send frame to client for task {}: {}", task.getTaskId(), e.getMessage());
            }
        }
    }

    private void sendErrorToClient(MssqlTask task, String errorMsg) {
        String json = String.format("{\"type\":\"output\",\"taskId\":\"%s\",\"success\":false,\"error\":\"%s\",\"end\":true}",
                task.getTaskId(), errorMsg != null ? errorMsg.replace("\"", "\\\"") : "Unknown error");
        sendToClient(task, json);
    }

    public void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }
}
