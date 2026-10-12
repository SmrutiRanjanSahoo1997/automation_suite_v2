CREATE TABLE IF NOT EXISTS terminal_session_log (
    id VARCHAR(36) PRIMARY KEY,
    terminal_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    host VARCHAR(255) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    current_path VARCHAR(1024),
    state VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    last_activity_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS terminal_command_log (
    id VARCHAR(36) PRIMARY KEY,
    terminal_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    host VARCHAR(255) NOT NULL,
    command TEXT NOT NULL,
    timestamp BIGINT NOT NULL
);
