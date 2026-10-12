package com.automation.mssql.config;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MssqlAgentConfig {

    private static final Logger log = LoggerFactory.getLogger(MssqlAgentConfig.class);

    private int port = 9012;

    public MssqlAgentConfig() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("agent.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            log.debug("No agent.properties found or failed to load: {}", e.getMessage());
        }

        String portProp = System.getProperty("agent.port", System.getenv("AGENT_PORT"));
        if (portProp == null) {
            portProp = props.getProperty("agent.port", "9012");
        }

        try {
            this.port = Integer.parseInt(portProp.trim());
        } catch (NumberFormatException e) {
            this.port = 9012;
        }

        log.info("MSSQL Agent configured with port: {}", this.port);
    }

    public int getPort() {
        return port;
    }
}
