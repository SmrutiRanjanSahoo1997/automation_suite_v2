package com.automation.mssql.model;

import org.java_websocket.WebSocket;

public class MssqlTask {

    private final String taskId;
    private final String eventType;
    private final String userId;
    private final String environment;
    private final String database;
    private final String table;
    private final String sql;
    private final WebSocket client;

    public MssqlTask(String taskId, String eventType, String userId, String environment,
                     String database, String table, String sql, WebSocket client) {
        this.taskId = taskId;
        this.eventType = eventType;
        this.userId = userId;
        this.environment = environment != null ? environment.toLowerCase() : "prod";
        this.database = database;
        this.table = table;
        this.sql = sql;
        this.client = client;
    }

    public String getTaskId() { return taskId; }
    public String getEventType() { return eventType; }
    public String getUserId() { return userId; }
    public String getEnvironment() { return environment; }
    public String getDatabase() { return database; }
    public String getTable() { return table; }
    public String getSql() { return sql; }
    public WebSocket getClient() { return client; }
}
