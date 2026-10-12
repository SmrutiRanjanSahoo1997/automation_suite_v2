package com.automation.mssql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.mssql.config.MssqlAgentConfig;
import com.automation.mssql.executor.MssqlPersistentTerminal;
import com.automation.mssql.queue.MssqlQueueManager;
import com.automation.mssql.server.MssqlWebSocketServer;

public class MssqlAgentApplication {

    private static final Logger log = LoggerFactory.getLogger(MssqlAgentApplication.class);

    public static void main(String[] args) {
        log.info("Starting AutomationSuite MSSQL Agent Service...");

        try {
            MssqlAgentConfig config = new MssqlAgentConfig();

            // 1. Start persistent terminal
            MssqlPersistentTerminal terminal = new MssqlPersistentTerminal();
            terminal.start();

            // 2. Start FIFO queue manager
            MssqlQueueManager queueManager = new MssqlQueueManager(terminal);
            queueManager.start();

            // 3. Start WebSocket server on configured port (9012)
            MssqlWebSocketServer server = new MssqlWebSocketServer(config.getPort(), queueManager);
            server.start();

            // 4. Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down MSSQL Agent Service...");
                try {
                    server.stop();
                } catch (Exception ignored) {}
                queueManager.stop();
                terminal.stop();
                log.info("MSSQL Agent Service stopped successfully.");
            }, "mssql-agent-shutdown"));

            log.info("MSSQL Agent Service initialized and listening on port {}", config.getPort());

        } catch (Exception e) {
            log.error("Fatal error during MSSQL Agent startup: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
