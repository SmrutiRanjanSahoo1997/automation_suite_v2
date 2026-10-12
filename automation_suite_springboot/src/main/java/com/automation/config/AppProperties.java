package com.automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agent")
public class AppProperties {

    private int defaultPort = 9010;
    private String secretKey = "zGQ3aJB3IluC5eeU";
    private long reconnectIntervalMs = 5000;

    public int getDefaultPort() { return defaultPort; }
    public void setDefaultPort(int defaultPort) { this.defaultPort = defaultPort; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public long getReconnectIntervalMs() { return reconnectIntervalMs; }
    public void setReconnectIntervalMs(long reconnectIntervalMs) { this.reconnectIntervalMs = reconnectIntervalMs; }
}
