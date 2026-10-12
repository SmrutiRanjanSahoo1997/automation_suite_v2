package com.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "terminal_command_log")
public class TerminalCommandLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "command", nullable = false, columnDefinition = "TEXT")
    private String command;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    public TerminalCommandLog() {
    }

    public TerminalCommandLog(String id, String terminalId, String userId, String host, String command, long timestamp) {
        this(id, terminalId, userId, host, command, null, timestamp);
    }

    public TerminalCommandLog(String id, String terminalId, String userId, String host, String command, String result, long timestamp) {
        this.id = id;
        this.terminalId = terminalId;
        this.userId = userId;
        this.host = host;
        this.command = command;
        this.result = result;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
