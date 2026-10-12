// Centralized Event Constants for Copilot AI Agent Extension
// Strictly matches Java EventConstants.java

const EventConstants = {
  // Configurable Server Host & Endpoints
  AI_AGENT_WS_HOST: "127.0.0.1:9020",
  get EXTSN_WS_URL() {
    return `ws://${this.AI_AGENT_WS_HOST}/ws/extension`;
  },
  get AI_AGENT_WS_URL() {
    return `ws://${this.AI_AGENT_WS_HOST}/ws/ai_agent`;
  },

  // Extension Heartbeat
  EXTSN_PING: "EXTSN_PING",
  EXTSN_PONG: "EXTSN_PONG",

  // AI Agent Health Status pushed to SpringBoot
  AI_HEALTH: "AI_HEALTH",

  // Task Commands & Results
  EXECUTE_TASK: "EXECUTE_TASK",
  TASK_RESULT: "TASK_RESULT",

  // Delete Conversation Events
  EXECUTE_DELETE: "EXECUTE_DELETE",
  DELETE_RESPONSE: "DELETE_RESPONSE",
  EXTSN_DELETE: "EXTSN_DELETE",
  EXTSN_DELETE_RESPONSE: "EXTSN_DELETE_RESPONSE",

  // Minimization Toggle Command
  SET_MINIMIZABLE: "SET_MINIMIZABLE",

  // Commands to Extension
  EXTSN_CHAT: "EXTSN_CHAT",
  EXTSN_SENT: "EXTSN_SENT",
  EXTSN_CHAT_RESPONSE: "EXTSN_CHAT_RESPONSE",

  // File Processing Events
  EXTSN_FILE_REQUEST: "EXTSN_FILE_REQUEST",
  EXTSN_FILE_RESPONSE: "EXTSN_FILE_RESPONSE",

  // Verification Events (PyAutoGUI)
  EXTSN_VERIFICATION_REQUIRED: "EXTSN_VERIFICATION_REQUIRED",
  EXTSN_VERIFICATION_RESOLVED: "EXTSN_VERIFICATION_RESOLVED",

  // Status Values
  STATUS_OK: "OK",
  STATUS_NOT_OK: "NOT_OK",
  STATUS_RESPONSE: "RESPONSE",
  STATUS_FAILED: "FAILED"
};

if (typeof module !== "undefined" && module.exports) {
  module.exports = EventConstants;
}
