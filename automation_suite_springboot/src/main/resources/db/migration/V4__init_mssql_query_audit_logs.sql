CREATE TABLE IF NOT EXISTS mssql_query_audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    host VARCHAR(255),
    input_sql TEXT NOT NULL,
    executed_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_mssql_audit_user_id ON mssql_query_audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_mssql_audit_executed_at ON mssql_query_audit_logs(executed_at);
