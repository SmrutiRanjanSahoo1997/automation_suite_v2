package com.automation.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automation.agent.config.AgentConfig;
import com.automation.agent.terminal.TerminalManager;
import com.automation.agent.websocket.AgentWebSocket;

/**
 * Application bootstrap: load config → create manager → start the embedded
 * WebSocket server. Clients (Spring Boot, HTML Console, CLI tools) connect TO
 * this agent on port 9010.
 */
public final class AgentMain {

    private static final Logger log = LoggerFactory.getLogger(AgentMain.class);

    private AgentMain() {
    }

    public static void main(String[] args) throws Exception {
        AgentConfig config = AgentConfig.load();
        int port = config.port();
        log.info("Automation Suite Agent V2 starting. Listening on port {} (maxTerminals={})",
                port, config.terminalMaxCount());

        TerminalManager manager = new TerminalManager(config);
        AgentWebSocket server = new AgentWebSocket(port, config.agentKey(), manager);
        manager.setSink(server);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Agent shutting down; terminating active terminals");
            server.shutdown();
            manager.closeAll();
            manager.shutdown();
        }, "agent-shutdown"));

        server.start();

        log.info("Agent Server ready! Clients can connect to ws://localhost:{}/ws/agent", port);

        // Keep main thread alive while server runs.
        Thread.currentThread().join();
    }
}