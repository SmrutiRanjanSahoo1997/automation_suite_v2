package com.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "terminal_session_log")
public class TerminalSessionLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "current_path", length = 1024)
    private String currentPath;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private long lastActivityAt;

    public TerminalSessionLog() {
    }

    public TerminalSessionLog(String id, String terminalId, String userId, String name, String host,
                              String platform, String currentPath, String state, long createdAt, long lastActivityAt) {
        this.id = id;
        this.terminalId = terminalId;
        this.userId = userId;
        this.name = name;
        this.host = host;
        this.platform = platform;
        this.currentPath = currentPath;
        this.state = state;
        this.createdAt = createdAt;
        this.lastActivityAt = lastActivityAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getCurrentPath() { return currentPath; }
    public void setCurrentPath(String currentPath) { this.currentPath = currentPath; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(long lastActivityAt) { this.lastActivityAt = lastActivityAt; }
}
