package com.automation.mssql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mssql_query_audit_logs")
public class MssqlQueryAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "host")
    private String host;

    @Column(name = "input_sql", nullable = false, columnDefinition = "TEXT")
    private String inputSql;

    @Column(name = "executed_at", nullable = false)
    private long executedAt;

    public MssqlQueryAuditLog() {}

    public MssqlQueryAuditLog(String taskId, String userId, String environment, String host, String inputSql, long executedAt) {
        this.taskId = taskId;
        this.userId = userId;
        this.environment = environment;
        this.host = host;
        this.inputSql = inputSql;
        this.executedAt = executedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getInputSql() { return inputSql; }
    public void setInputSql(String inputSql) { this.inputSql = inputSql; }

    public long getExecutedAt() { return executedAt; }
    public void setExecutedAt(long executedAt) { this.executedAt = executedAt; }
}
