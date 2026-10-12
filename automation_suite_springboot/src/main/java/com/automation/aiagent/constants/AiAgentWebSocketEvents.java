package com.automation.aiagent.constants;

/**
 * Central catalog of AI Agent Event Constants (Port 9020) & UI Wire Event Names.
 */
public class AiAgentWebSocketEvents {

    // AI Agent Server Wire Events (Port 9020)
    public static final String AGENT_EXECUTE_TASK = "EXECUTE_TASK";
    public static final String AGENT_EXECUTE_DELETE = "EXECUTE_DELETE";
    public static final String AGENT_TASK_RESULT = "TASK_RESULT";
    public static final String AGENT_DELETE_RESPONSE = "DELETE_RESPONSE";
    public static final String AGENT_AI_HEALTH = "AI_HEALTH";
    public static final String AGENT_EXTSN_PING = "EXTSN_PING";
    public static final String AGENT_EXTSN_PONG = "EXTSN_PONG";

    public static final String AGENT_AI_RUNBOOKS_ROOT_PATH = "AI_RUNBOOKS_ROOT_PATH";

    // React UI Wire Events (/ws/app)
    public static final String UI_AI_EXECUTE_TASK = "AI_EXECUTE_TASK";
    public static final String UI_AI_TASK_RESULT = "AI_TASK_RESULT";
    public static final String UI_AI_DELETE_REQ = "AI_DELETE_REQ";
    public static final String UI_AI_DELETE_RESP = "AI_DELETE_RESP";
    public static final String UI_AI_HEALTH_STATUS = "ai_health_status";
    public static final String UI_AI_RUNBOOKS_ROOT_PATH = "AI_RUNBOOKS_ROOT_PATH";

    // Status Values
    public static final String STATUS_OK = "OK";
    public static final String STATUS_NOT_OK = "NOT_OK";
    public static final String STATUS_RESPONSE = "RESPONSE";
    public static final String STATUS_FAILED = "FAILED";

    private AiAgentWebSocketEvents() {
        // Prevent instantiation
    }
}
