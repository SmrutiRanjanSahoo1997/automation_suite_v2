package com.automation.mssql.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.automation.mssql.repository.MssqlQueryAuditLogRepository;

@Component
public class MssqlAuditLogCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(MssqlAuditLogCleanupTask.class);
    private static final long FIFTEEN_DAYS_MS = 15L * 24 * 60 * 60 * 1000;

    private final MssqlQueryAuditLogRepository repository;

    public MssqlAuditLogCleanupTask(MssqlQueryAuditLogRepository repository) {
        this.repository = repository;
    }

    // Runs every day at 02:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldAuditLogs() {
        long cutoff = System.currentTimeMillis() - FIFTEEN_DAYS_MS;
        log.info("Starting MSSQL audit log cleanup for records older than 15 days (cutoff: {})...", cutoff);
        try {
            repository.deleteByExecutedAtBefore(cutoff);
            log.info("MSSQL audit log cleanup completed successfully.");
        } catch (Exception e) {
            log.error("Failed to cleanup old MSSQL audit logs: {}", e.getMessage(), e);
        }
    }
}
