package com.automation.agent.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads application.properties from (in order):
 * <ol>
 *   <li>{@code ./application.properties} next to the working directory (allows ops override),</li>
 *   <li>classpath {@code application.properties} bundled in resources,</li>
 *   <li>classpath {@code agent.properties} (legacy name, back-compat).</li>
 * </ol>
 * Holds only transport + terminal capacity/metadata limits. No business config.
 */
public final class AgentConfig {

    private static final Logger log = LoggerFactory.getLogger(AgentConfig.class);

    /** V2 protocol invariant: exactly 10 shared CMD/PS terminal slots. Not tunable. */
    public static final int MAX_TERMINALS = 10;

    /** V2 default endpoint when the property is missing. */
    public static final String DEFAULT_WEBSOCKET_URL = "ws://localhost:9010/ws/agent";

    private final String agentKey;
    private final int port;
    private final String websocketUrl;
    private final long reconnectInitialMs;
    private final long reconnectMaxMs;
    private final int terminalMaxCount;
    private final int maxResultChars;
    private final int maxErrorChars;
    private final int transferChunkChars;
    private final int conptyCols;
    private final int conptyRows;
    private final long stopTimeoutMs;

    AgentConfig(Properties p) {
        this.agentKey = required(p, "agent.key");
        this.port = Integer.parseInt(p.getProperty("agent.port", p.getProperty("server.port", "9010")));
        this.websocketUrl = optional(p, "agent.websocket.url", DEFAULT_WEBSOCKET_URL);
        this.reconnectInitialMs = Long.parseLong(p.getProperty("agent.websocket.reconnect-initial-ms", "1000"));
        this.reconnectMaxMs = Long.parseLong(p.getProperty("agent.websocket.reconnect-max-ms", "30000"));
        this.terminalMaxCount = Integer.parseInt(p.getProperty("terminal.max-count", "10"));
        this.maxResultChars = Integer.parseInt(p.getProperty("terminal.metadata.max-result-chars", "1000000"));
        this.maxErrorChars = Integer.parseInt(p.getProperty("terminal.metadata.max-error-chars", "1000000"));
        this.transferChunkChars = Integer.parseInt(p.getProperty("terminal.metadata.transfer-chunk-chars", "100000"));
        this.conptyCols = Integer.parseInt(p.getProperty("terminal.conpty.cols", "120"));
        this.conptyRows = Integer.parseInt(p.getProperty("terminal.conpty.rows", "30"));
        this.stopTimeoutMs = Long.parseLong(p.getProperty("terminal.stop-timeout-ms", "10000"));

        // Hard invariant: configuration must not override the 10-slot protocol contract.
        if (terminalMaxCount != MAX_TERMINALS) {
            throw new IllegalStateException(
                    "terminal.max-count must be " + MAX_TERMINALS + " (V2 invariant), got " + terminalMaxCount);
        }
        if (stopTimeoutMs <= 0) {
            throw new IllegalStateException("terminal.stop-timeout-ms must be positive, got " + stopTimeoutMs);
        }
    }

    public static AgentConfig load() throws IOException {
        Properties p = new Properties();
        Path override = Path.of("application.properties");
        if (Files.isRegularFile(override)) {
            try (InputStream in = Files.newInputStream(override)) {
                p.load(in);
            }
            log.info("Loaded application.properties from working directory override: {}", override.toAbsolutePath());
            return new AgentConfig(p);
        }
        // Classpath application.properties first, then legacy agent.properties.
        for (String name : new String[]{"application.properties", "agent.properties"}) {
            try (InputStream in = AgentConfig.class.getClassLoader().getResourceAsStream(name)) {
                if (in != null) {
                    p.load(in);
                    log.info("Loaded {} from classpath", name);
                    return new AgentConfig(p);
                }
            }
        }
        throw new IOException("Neither application.properties nor agent.properties found on classpath "
                + "and no ./application.properties override");
    }

    private static String required(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required agent property: " + key);
        }
        return v.trim();
    }

    private static String optional(Properties p, String key, String fallback) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            return fallback;
        }
        return v.trim();
    }

    public String agentKey() {
        return agentKey;
    }

    public int port() {
        return port;
    }

    public String websocketUrl() {
        return websocketUrl;
    }

    public long reconnectInitialMs() {
        return reconnectInitialMs;
    }

    public long reconnectMaxMs() {
        return reconnectMaxMs;
    }

    public int terminalMaxCount() {
        return terminalMaxCount;
    }

    public int maxResultChars() {
        return maxResultChars;
    }

    public int maxErrorChars() {
        return maxErrorChars;
    }

    public int transferChunkChars() {
        return transferChunkChars;
    }

    public int conptyCols() {
        return conptyCols;
    }

    public int conptyRows() {
        return conptyRows;
    }

    /**
     * Bound for the STOP command-boundary wait: after Ctrl+C, the terminal stays
     * RUNNING until the queued marker boundary arrives or this timeout fires.
     */
    public long stopTimeoutMs() {
        return stopTimeoutMs;
    }

    /** Test seam: build a config from explicit properties (same validation as production). */
    public static AgentConfig fromProperties(Properties p) {
        return new AgentConfig(p);
    }
}