package com.automation.aiagent.constants;

/**
 * Centralized Event Constants for WebSocket Communication.
 * Guaranteed no string typos across AI Agent, SpringBoot, and Chrome Extension.
 */
public class EventConstants {

    // Extension Heartbeat
    public static final String EXTSN_PING = "EXTSN_PING";
    public static final String EXTSN_PONG = "EXTSN_PONG";

    // AI Agent Health Status pushed to SpringBoot
    public static final String AI_HEALTH = "AI_HEALTH";

    // Task Commands & Results
    public static final String EXECUTE_TASK = "EXECUTE_TASK";
    public static final String TASK_RESULT = "TASK_RESULT";

    // Delete Conversation Events
    public static final String EXECUTE_DELETE = "EXECUTE_DELETE";
    public static final String DELETE_RESPONSE = "DELETE_RESPONSE";
    public static final String EXTSN_DELETE = "EXTSN_DELETE";
    public static final String EXTSN_DELETE_RESPONSE = "EXTSN_DELETE_RESPONSE";

    // Minimization Toggle Command
    public static final String SET_MINIMIZABLE = "SET_MINIMIZABLE";

    // Commands to Extension
    public static final String EXTSN_CHAT = "EXTSN_CHAT";
    public static final String EXTSN_SENT = "EXTSN_SENT";
    public static final String EXTSN_CHAT_RESPONSE = "EXTSN_CHAT_RESPONSE";

    // File Processing Events
    public static final String EXTSN_FILE_REQUEST = "EXTSN_FILE_REQUEST";
    public static final String EXTSN_FILE_RESPONSE = "EXTSN_FILE_RESPONSE";

    // Verification Events (PyAutoGUI)
    public static final String EXTSN_VERIFICATION_REQUIRED = "EXTSN_VERIFICATION_REQUIRED";
    public static final String EXTSN_VERIFICATION_RESOLVED = "EXTSN_VERIFICATION_RESOLVED";

    // Runbooks Events
    public static final String AI_RUNBOOKS_ROOT_PATH = "AI_RUNBOOKS_ROOT_PATH";

    // Status Values
    public static final String STATUS_OK = "OK";
    public static final String STATUS_NOT_OK = "NOT_OK";
    public static final String STATUS_RESPONSE = "RESPONSE";
    public static final String STATUS_FAILED = "FAILED";

    private EventConstants() {
        // Private constructor to prevent instantiation
    }
}
