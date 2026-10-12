/**
 * Central Catalog of ALL WebSocket Event Name Constants.
 * Carries all AI and Terminal event wire names.
 */
export const WS_EVENTS = {
  // AI Events
  AI_EXECUTE_TASK: 'AI_EXECUTE_TASK',
  AI_TASK_RESULT: 'AI_TASK_RESULT',
  AI_DELETE_REQ: 'AI_DELETE_REQ',
  AI_DELETE_RESP: 'AI_DELETE_RESP',
  AI_HEALTH_STATUS: 'ai_health_status',
  AI_RUNBOOKS_ROOT_PATH: 'AI_RUNBOOKS_ROOT_PATH',

  // Terminal Events
  TERMINAL_START: 'terminal_start',
  TERMINAL_EXECUTE: 'terminal_execute',
  TERMINAL_STOP: 'terminal_stop',
  TERMINAL_CLOSE: 'terminal_close',
  TERMINAL_RESULT: 'terminal_result',
  TERMINAL_ERROR: 'terminal_error',
  TERMINAL_STATUS: 'terminal_status',
  TERMINAL_HISTORY: 'terminal_history',
  METADATA_LIST: 'metadata_list',
  METADATA_LAST_RESULT: 'metadata_lastResult',
  METADATA_LAST_ERROR: 'metadata_lastError',
  TERMINAL_REMOVE: 'terminal_remove',
} as const;

export type WsEventType = typeof WS_EVENTS[keyof typeof WS_EVENTS];
