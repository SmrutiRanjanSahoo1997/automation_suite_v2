package com.automation.agent;

public class AgentConnectionStatus {
    private String host;
    private boolean connected;
    private Long lastConnectedAt;
    private int reconnectAttempts;

    public AgentConnectionStatus() {
    }

    public AgentConnectionStatus(String host, boolean connected, Long lastConnectedAt, int reconnectAttempts) {
        this.host = host;
        this.connected = connected;
        this.lastConnectedAt = lastConnectedAt;
        this.reconnectAttempts = reconnectAttempts;
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public Long getLastConnectedAt() { return lastConnectedAt; }
    public void setLastConnectedAt(Long lastConnectedAt) { this.lastConnectedAt = lastConnectedAt; }

    public int getReconnectAttempts() { return reconnectAttempts; }
    public void setReconnectAttempts(int reconnectAttempts) { this.reconnectAttempts = reconnectAttempts; }
}
