package com.automation.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.automation.agent.AgentClientManager;
import com.automation.domain.TerminalSessionLog;
import com.automation.protocol.TerminalProtocol;
import com.automation.repository.TerminalCommandLogRepository;
import com.automation.repository.TerminalSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SessionLogCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(SessionLogCleanupScheduler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final long SEVEN_DAYS_MS = TimeUnit.DAYS.toMillis(7);
    private static final long TWENTY_FIVE_DAYS_MS = TimeUnit.DAYS.toMillis(25);

    private final TerminalSessionRepository sessionRepository;
    private final TerminalCommandLogRepository commandLogRepository;
    private final AgentClientManager agentClientManager;

    public SessionLogCleanupScheduler(TerminalSessionRepository sessionRepository,
                                       TerminalCommandLogRepository commandLogRepository,
                                       AgentClientManager agentClientManager) {
        this.sessionRepository = sessionRepository;
        this.commandLogRepository = commandLogRepository;
        this.agentClientManager = agentClientManager;
    }

    /** Runs every day at 02:00 AM (or 1 hour initial delay in dev) to enforce 7-day session and 25-day command retention policies. */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredLogs() {
        long now = System.currentTimeMillis();
        long sessionCutoff = now - SEVEN_DAYS_MS;
        long commandCutoff = now - TWENTY_FIVE_DAYS_MS;

        log.info("Starting background retention cleanup: sessionCutoff={} (7d), commandCutoff={} (25d)", sessionCutoff, commandCutoff);

        // 1. Clean up CLOSED sessions > 7 days old
        List<TerminalSessionLog> expiredSessions = sessionRepository.findExpiredClosedSessions(sessionCutoff);
        if (!expiredSessions.isEmpty()) {
            for (TerminalSessionLog s : expiredSessions) {
                try {
                    // Send terminal_remove frame to host agent to purge metadata from memory
                    TerminalProtocol.TerminalRemoveEvent removeEvent = new TerminalProtocol.TerminalRemoveEvent();
                    removeEvent.terminalId = s.getTerminalId();
                    String json = MAPPER.writeValueAsString(removeEvent);
                    agentClientManager.sendToAgent(s.getHost(), json);
                } catch (Exception e) {
                    log.warn("Failed to notify agent of terminal_remove for terminal {}: {}", s.getTerminalId(), e.getMessage());
                }
            }

            List<String> idsToDelete = expiredSessions.stream().map(TerminalSessionLog::getId).collect(Collectors.toList());
            sessionRepository.deleteAllByIds(idsToDelete);
            log.info("Purged {} expired CLOSED terminal session logs (>7 days)", idsToDelete.size());
        }

        // 2. Clean up command execution snapshots > 25 days old
        int deletedCommands = commandLogRepository.deleteOlderThan(commandCutoff);
        log.info("Purged {} expired command execution log snapshots (>25 days)", deletedCommands);
    }
}
