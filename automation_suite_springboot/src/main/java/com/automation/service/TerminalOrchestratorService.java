package com.automation.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.automation.agent.AgentClientManager;
import com.automation.domain.TerminalCommandLog;
import com.automation.domain.TerminalSessionLog;
import com.automation.protocol.TerminalProtocol.TerminalEventType;
import com.automation.repository.TerminalCommandLogRepository;
import com.automation.repository.TerminalSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Terminal Orchestrator Service.
 * Dedicated solely to terminal feature management (PTY execution, server agent communication,
 * command log repositories, session retention cleanup).
 * Implements UiWebSocketListener for processing terminal wire events.
 */
@Service
public class TerminalOrchestratorService implements GlobalUiWebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(TerminalOrchestratorService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AgentClientManager agentClientManager;
    private final TerminalSessionRepository sessionRepository;
    private final TerminalCommandLogRepository commandLogRepository;
    private final GlobalUiWebSocketChannel globalUiWebSocketChannel;

    public TerminalOrchestratorService(AgentClientManager agentClientManager,
                                       TerminalSessionRepository sessionRepository,
                                       TerminalCommandLogRepository commandLogRepository,
                                       GlobalUiWebSocketChannel globalUiWebSocketChannel) {
        this.agentClientManager = agentClientManager;
        this.sessionRepository = sessionRepository;
        this.commandLogRepository = commandLogRepository;
        this.globalUiWebSocketChannel = globalUiWebSocketChannel;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("Registering TerminalOrchestratorService with GlobalUiWebSocketChannel");
        globalUiWebSocketChannel.registerListener(this);
    }

    // =========================================================================
    // GlobalUiWebSocketListener Implementation (Terminal Wire Events Only)
    // =========================================================================

    @Override
    @Transactional
    public void onUiMessage(WebSocketSession session, String eventType, JsonNode root, String rawMessage) {
        TerminalEventType type = TerminalEventType.fromWire(eventType);
        if (type == null) {
            // Not a terminal wire event, ignore silently
            return;
        }

        String terminalId = root.path("terminalid").asText(null);
        String host = root.path("host").asText("localhost");
        String userId = root.path("userId").asText("user");

        long now = System.currentTimeMillis();

        switch (type) {
            case TERMINAL_START -> {
                String name = root.path("name").asText(host + " Terminal");
                String platform = root.path("platform").asText("PS");

                TerminalSessionLog sessionLog = sessionRepository.findByTerminalId(terminalId)
                        .orElseGet(() -> new TerminalSessionLog(
                                UUID.randomUUID().toString(),
                                terminalId,
                                userId,
                                name,
                                host,
                                platform,
                                "C:\\",
                                "IDLE",
                                now,
                                now
                        ));
                sessionLog.setState("IDLE");
                sessionLog.setLastActivityAt(now);
                sessionRepository.save(sessionLog);

                agentClientManager.sendToAgent(host, rawMessage);
            }
            case TERMINAL_EXECUTE -> {
                String command = root.path("command").asText("");
                sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                    s.setState("RUNNING");
                    s.setLastActivityAt(now);
                    sessionRepository.save(s);
                });

                // Save command audit log snapshot
                TerminalCommandLog cmdLog = new TerminalCommandLog(
                        UUID.randomUUID().toString(),
                        terminalId,
                        userId,
                        host,
                        command,
                        now
                );
                commandLogRepository.saveAndFlush(cmdLog);

                agentClientManager.sendToAgent(host, rawMessage);
            }
            case TERMINAL_STOP -> {
                sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                    s.setLastActivityAt(now);
                    sessionRepository.saveAndFlush(s);
                });
                agentClientManager.sendToAgent(host, rawMessage);
            }
            case TERMINAL_CLOSE -> {
                sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                    s.setState("CLOSED");
                    s.setLastActivityAt(now);
                    sessionRepository.saveAndFlush(s);
                });
                agentClientManager.sendToAgent(host, rawMessage);
            }
            case TERMINAL_HISTORY -> {
                if (terminalId != null && session != null) {
                    sendTerminalHistory(session, terminalId);
                }
            }
            default -> agentClientManager.sendToAgent(host, rawMessage);
        }
    }

    // =========================================================================
    // Target Agent Output Handler (Agent -> UI Relay)
    // =========================================================================

    @Transactional
    public void handleAgentMessage(String host, String messageJson) {
        try {
            JsonNode root = MAPPER.readTree(messageJson);
            String wire = root.path("event-type").asText(null);
            TerminalEventType type = TerminalEventType.fromWire(wire);

            if (type == TerminalEventType.TERMINAL_STATUS) {
                String terminalId = root.path("terminalid").asText(root.path("terminalId").asText(null));
                String state = root.path("state").asText(null);
                String currentPath = root.path("currentPath").asText(null);
                long now = System.currentTimeMillis();
                long lastOutput = root.path("lastOutputTimestamp").asLong(0);
                long lastInput = root.path("lastInputTimestamp").asLong(0);
                long lastAct = lastOutput > 0 ? lastOutput : (lastInput > 0 ? lastInput : now);

                if (terminalId != null && state != null) {
                    sessionRepository.findByTerminalId(terminalId).ifPresent(s -> {
                        s.setState(state);
                        if (currentPath != null) {
                            s.setCurrentPath(currentPath);
                        }
                        s.setLastActivityAt(lastAct);
                        sessionRepository.saveAndFlush(s);
                    });
                }
            } else if (type == TerminalEventType.TERMINAL_RESULT) {
                String terminalId = root.path("terminalid").asText(root.path("terminalId").asText(null));
                String chunk = root.path("chunk").asText(null);
                if (terminalId != null && chunk != null && !chunk.isEmpty()) {
                    commandLogRepository.findTopByTerminalIdOrderByTimestampDesc(terminalId).ifPresent(cmd -> {
                        String current = cmd.getResult() == null ? "" : cmd.getResult();
                        cmd.setResult(current + chunk);
                        commandLogRepository.saveAndFlush(cmd);
                    });
                }
            } else if (type == TerminalEventType.METADATA_LIST) {
                if (root instanceof com.fasterxml.jackson.databind.node.ObjectNode rootNode) {
                    rootNode.put("host", host);
                    JsonNode termArray = rootNode.path("terminals");
                    com.fasterxml.jackson.databind.node.ArrayNode arrayNode;
                    if (termArray.isArray() && termArray instanceof com.fasterxml.jackson.databind.node.ArrayNode an) {
                        arrayNode = an;
                    } else {
                        arrayNode = MAPPER.createArrayNode();
                        rootNode.set("terminals", arrayNode);
                    }

                    long now = System.currentTimeMillis();
                    java.util.Set<String> seenIds = new java.util.HashSet<>();

                    for (JsonNode t : arrayNode) {
                        if (t instanceof com.fasterxml.jackson.databind.node.ObjectNode termNode) {
                            termNode.put("host", host);
                            String terminalId = termNode.path("terminalId").asText(termNode.path("terminalid").asText(null));
                            if (terminalId != null) {
                                seenIds.add(terminalId);
                                String name = termNode.path("name").asText(null);
                                String platform = termNode.path("platform").asText("PS");
                                String state = termNode.path("state").asText("IDLE");
                                String currentPath = termNode.path("currentPath").asText("C:\\");
                                long lastOutput = termNode.path("lastOutputTimestamp").asLong(0);
                                long lastInput = termNode.path("lastInputTimestamp").asLong(0);
                                long lastAct = lastOutput > 0 ? lastOutput : (lastInput > 0 ? lastInput : now);

                                TerminalSessionLog sessionLog = sessionRepository.findByTerminalId(terminalId)
                                        .orElseGet(() -> new TerminalSessionLog(
                                                UUID.randomUUID().toString(),
                                                terminalId,
                                                "user",
                                                name != null ? name : terminalId,
                                                host,
                                                platform,
                                                currentPath,
                                                state,
                                                now,
                                                lastAct
                                        ));
                                sessionLog.setState(state);
                                sessionLog.setCurrentPath(currentPath);
                                sessionLog.setLastActivityAt(lastAct);
                                sessionRepository.save(sessionLog);

                                termNode.put("name", sessionLog.getName());
                            }
                        }
                    }

                    // Merge historical DB terminal sessions for this host
                    List<TerminalSessionLog> dbSessions = sessionRepository.findByHost(host);
                    for (TerminalSessionLog dbSess : dbSessions) {
                        String dbTermId = dbSess.getTerminalId();
                        if (dbTermId != null && dbTermId.contains("_ASR_AI_")) {
                            boolean isCanonical = dbTermId.matches("^.+_ASR_AI_(?:PS|CMD)_.+$");
                            if (!isCanonical) {
                                sessionRepository.delete(dbSess);
                                continue;
                            }
                        }
                        if (!seenIds.contains(dbSess.getTerminalId())) {
                            seenIds.add(dbSess.getTerminalId());
                            com.fasterxml.jackson.databind.node.ObjectNode dbNode = MAPPER.createObjectNode();
                            dbNode.put("terminalId", dbSess.getTerminalId());
                            dbNode.put("host", host);
                            dbNode.put("name", dbSess.getName());
                            dbNode.put("platform", dbSess.getPlatform());
                            dbNode.put("state", dbSess.getState());
                            dbNode.put("currentPath", dbSess.getCurrentPath());
                            dbNode.put("lastActivityAt", dbSess.getLastActivityAt());
                            dbNode.put("elevated", false);
                            arrayNode.add(dbNode);
                        }
                    }

                    messageJson = MAPPER.writeValueAsString(rootNode);
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing agent message for status sync: {}", e.getMessage());
        }

        // Broadcast terminal result to connected UI clients
        globalUiWebSocketChannel.broadcastToUi(messageJson);
    }

    public void sendTerminalHistory(WebSocketSession uiSession, String terminalId) {
        List<TerminalCommandLog> cmdLogs = commandLogRepository.findByTerminalIdOrderByTimestampAsc(terminalId);
        Optional<TerminalSessionLog> sess = sessionRepository.findByTerminalId(terminalId);
        String platform = sess.map(TerminalSessionLog::getPlatform).orElse("PS");
        String currentPath = sess.map(TerminalSessionLog::getCurrentPath).orElse("C:\\");

        com.fasterxml.jackson.databind.node.ArrayNode linesArr = MAPPER.createArrayNode();
        for (TerminalCommandLog cmd : cmdLogs) {
            String path = currentPath != null && !currentPath.isBlank() ? currentPath : "C:\\";
            String promptPrefix = "PS".equalsIgnoreCase(platform) ? "PS " + path + ">" : path + ">";
            if (cmd.getCommand() != null && !cmd.getCommand().isBlank()) {
                linesArr.add(promptPrefix + " " + cmd.getCommand());
            }
            if (cmd.getResult() != null && !cmd.getResult().isBlank()) {
                String[] parts = cmd.getResult().replace("\r\n", "\n").replace("\r", "\n").split("\n");
                for (String part : parts) {
                    linesArr.add(part);
                }
            } else if (cmd.getResult() == null && cmd.getCommand() != null && !cmd.getCommand().isBlank()) {
                long now = System.currentTimeMillis();
                if (now - cmd.getTimestamp() >= 4L * 24 * 60 * 60 * 1000) {
                    linesArr.add("[Output pruned (>4 days)]");
                }
            }
        }

        com.fasterxml.jackson.databind.node.ObjectNode resp = MAPPER.createObjectNode();
        resp.put("event-type", "terminal_history");
        resp.put("terminalid", terminalId);
        resp.set("lines", linesArr);

        globalUiWebSocketChannel.sendToSession(uiSession, resp.toString());
    }

    /**
     * Retention Cleanup:
     * 1. Nullify output text > 4 days.
     * 2. Delete command logs > 15 days.
     * 3. Delete expired closed sessions > 15 days.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        long fourDaysAgo = now - (4L * 24 * 60 * 60 * 1000);
        long fifteenDaysAgo = now - (15L * 24 * 60 * 60 * 1000);

        int nullified = commandLogRepository.nullifyResultsOlderThan(fourDaysAgo);
        if (nullified > 0) {
            log.info("Nullified execution results for {} command logs older than 4 days", nullified);
        }

        int deletedCmds = commandLogRepository.deleteOlderThan(fifteenDaysAgo);
        if (deletedCmds > 0) {
            log.info("Purged {} command log transaction rows older than 15 days", deletedCmds);
        }

        List<TerminalSessionLog> expired = sessionRepository.findExpiredClosedSessions(fifteenDaysAgo);
        if (!expired.isEmpty()) {
            for (TerminalSessionLog s : expired) {
                commandLogRepository.deleteByTerminalId(s.getTerminalId());
                String removeMsg = String.format("{\"event-type\":\"terminal_remove\",\"terminalid\":\"%s\",\"host\":\"%s\"}", s.getTerminalId(), s.getHost());
                agentClientManager.sendToAgent(s.getHost(), removeMsg);
            }
            List<String> ids = expired.stream().map(TerminalSessionLog::getId).toList();
            sessionRepository.deleteAllByIds(ids);
            log.info("Cleaned up {} expired closed terminal sessions older than 15 days", ids.size());
        }
    }

    @Transactional
    public boolean renameTerminalSession(String terminalId, String newName) {
        return sessionRepository.findByTerminalId(terminalId).map(s -> {
            s.setName(newName);
            s.setLastActivityAt(System.currentTimeMillis());
            sessionRepository.save(s);
            log.info("Renamed terminal session {} to {}", terminalId, newName);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void handleUiMessageDirect(String payload) {
        try {
            JsonNode root = MAPPER.readTree(payload);
            String wire = root.path("event-type").asText(root.path("event").asText(""));
            onUiMessage(null, wire, root, payload);
        } catch (Exception e) {
            log.error("Error in handleUiMessageDirect: {}", e.getMessage(), e);
        }
    }
}
