import React, { useState, useEffect, useRef } from 'react';
import { TopControlBar } from './components/TopControlBar';
import { ChatThread } from './components/ChatThread';
import { InputBox } from './components/InputBox';
import { ConversationList } from './components/ConversationList';
import { ServerConfig } from '../terminal/types/terminal.types';
import { fetchServerInventory } from '../../network/api';
import { globalWebSocket, GlobalWebSocketListener } from '../../network/globalWebSocket';
import { WS_EVENTS } from '../../constants/globalEventConstants';
import { logger } from '../../utils/logger';
import { safeParseAiJson } from '../../utils/jsonRepair';
import {
  AiConversationRecord,
  getAllAiConversations,
  saveAiConversation,
  deleteAiConversationRepo,
} from './repository/aiConversationRepo';
import {
  AiChatMessageRecord,
  getAiMessagesByConversation,
  saveAiChatMessage,
  deleteAiMessagesByConversation,
} from './repository/aiMessageRepo';
import { PromptBuilder } from './services/prompt_building/PromptBuilder';
import { ContextAccumulatorService, ConversationTurnRecord } from './services/ContextAccumulatorService';
import { getActiveAuthUserInfo } from '../auth/repository/authRepo';
import { databaseMssqlService } from '../database/services/databaseMssqlService';
import { MssqlEnvironment } from '../database/types';

type PipelineStep =
  | 'FEATURE_SELECTION'
  | 'HOST_SELECTION'
  | 'COMMAND_GENERATION'
  | 'COMMAND_FEEDBACK'
  | 'DATABASE_QUERY_GENERATION'
  | 'DATABASE_RESULT_SYNTHESIS'
  | 'RUNBOOK_UPDATE'
  | null;

interface ActiveTerminalExec {
  terminalId: string;
  host: string;
  userId?: string;
  platform: 'PS' | 'CMD';
  command: string;
  outputBuffer: string;
  convId?: string;
  hasExecuted?: boolean;
}

interface ActiveDatabaseExec {
  taskId: string;
  environment: MssqlEnvironment;
  sql: string;
  convId?: string;
  results: any[];
  rowCount?: number;
  durationMs?: number;
  error?: string | null;
  hasExecuted?: boolean;
}

export const AsrAiDashboardPage: React.FC = () => {
  const [servers, setServers] = useState<ServerConfig[]>([]);
  const [conversations, setConversations] = useState<AiConversationRecord[]>([]);
  const [activeConvId, setActiveConvId] = useState<string | null>(null);
  const [messages, setMessages] = useState<AiChatMessageRecord[]>([]);

  const [selectedHosts, setSelectedHosts] = useState<string[]>([]);
  const [selectedDashboards, setSelectedDashboards] = useState<string[]>(['auto']);
  const [isLoading, setIsLoading] = useState(false);
  const [statusText, setStatusText] = useState('');
  const [isAwaitingHostReview, setIsAwaitingHostReview] = useState(false);
  const [isAwaitingCommandReview, setIsAwaitingCommandReview] = useState(false);
  const [isAwaitingDatabaseReview, setIsAwaitingDatabaseReview] = useState(false);

  const activeConvRef = useRef<AiConversationRecord | null>(null);
  const activeConvIdRef = useRef<string | null>(activeConvId);
  const isExecutingRef = useRef(false);
  const taskTimeoutTimerRef = useRef<any>(null);
  const lastUserQueryRef = useRef<string>('');
  const lastFilesPathRef = useRef<string[] | null>(null);
  const runbookRootPathRef = useRef<string>('C:/Ranjan/AutomationSuite_v2/Runbooks');
  const currentPipelineStepRef = useRef<PipelineStep>(null);
  const activeTerminalExecRef = useRef<ActiveTerminalExec | null>(null);
  const activeDatabaseExecRef = useRef<ActiveDatabaseExec | null>(null);
  const turnsRef = useRef<ConversationTurnRecord[]>([]);
  const lastExecutedCmdRef = useRef<string>('');
  const lastExecutionOutputRef = useRef<string>('');


  // Tracks pending new chat data until Copilot assigns the real conversationId
  const pendingNewChatRef = useRef<{
    queryText: string;
    userMsg: AiChatMessageRecord;
    targetHosts: string[];
  } | null>(null);

  useEffect(() => {
    activeConvIdRef.current = activeConvId;
  }, [activeConvId]);

  // Load server inventory and initial conversations list
  useEffect(() => {
    loadServers();
    loadConversations();
  }, []);

  const loadServers = async () => {
    try {
      const inv = await fetchServerInventory();
      setServers(inv);
    } catch (err) {
      logger.info('AsrAiDashboardPage', 'loadServers', 'Error loading server inventory', err);
    }
  };

  const loadConversations = async () => {
    try {
      const list = await getAllAiConversations();
      setConversations(list);
      if (list.length > 0 && !activeConvId) {
        selectConversation(list[0]);
      } else if (list.length === 0) {
        createNewChat();
      }
    } catch (err) {
      logger.info('AsrAiDashboardPage', 'loadConversations', 'Error loading conversations', err);
    }
  };

  const selectConversation = async (conv: AiConversationRecord) => {
    if (taskTimeoutTimerRef.current) {
      clearTimeout(taskTimeoutTimerRef.current);
      taskTimeoutTimerRef.current = null;
    }
    setActiveConvId(conv.id);
    activeConvRef.current = conv;
    pendingNewChatRef.current = null;
    setIsAwaitingHostReview(false);
    setIsAwaitingCommandReview(false);
    setIsAwaitingDatabaseReview(false);
    activeDatabaseExecRef.current = null;
    setSelectedHosts(conv.targetHosts || []);
    turnsRef.current = [];
    try {
      const msgs = await getAiMessagesByConversation(conv.id);
      setMessages(msgs);
    } catch (err) {
      logger.info('AsrAiDashboardPage', 'selectConversation', 'Error loading messages for conversation', { id: conv.id, err });
    }
  };

  const createNewChat = () => {
    if (taskTimeoutTimerRef.current) {
      clearTimeout(taskTimeoutTimerRef.current);
      taskTimeoutTimerRef.current = null;
    }
    setActiveConvId(null);
    activeConvRef.current = null;
    pendingNewChatRef.current = null;
    lastUserQueryRef.current = '';
    currentPipelineStepRef.current = null;
    activeTerminalExecRef.current = null;
    activeDatabaseExecRef.current = null;
    turnsRef.current = [];
    lastExecutedCmdRef.current = '';
    lastExecutionOutputRef.current = '';
    setIsAwaitingHostReview(false);
    setIsAwaitingCommandReview(false);
    setIsAwaitingDatabaseReview(false);
    setMessages([]);
    setSelectedHosts([]);
    setSelectedDashboards(['auto']);
    setIsLoading(false);
    setStatusText('');
  };

  const handleRenameConversation = (id: string, newTitle: string) => {
    setConversations((prev) =>
      prev.map((c) => {
        if (c.id === id) {
          const updated = { ...c, title: newTitle, updatedAt: Date.now() };
          saveAiConversation(updated);
          return updated;
        }
        return c;
      })
    );
  };

  const handleDeleteConversation = async (id: string) => {
    try {
      globalWebSocket.send({
        'event-type': WS_EVENTS.AI_DELETE_REQ,
        conversationId: id,
      });
    } catch (err) {
      logger.warn('AsrAiDashboardPage', 'handleDeleteConversation', 'Failed to send delete event', err);
    }

    await deleteAiConversationRepo(id);
    await deleteAiMessagesByConversation(id);

    const updated = conversations.filter((c) => c.id !== id);
    setConversations(updated);
    if (activeConvId === id) {
      if (updated.length > 0) {
        selectConversation(updated[0]);
      } else {
        createNewChat();
      }
    }
  };

  const handleApplySelectedHosts = (hosts: string[]) => {
    setSelectedHosts(hosts);
    if (activeConvRef.current && activeConvIdRef.current) {
      const updated = {
        ...activeConvRef.current,
        targetHosts: hosts,
        isHostKnown: hosts.length > 0,
        updatedAt: Date.now(),
      };
      activeConvRef.current = updated;
      saveAiConversation(updated);
      setConversations((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
    }
  };

  // Subscribe to Global WebSocket as independent GlobalWebSocketListener
  useEffect(() => {
    const listener: GlobalWebSocketListener = {
      onMessage: (frame: any) => {
        handleIncomingFrame(frame);
      },
    };

    globalWebSocket.registerListener(listener);
    // Prefetch Runbook Root Path from AI Agent backend
    globalWebSocket.send({ 'event-type': WS_EVENTS.AI_RUNBOOKS_ROOT_PATH });
    return () => {
      globalWebSocket.unregisterListener(listener);
      if (taskTimeoutTimerRef.current) {
        clearTimeout(taskTimeoutTimerRef.current);
        taskTimeoutTimerRef.current = null;
      }
      if (activeTerminalExecRef.current) {
        globalWebSocket.send({
          'event-type': WS_EVENTS.TERMINAL_CLOSE,
          terminalid: activeTerminalExecRef.current.terminalId,
          host: activeTerminalExecRef.current.host,
          userId: 'user',
        });
        activeTerminalExecRef.current = null;
      }
      activeDatabaseExecRef.current = null;
    };
  }, []);

  const resetToAutoMode = () => {
    setSelectedDashboards(['auto']);
    setSelectedHosts([]);
    currentPipelineStepRef.current = null;
    if (activeTerminalExecRef.current) {
      globalWebSocket.send({
        'event-type': WS_EVENTS.TERMINAL_CLOSE,
        terminalid: activeTerminalExecRef.current.terminalId,
        host: activeTerminalExecRef.current.host,
        userId: 'user',
      });
      activeTerminalExecRef.current = null;
    }
    activeDatabaseExecRef.current = null;
    isExecutingRef.current = false;
    setIsAwaitingHostReview(false);
    setIsAwaitingCommandReview(false);
    setIsAwaitingDatabaseReview(false);
    lastFilesPathRef.current = null;
  };

  // Records ground-truth turn details and maintains rolling accumulated context
  const recordTurnAndPersistContext = async (
    query: string,
    outcome?: string,
    host?: string,
    feature?: string,
    cmd?: string,
    findings?: string,
    aiReportedContext?: string | null
  ) => {
    const turnNumber = turnsRef.current.length + 1;
    const turn: ConversationTurnRecord = {
      turnNumber,
      userQuery: query,
      targetHost: host,
      feature: feature,
      commandExecuted: cmd,
      keyFindings: findings,
      finalOutcome: outcome,
      timestamp: Date.now(),
    };
    turnsRef.current.push(turn);

    const groundTruth = ContextAccumulatorService.formatRollingContext(turnsRef.current);
    const sanitized = ContextAccumulatorService.sanitizeContext(aiReportedContext, groundTruth);

    if (activeConvRef.current) {
      activeConvRef.current.accumulatedContextData = sanitized;
      await saveAiConversation(activeConvRef.current);
    }
  };

  // Step 2: Trigger Target Host Resolution Prompt
  const triggerHostResolution = (query: string, convId?: string) => {
    const targetConvId = convId || activeConvIdRef.current;
    currentPipelineStepRef.current = 'HOST_SELECTION';
    setIsLoading(true);
    setStatusText('Identifying target hosts from inventory...');

    const hostPrompt = PromptBuilder.buildHostSelectionPrompt(
      query,
      servers,
      activeConvRef.current?.accumulatedContextData
    );

    const sent = globalWebSocket.send({
      'event-type': WS_EVENTS.AI_EXECUTE_TASK,
      conversationId: targetConvId || null,
      prompt: hostPrompt,
      query: query,
      targetHosts: [],
      dashboardId: 'terminal',
    });

    if (!sent) {
      handleWebSocketSendError(targetConvId);
      return;
    }
    startTaskTimeoutGuard(targetConvId);
  };

  // Step 3: Trigger Diagnostics / Command Creation Prompt
  const triggerCommandCreation = (hosts: string[], convId?: string, accumulatedContextOverride?: string, filesPathOverride?: string[]) => {
    const targetConvId = convId || activeConvIdRef.current;
    const targetQuery = lastUserQueryRef.current || 'execute diagnostics';
    currentPipelineStepRef.current = 'COMMAND_GENERATION';

    setIsLoading(true);
    setStatusText(`Generating commands for host(s): ${hosts.join(', ')}...`);

    const contextToUse = accumulatedContextOverride || activeConvRef.current?.accumulatedContextData || null;
    const cmdPrompt = PromptBuilder.buildDiagnosticsRemediationPrompt(
      targetQuery,
      hosts.join(', '),
      contextToUse
    );

    const filesToSend = filesPathOverride || lastFilesPathRef.current || [];
    lastFilesPathRef.current = null;

    const sent = globalWebSocket.send({
      'event-type': WS_EVENTS.AI_EXECUTE_TASK,
      conversationId: targetConvId || null,
      prompt: cmdPrompt,
      query: targetQuery,
      targetHosts: hosts,
      dashboardId: 'terminal',
      ...(filesToSend.length > 0 ? { files_path: filesToSend, files_needed: filesToSend } : {}),
    });

    if (!sent) {
      handleWebSocketSendError(targetConvId);
      return;
    }
    startTaskTimeoutGuard(targetConvId);
  };

  // Step 5: Send Command Execution Results Back to AI (Multi-Turn Feedback Loop)
  const triggerFeedbackEvaluation = (cmd: string, output: string, host: string, convId?: string, filesPathOverride?: string[]) => {
    const targetConvId = convId || activeConvIdRef.current;
    const originalQuery = lastUserQueryRef.current || 'execute task';
    currentPipelineStepRef.current = 'COMMAND_FEEDBACK';
    lastExecutedCmdRef.current = cmd;
    lastExecutionOutputRef.current = output;

    setIsLoading(true);
    setStatusText('ASR_AI is evaluating command results...');

    const feedbackInstruction = 'Note: If this gave an error, you may provide a proper command to run smoothly, or if you already got the resolution you may proceed to share the final output.';
    const executionFeedbackContext = `Executed Command: "${cmd}" on Host "${host}"\nCommand Output:\n${output}\n\n${feedbackInstruction}\n\n${activeConvRef.current?.accumulatedContextData || ''}`;
    const cmdPrompt = PromptBuilder.buildDiagnosticsRemediationPrompt(
      originalQuery,
      host,
      executionFeedbackContext
    );

    const filesToSend = filesPathOverride || lastFilesPathRef.current || [];
    lastFilesPathRef.current = null;

    const sent = globalWebSocket.send({
      'event-type': WS_EVENTS.AI_EXECUTE_TASK,
      conversationId: targetConvId || null,
      prompt: cmdPrompt,
      query: originalQuery,
      targetHosts: [host],
      dashboardId: 'terminal',
      ...(filesToSend.length > 0 ? { files_path: filesToSend, files_needed: filesToSend } : {}),
    });

    if (!sent) {
      handleWebSocketSendError(targetConvId);
      return;
    }
    startTaskTimeoutGuard(targetConvId);
  };

  // Step 6: Post-Outcome Runbook Evaluation (Non-blocking background turn in same conversation)
  const triggerRunbookEvaluation = async (query: string, outcome: string, convId?: string) => {
    const targetConvId = convId || activeConvIdRef.current;
    if (!targetConvId) return;

    currentPipelineStepRef.current = 'RUNBOOK_UPDATE';

    const runbookPrompt = PromptBuilder.buildRunbookUpdatePrompt(
      query,
      outcome,
      activeConvRef.current?.accumulatedContextData
    );

    const activeUser = await getActiveAuthUserInfo();
    const effectiveUserId = activeUser?.userId || 'sahoos';

    globalWebSocket.send({
      'event-type': WS_EVENTS.AI_EXECUTE_TASK,
      conversationId: targetConvId,
      prompt: runbookPrompt,
      query: query,
      targetHosts: selectedHosts,
      dashboardId: 'terminal',
      userId: effectiveUserId,
    });
  };

  // Database Feature: Trigger T-SQL Query Generation Prompt
  const triggerDatabaseQueryGeneration = (query: string, convId?: string) => {
    const targetConvId = convId || activeConvIdRef.current;
    currentPipelineStepRef.current = 'DATABASE_QUERY_GENERATION';
    setIsLoading(true);
    setStatusText('Generating MSSQL query and evaluating safety...');

    const dbPrompt = PromptBuilder.buildDatabaseQueryPrompt(
      query,
      activeConvRef.current?.accumulatedContextData
    );

    const sent = globalWebSocket.send({
      'event-type': WS_EVENTS.AI_EXECUTE_TASK,
      conversationId: targetConvId || null,
      prompt: dbPrompt,
      query: query,
      targetHosts: [],
      dashboardId: 'database',
    });

    if (!sent) {
      handleWebSocketSendError(targetConvId);
      return;
    }
    startTaskTimeoutGuard(targetConvId);
  };

  // Database Feature: Execute generated SQL query over globalWebSocket with ASR_AI identity
  const executeAiDatabaseQuery = async (msg: AiChatMessageRecord) => {
    if (!msg.sql) return;

    setIsLoading(true);
    isExecutingRef.current = true;

    const activeUser = await getActiveAuthUserInfo();
    if (!activeUser || !activeUser.userId) {
      alert('You are not authorized. Your session has expired, please log in.');
      setIsLoading(false);
      isExecutingRef.current = false;
      return;
    }

    const env: MssqlEnvironment = msg.environment === 'test' ? 'test' : 'prod';
    const aiUserId = `${activeUser.userId}_ASR_AI`;
    const taskId = `ai_db_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;

    setStatusText(`Connecting and executing query on MSSQL (${env.toUpperCase()})...`);

    activeDatabaseExecRef.current = {
      taskId,
      environment: env,
      sql: msg.sql,
      convId: activeConvIdRef.current || msg.conversationId,
      results: [],
      rowCount: 0,
      durationMs: 0,
      error: null,
      hasExecuted: true,
    };

    // Arm 30s execution timeout guard
    if (taskTimeoutTimerRef.current) clearTimeout(taskTimeoutTimerRef.current);
    taskTimeoutTimerRef.current = setTimeout(() => {
      const current = activeDatabaseExecRef.current;
      if (current && isExecutingRef.current) {
        logger.warn('AsrAiDashboardPage', 'executeAiDatabaseQuery', 'Database query reached 30s timeout limit; finalizing');
        current.error = 'Query execution timed out after 30 seconds.';
        completeDatabaseExecution(current);
      }
    }, 30000);

    databaseMssqlService.executeQuery(taskId, aiUserId, env, msg.sql);
  };

  // Database Feature: Complete query execution, send system summary, and trigger AI result synthesis
  const completeDatabaseExecution = async (exec: ActiveDatabaseExec) => {
    if (taskTimeoutTimerRef.current) {
      clearTimeout(taskTimeoutTimerRef.current);
      taskTimeoutTimerRef.current = null;
    }

    setIsLoading(true);
    setStatusText('ASR_AI is synthesizing database findings...');
    isExecutingRef.current = false;
    activeDatabaseExecRef.current = null;

    const currentConvId = activeConvIdRef.current || exec.convId;
    const rowCount = exec.rowCount !== undefined ? exec.rowCount : exec.results.length;

    // Render system output badge in chat
    const sysSummaryMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: currentConvId || '',
      sender: 'system',
      text: exec.error
        ? `Database query completed with error: ${exec.error}. Evaluating next action with AI...`
        : `Database query returned ${rowCount} row(s) (${exec.durationMs || 0}ms). Evaluating next action with AI...`,
      timestamp: Date.now(),
    };
    if (currentConvId) await saveAiChatMessage(sysSummaryMsg);
    setMessages((prev) => [...prev, sysSummaryMsg]);

    // Format sample rows (capped at 50) for result synthesis prompt
    const sampleRows = exec.results.slice(0, 50);
    const resultsPayload = JSON.stringify(
      {
        totalRowCount: rowCount,
        durationMs: exec.durationMs,
        error: exec.error || null,
        rows: sampleRows,
        truncated: exec.results.length > 50,
      },
      null,
      2
    );

    triggerDatabaseResultSynthesis(
      lastUserQueryRef.current || 'query database',
      exec.environment,
      exec.sql,
      resultsPayload,
      currentConvId || undefined
    );
  };

  // Database Feature: Trigger AI Result Synthesis Prompt
  const triggerDatabaseResultSynthesis = (
    query: string,
    env: string,
    sql: string,
    resultsJson: string,
    convId?: string
  ) => {
    const targetConvId = convId || activeConvIdRef.current;
    currentPipelineStepRef.current = 'DATABASE_RESULT_SYNTHESIS';
    setIsLoading(true);
    setStatusText('ASR_AI is synthesizing database insights...');

    const synthPrompt = PromptBuilder.buildDatabaseResultSynthesisPrompt(
      query,
      env,
      sql,
      resultsJson,
      activeConvRef.current?.accumulatedContextData
    );

    const sent = globalWebSocket.send({
      'event-type': WS_EVENTS.AI_EXECUTE_TASK,
      conversationId: targetConvId || null,
      prompt: synthPrompt,
      query: query,
      targetHosts: [],
      dashboardId: 'database',
    });

    if (!sent) {
      handleWebSocketSendError(targetConvId);
      return;
    }
    startTaskTimeoutGuard(targetConvId);
  };

  const handleApproveDatabase = (msg: AiChatMessageRecord) => {
    setIsAwaitingDatabaseReview(false);
    executeAiDatabaseQuery(msg);
  };

  const handleDeclineDatabase = async (msg: AiChatMessageRecord) => {
    setIsAwaitingDatabaseReview(false);
    setIsLoading(false);
    setStatusText('');
    currentPipelineStepRef.current = null;

    const currentConvId = activeConvIdRef.current || msg.conversationId || '';
    const declineMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: currentConvId,
      sender: 'ai',
      text: 'Database query execution declined. Please let me know how you would like to refine the query or if you need anything else.',
      timestamp: Date.now(),
      status: 'success',
    };
    if (currentConvId) {
      await saveAiChatMessage(declineMsg);
    }
    setMessages((prev) => [...prev, declineMsg]);
  };

  const handleWebSocketSendError = (convId?: string | null) => {
    setIsLoading(false);
    setStatusText('');
    const wsErrMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: convId || '',
      sender: 'ai',
      text: 'WebSocket offline: Failed to send request to backend server.',
      timestamp: Date.now(),
      status: 'error',
    };
    if (convId) {
      saveAiChatMessage(wsErrMsg);
    }
    setMessages((prev) => [...prev, wsErrMsg]);
  };

  const startTaskTimeoutGuard = (convId?: string | null) => {
    if (taskTimeoutTimerRef.current) clearTimeout(taskTimeoutTimerRef.current);
    taskTimeoutTimerRef.current = setTimeout(() => {
      setIsLoading(false);
      setStatusText('');
      const timeoutMsg: AiChatMessageRecord = {
        id: `msg_${Date.now()}`,
        conversationId: convId || '',
        sender: 'ai',
        text: 'AI Agent task timed out after 120 seconds.',
        timestamp: Date.now(),
        status: 'error',
      };
      if (convId) {
        saveAiChatMessage(timeoutMsg);
      }
      setMessages((prev) => [...prev, timeoutMsg]);
    }, 120000);
  };

  // Completes terminal execution, cleans up resources, and dispatches captured output to AI
  const completeTerminalExecution = async (exec: ActiveTerminalExec) => {
    if (taskTimeoutTimerRef.current) {
      clearTimeout(taskTimeoutTimerRef.current);
      taskTimeoutTimerRef.current = null;
    }

    setIsLoading(false);
    setStatusText('');
    isExecutingRef.current = false;
    activeTerminalExecRef.current = null;

    // Standard lifecycle: stop any running process and close reserved terminal session
    globalWebSocket.send({
      'event-type': WS_EVENTS.TERMINAL_STOP,
      terminalid: exec.terminalId,
      host: exec.host,
      userId: 'user',
    });
    globalWebSocket.send({
      'event-type': WS_EVENTS.TERMINAL_CLOSE,
      terminalid: exec.terminalId,
      host: exec.host,
      userId: 'user',
    });

    // Strip terminal ANSI escape color codes
    const rawOutput = exec.outputBuffer.trim();
    const capturedOutput = rawOutput.replace(/\u001b\[[0-9;]*[a-zA-Z]/g, '').trim();
    const targetHost = exec.host;
    const executedCmd = exec.command;
    const currentConvId = activeConvIdRef.current || exec.convId;

    // Render execution result summary in chat
    const sysSummaryMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: currentConvId || '',
      sender: 'system',
      text: capturedOutput
        ? `Command output received (${capturedOutput.length} characters). Evaluating next action with AI...`
        : `Command executed with no output. Evaluating next action with AI...`,
      timestamp: Date.now(),
    };
    if (currentConvId) await saveAiChatMessage(sysSummaryMsg);
    setMessages((prev) => [...prev, sysSummaryMsg]);

    // Multi-turn feedback loop: dispatch captured output back to AI for evaluation
    triggerFeedbackEvaluation(executedCmd, capturedOutput, targetHost, currentConvId || undefined);
  };

  // Main Incoming WebSocket Frames Handler
  const handleIncomingFrame = async (frame: any) => {
    const eventType = frame['event-type'];

    // Handle AI_RUNBOOKS_ROOT_PATH response from AI Agent backend
    if (eventType === WS_EVENTS.AI_RUNBOOKS_ROOT_PATH || frame.event === 'AI_RUNBOOKS_ROOT_PATH' || frame['event-type'] === 'AI_RUNBOOKS_ROOT_PATH') {
      if (frame.path) {
        runbookRootPathRef.current = frame.path;
        logger.info('AsrAiDashboardPage', 'handleIncomingFrame', `Runbook root path synchronized: ${frame.path}`);
      }
      return;
    }

    // 1. Unified AI Task Result Event (Handles AI prompt turns)
    if (eventType === WS_EVENTS.AI_TASK_RESULT) {
      if (taskTimeoutTimerRef.current) {
        clearTimeout(taskTimeoutTimerRef.current);
        taskTimeoutTimerRef.current = null;
      }

      setIsLoading(false);
      setStatusText('');

      const rawStatus = frame.status || '';
      const statusUpper = String(rawStatus).toUpperCase();
      const returnedConvId = frame.conversationId;
      const rawResp = frame.rawResponse || frame.response || '';
      const errorMsg = frame.error || frame.message || null;

      const isFailed =
        statusUpper === 'FAILED' ||
        statusUpper === 'ERROR' ||
        statusUpper === 'NOT_OK' ||
        (errorMsg !== null && errorMsg !== '');

      let effectiveConvId = activeConvIdRef.current;

      // Handle new chat real conversation ID assignment
      if (!effectiveConvId && returnedConvId) {
        effectiveConvId = returnedConvId;
        setActiveConvId(returnedConvId);
        activeConvIdRef.current = returnedConvId;

        const pending = pendingNewChatRef.current;
        const titleText = pending?.queryText
          ? (pending.queryText.length > 25 ? `${pending.queryText.substring(0, 25)}...` : pending.queryText)
          : 'New AI Conversation';

        const newConv: AiConversationRecord = {
          id: returnedConvId,
          title: titleText,
          targetHosts: pending?.targetHosts || selectedHosts,
          isHostKnown: (pending?.targetHosts || selectedHosts).length > 0,
          createdAt: Date.now(),
          updatedAt: Date.now(),
        };

        activeConvRef.current = newConv;
        await saveAiConversation(newConv);
        setConversations((prev) => [newConv, ...prev.filter((c) => c.id !== returnedConvId)]);

        if (pending?.userMsg) {
          const persistedUserMsg: AiChatMessageRecord = {
            ...pending.userMsg,
            conversationId: returnedConvId,
          };
          await saveAiChatMessage(persistedUserMsg);
        }
        pendingNewChatRef.current = null;
      }

      // Handle Failures
      if (isFailed) {
        const finalError =
          errorMsg ||
          (typeof rawResp === 'string' && rawResp.trim() ? rawResp.trim() : null) ||
          'AI task execution failed.';

        const errMsg: AiChatMessageRecord = {
          id: `msg_${Date.now()}`,
          conversationId: effectiveConvId || '',
          sender: 'ai',
          text: finalError,
          timestamp: Date.now(),
          status: 'error',
        };

        if (effectiveConvId) {
          await saveAiChatMessage(errMsg);
        }
        setMessages((prev) => [...prev, errMsg]);
        pendingNewChatRef.current = null;
        currentPipelineStepRef.current = null;
        setIsAwaitingHostReview(false);
        return;
      }

      // Parse AI JSON response
      const parseResult = safeParseAiJson(rawResp);

      if (!parseResult.success || !parseResult.data || typeof parseResult.data !== 'object') {
        // If raw response looks like a JSON string that failed parsing, never dump raw JSON to user!
        const looksLikeJson = /^\s*\{.*\}\s*$/s.test(rawResp) || /"(?:feature_id|Command|pre_response|review_title)"/i.test(rawResp);
        if (looksLikeJson) {
          logger.warn('AsrAiDashboardPage', 'handleIncomingFrame', 'Raw JSON response failed all repair attempts', rawResp);
          const friendlyErr: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            text: 'I encountered an issue formatting the system actions. Please try rephrasing or resubmitting your request.',
            timestamp: Date.now(),
            status: 'error',
          };
          if (effectiveConvId) await saveAiChatMessage(friendlyErr);
          setMessages((prev) => [...prev, friendlyErr]);
          resetToAutoMode();
          return;
        }

        // Plain text response (conversational)
        const isLikelyErrorText = /error|failed|exception|unavailable|denied|no longer available/i.test(rawResp);
        const aiMsg: AiChatMessageRecord = {
          id: `msg_${Date.now()}`,
          conversationId: effectiveConvId || '',
          sender: 'ai',
          text: isLikelyErrorText ? rawResp : undefined,
          finalOutcome: !isLikelyErrorText ? (rawResp || 'Completed.') : undefined,
          timestamp: Date.now(),
          status: isLikelyErrorText ? 'error' : 'success',
        };
        if (effectiveConvId) await saveAiChatMessage(aiMsg);
        setMessages((prev) => [...prev, aiMsg]);

        if (!isLikelyErrorText) {
          await recordTurnAndPersistContext(lastUserQueryRef.current, rawResp || 'Completed.');
        }
        resetToAutoMode();
        return;
      }

      const data = parseResult.data;

      // Check if JSON explicitly reports error
      if (data.error || data.status === 'error' || data.status === 'failed') {
        const jsonErrMsg = data.error || data.message || 'AI task reported an error.';
        const errAiMsg: AiChatMessageRecord = {
          id: `msg_${Date.now()}`,
          conversationId: effectiveConvId || '',
          sender: 'ai',
          text: jsonErrMsg,
          timestamp: Date.now(),
          status: 'error',
        };
        if (effectiveConvId) await saveAiChatMessage(errAiMsg);
        setMessages((prev) => [...prev, errAiMsg]);
        currentPipelineStepRef.current = null;
        setIsAwaitingHostReview(false);
        return;
      }

      const preResponse = data.pre_response || data.preResponse || data.thought || data.reasoning || null;
      const finalOutcome = data.final_outcome || data.finalOutcome || null;
      const featureId = data.feature_id || data.featureId || null;

      // Guard against raw schema template text hallucinated or scraped from unsubmitted prompt
      const isPlaceholderText = (val: any) =>
        typeof val === 'string' &&
        (val.includes('<short active status message>') ||
         val.includes('<string | null:') ||
         val.includes('<array of resolved host names') ||
         val.includes('<shell command string'));

      if (isPlaceholderText(preResponse) || isPlaceholderText(finalOutcome)) {
        logger.warn('AsrAiDashboardPage', 'handleIncomingFrame', 'Detected schema placeholder string in AI response', { preResponse, finalOutcome });
        const invalidRespMsg: AiChatMessageRecord = {
          id: `msg_${Date.now()}`,
          conversationId: effectiveConvId || '',
          sender: 'ai',
          text: 'I could not generate a valid response for this turn. Please try again.',
          timestamp: Date.now(),
          status: 'error',
        };
        if (effectiveConvId) await saveAiChatMessage(invalidRespMsg);
        setMessages((prev) => [...prev, invalidRespMsg]);
        resetToAutoMode();
        return;
      }

      const targetHosts: string[] | undefined = Array.isArray(data.target_hosts)
        ? data.target_hosts
        : Array.isArray(data.targetHosts)
        ? data.targetHosts
        : undefined;

      const reviewTitle = data.review_title || data.reviewTitle || null;
      const reviewDescription = data.review_description || data.reviewDescription || null;

      const command = data.Command || data.command || data.cmd || null;
      const platform: 'PS' | 'CMD' = data.platform === 'CMD' ? 'CMD' : 'PS';
      const reviewNeeded = Boolean(data.review_needed ?? data.reviewNeeded ?? (command ? true : false));
      const criticalityLevel = Number(data.CriticalityLevele ?? data.criticality_level ?? data.criticalityLevel ?? 1);
      const fileNeeded: string[] | null = Array.isArray(data.file_needed)
        ? data.file_needed
        : Array.isArray(data.files_needed)
        ? data.files_needed
        : Array.isArray(data.fileNeeded)
        ? data.fileNeeded
        : Array.isArray(data.filesNeeded)
        ? data.filesNeeded
        : null;

      if (fileNeeded && fileNeeded.length > 0) {
        lastFilesPathRef.current = fileNeeded;
      }

      // Handle Current Pipeline Step
      const currentStep = currentPipelineStepRef.current;

      // Handle Post-Outcome Runbook Evaluation
      if (currentStep === 'RUNBOOK_UPDATE') {
        currentPipelineStepRef.current = null;
        if (data.runbook_action && data.runbook_action.target_file) {
          logger.info('AsrAiDashboardPage', 'Runbook Updated Successfully', data.runbook_action);
        }
        return;
      }

      // STEP A: FEATURE / INTENT ROUTING RESPONSE
      if (currentStep === 'FEATURE_SELECTION') {
        if (featureId === 'database') {
          // Action request for Database: show thought process and advance to database query generation
          const thoughtMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse || 'Query intent requires database query execution. Selected Feature: Database (MSSQL).',
            timestamp: Date.now(),
            status: 'success',
          };
          if (effectiveConvId) await saveAiChatMessage(thoughtMsg);
          setMessages((prev) => [...prev, thoughtMsg]);

          setSelectedDashboards(['database']);

          if (data.accumulated_context_data && activeConvRef.current) {
            const sanitized = ContextAccumulatorService.sanitizeContext(
              data.accumulated_context_data,
              activeConvRef.current.accumulatedContextData || 'None'
            );
            activeConvRef.current.accumulatedContextData = sanitized;
            saveAiConversation(activeConvRef.current);
          }

          triggerDatabaseQueryGeneration(lastUserQueryRef.current, effectiveConvId || undefined);
          return;
        }

        if (!featureId || featureId !== 'terminal') {
          // General Q&A or directly resolved query: deliver Markdown answer

          const answerText = finalOutcome || reviewDescription || 'Query resolved.';
          const directMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse,
            finalOutcome: answerText,
            timestamp: Date.now(),
            status: 'success',
          };
          if (effectiveConvId) await saveAiChatMessage(directMsg);
          setMessages((prev) => [...prev, directMsg]);

          await recordTurnAndPersistContext(
            lastUserQueryRef.current,
            answerText,
            undefined,
            'Q&A',
            undefined,
            undefined,
            data.accumulated_context_data
          );

          resetToAutoMode();
          triggerRunbookEvaluation(
            lastUserQueryRef.current,
            answerText,
            effectiveConvId || undefined
          );
          return;
        }

        // Action request for Terminal: show thought process and advance to host resolution
        const thoughtMsg: AiChatMessageRecord = {
          id: `msg_${Date.now()}`,
          conversationId: effectiveConvId || '',
          sender: 'ai',
          preResponse: preResponse || 'Query intent requires system diagnostics and command execution. Selected Feature: Terminal.',
          timestamp: Date.now(),
          status: 'success',
        };
        if (effectiveConvId) await saveAiChatMessage(thoughtMsg);
        setMessages((prev) => [...prev, thoughtMsg]);

        setSelectedDashboards(['terminal']);

        if (data.accumulated_context_data && activeConvRef.current) {
          const sanitized = ContextAccumulatorService.sanitizeContext(
            data.accumulated_context_data,
            activeConvRef.current.accumulatedContextData || 'None'
          );
          activeConvRef.current.accumulatedContextData = sanitized;
          saveAiConversation(activeConvRef.current);
        }

        // Check if host is in Auto mode or pre-selected
        if (selectedHosts.length === 0) {
          triggerHostResolution(lastUserQueryRef.current, effectiveConvId || undefined);
        } else {
          triggerCommandCreation(selectedHosts, effectiveConvId || undefined);
        }
        return;
      }

      // STEP B: TARGET HOST RESOLUTION RESPONSE
      if (currentStep === 'HOST_SELECTION') {
        if (data.accumulated_context_data && activeConvRef.current) {
          const sanitized = ContextAccumulatorService.sanitizeContext(
            data.accumulated_context_data,
            activeConvRef.current.accumulatedContextData || 'None'
          );
          activeConvRef.current.accumulatedContextData = sanitized;
          saveAiConversation(activeConvRef.current);
        }
        if (targetHosts && targetHosts.length > 0) {
          // Candidate host(s) selected: require Yes/No review and lock input box
          setIsAwaitingHostReview(true);
          const hostReviewMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse || `Target host identified: ${targetHosts.join(', ')}.`,
            targetHosts: targetHosts,
            reviewRequiredHost: true,
            reviewTitle: reviewTitle || `Confirm Target Host: ${targetHosts.join(', ')}`,
            reviewDescription: reviewDescription || `Inferred target host ${targetHosts.join(', ')} based on query context. Please confirm to proceed.`,
            timestamp: Date.now(),
            status: 'success',
          };
          if (effectiveConvId) await saveAiChatMessage(hostReviewMsg);
          setMessages((prev) => [...prev, hostReviewMsg]);
          return;
        } else {
          // Unresolved host: informational prompt asking user for details, input box remains unlocked!
          setIsAwaitingHostReview(false);
          const hostClarifyMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse,
            text: reviewDescription || 'Target Host Required: Could you please specify which host or server to target?',
            timestamp: Date.now(),
            status: 'success',
          };
          if (effectiveConvId) await saveAiChatMessage(hostClarifyMsg);
          setMessages((prev) => [...prev, hostClarifyMsg]);
          currentPipelineStepRef.current = null;
          return;
        }
      }

      // STEP C: COMMAND GENERATION OR STEP D: FEEDBACK EVALUATION RESPONSE
      if (currentStep === 'COMMAND_GENERATION' || currentStep === 'COMMAND_FEEDBACK' || !currentStep) {
        if (data.accumulated_context_data && activeConvRef.current) {
          const sanitized = ContextAccumulatorService.sanitizeContext(
            data.accumulated_context_data,
            activeConvRef.current.accumulatedContextData || 'None'
          );
          activeConvRef.current.accumulatedContextData = sanitized;
          saveAiConversation(activeConvRef.current);
        }
        if (command) {
          const isCriticalOrHigher = (criticalityLevel ?? 0) >= 2;

          const cmdMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse,
            command: command,
            platform: platform,
            reviewNeeded: isCriticalOrHigher,
            reviewRequiredHost: false,
            reviewTitle: reviewTitle || (isCriticalOrHigher ? 'Review Proposed Command' : 'Executing Command'),
            reviewDescription: reviewDescription || (isCriticalOrHigher ? 'Please confirm execution of this command.' : 'Automatically executing diagnostic command.'),
            criticalityLevel: criticalityLevel,
            finalOutcome: finalOutcome,
            targetHosts: selectedHosts,
            fileNeeded: fileNeeded || undefined,
            timestamp: Date.now(),
            status: 'success',
          };

          if (effectiveConvId) await saveAiChatMessage(cmdMsg);
          setMessages((prev) => [...prev, cmdMsg]);

          if (isCriticalOrHigher) {
            setIsAwaitingCommandReview(true);
          } else {
            setIsAwaitingCommandReview(false);
            executeTerminalCommand(cmdMsg);
          }
          return;
        } else if (fileNeeded && fileNeeded.length > 0) {
          // AI requested file content analysis without executing a command
          setIsAwaitingCommandReview(false);
          const fileAnalysisMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse || `Requesting file inspection for: ${fileNeeded.join(', ')}`,
            fileNeeded: fileNeeded,
            timestamp: Date.now(),
            status: 'success',
          };
          if (effectiveConvId) await saveAiChatMessage(fileAnalysisMsg);
          setMessages((prev) => [...prev, fileAnalysisMsg]);

          triggerFeedbackEvaluation('File inspection request', `Inspecting files: ${fileNeeded.join(', ')}`, selectedHosts[0] || 'localhost', effectiveConvId || undefined, fileNeeded);
          return;
        } else {
          // No command needed or final outcome achieved: user query resolved!
          setIsAwaitingCommandReview(false);
          const outcomeMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse,
            finalOutcome: finalOutcome || reviewDescription || 'Task completed.',
            timestamp: Date.now(),
            status: 'success',
          };
          if (effectiveConvId) await saveAiChatMessage(outcomeMsg);
          setMessages((prev) => [...prev, outcomeMsg]);

          // Record turn and synthesize updated ground-truth context
          await recordTurnAndPersistContext(
            lastUserQueryRef.current,
            finalOutcome || reviewDescription || 'Task completed.',
            selectedHosts[0] || undefined,
            'terminal',
            lastExecutedCmdRef.current || undefined,
            lastExecutionOutputRef.current || undefined,
            data.accumulated_context_data
          );

          // Query completed: automatically reset back to AUTO mode!
          resetToAutoMode();
          triggerRunbookEvaluation(
            lastUserQueryRef.current,
            finalOutcome || reviewDescription || 'Task completed.',
            effectiveConvId || undefined
          );
          return;
        }
      }

      // STEP E: DATABASE QUERY GENERATION RESPONSE
      if (currentStep === 'DATABASE_QUERY_GENERATION') {
        if (data.accumulated_context_data && activeConvRef.current) {
          const sanitized = ContextAccumulatorService.sanitizeContext(
            data.accumulated_context_data,
            activeConvRef.current.accumulatedContextData || 'None'
          );
          activeConvRef.current.accumulatedContextData = sanitized;
          saveAiConversation(activeConvRef.current);
        }

        const sql = data.sql || null;
        const env: MssqlEnvironment = (data.environment && String(data.environment).toLowerCase() === 'test') ? 'test' : 'prod';
        const isReviewNeeded = Boolean(data.review_needed ?? false);

        if (sql) {
          const dbMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse,
            sql: sql,
            environment: env,
            reviewNeededDatabase: isReviewNeeded,
            reviewTitle: reviewTitle || (isReviewNeeded ? 'Review Proposed SQL Query' : 'Executing SQL Query'),
            reviewDescription: reviewDescription || (isReviewNeeded ? `Data modification detected. Please review before executing on ${env}.` : `Executing query on ${env}.`),
            finalOutcome: finalOutcome,
            timestamp: Date.now(),
            status: 'success',
          };

          if (effectiveConvId) await saveAiChatMessage(dbMsg);
          setMessages((prev) => [...prev, dbMsg]);

          if (isReviewNeeded) {
            setIsAwaitingDatabaseReview(true);
          } else {
            setIsAwaitingDatabaseReview(false);
            executeAiDatabaseQuery(dbMsg);
          }
          return;
        } else {
          // No SQL query needed or query resolved directly
          setIsAwaitingDatabaseReview(false);
          const outcomeMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: effectiveConvId || '',
            sender: 'ai',
            preResponse: preResponse,
            finalOutcome: finalOutcome || reviewDescription || 'Task completed.',
            timestamp: Date.now(),
            status: 'success',
          };
          if (effectiveConvId) await saveAiChatMessage(outcomeMsg);
          setMessages((prev) => [...prev, outcomeMsg]);

          await recordTurnAndPersistContext(
            lastUserQueryRef.current,
            finalOutcome || reviewDescription || 'Task completed.',
            undefined,
            'database',
            undefined,
            undefined,
            data.accumulated_context_data
          );

          resetToAutoMode();
          triggerRunbookEvaluation(
            lastUserQueryRef.current,
            finalOutcome || reviewDescription || 'Task completed.',
            effectiveConvId || undefined
          );
          return;
        }
      }

      // STEP F: DATABASE RESULT SYNTHESIS RESPONSE
      if (currentStep === 'DATABASE_RESULT_SYNTHESIS') {
        if (data.accumulated_context_data && activeConvRef.current) {
          const sanitized = ContextAccumulatorService.sanitizeContext(
            data.accumulated_context_data,
            activeConvRef.current.accumulatedContextData || 'None'
          );
          activeConvRef.current.accumulatedContextData = sanitized;
          saveAiConversation(activeConvRef.current);
        }

        const answerText = finalOutcome || reviewDescription || 'Database query analysis completed.';
        const synthOutcomeMsg: AiChatMessageRecord = {
          id: `msg_${Date.now()}`,
          conversationId: effectiveConvId || '',
          sender: 'ai',
          preResponse: preResponse,
          finalOutcome: answerText,
          timestamp: Date.now(),
          status: 'success',
        };
        if (effectiveConvId) await saveAiChatMessage(synthOutcomeMsg);
        setMessages((prev) => [...prev, synthOutcomeMsg]);

        await recordTurnAndPersistContext(
          lastUserQueryRef.current,
          answerText,
          undefined,
          'database',
          undefined,
          undefined,
          data.accumulated_context_data
        );

        resetToAutoMode();
        triggerRunbookEvaluation(
          lastUserQueryRef.current,
          answerText,
          effectiveConvId || undefined
        );
        return;
      }
    }

    // 2. Health Status Events

    if (eventType === WS_EVENTS.AI_HEALTH_STATUS) {
      const healthStatus = String(frame.status || '').toUpperCase();
      const extsnConnected = frame.extsnConnected ?? true;

      if ((healthStatus === 'NOT_OK' || !extsnConnected) && isLoading) {
        if (taskTimeoutTimerRef.current) {
          clearTimeout(taskTimeoutTimerRef.current);
          taskTimeoutTimerRef.current = null;
        }
        setIsLoading(false);
        setStatusText('');
        pendingNewChatRef.current = null;
        currentPipelineStepRef.current = null;
        setIsAwaitingHostReview(false);

        const reason = !extsnConnected
          ? 'Chrome Extension disconnected from AI Agent.'
          : 'AI Agent Server (Port 9020) is disconnected.';

        const healthErrMsg: AiChatMessageRecord = {
          id: `msg_${Date.now()}`,
          conversationId: activeConvIdRef.current || '',
          sender: 'ai',
          text: `Connection Warning: ${reason}`,
          timestamp: Date.now(),
          status: 'error',
        };
        if (activeConvIdRef.current) saveAiChatMessage(healthErrMsg);
        setMessages((prev) => [...prev, healthErrMsg]);
      }
    }

    // 3. Isolated Terminal Execution Stream Filtering
    const incomingTermId = frame.terminalid || frame.terminalId;
    const activeExec = activeTerminalExecRef.current;
    if (activeExec && incomingTermId === activeExec.terminalId) {
      if (eventType === WS_EVENTS.TERMINAL_STATUS) {
        if (frame.state === 'IDLE' && activeExec && !activeExec.hasExecuted && activeExec.command) {
          activeExec.hasExecuted = true;
          setStatusText(`Executing ${activeExec.platform} command on ${activeExec.host}...`);
          globalWebSocket.send({
            'event-type': WS_EVENTS.TERMINAL_EXECUTE,
            terminalid: activeExec.terminalId,
            host: activeExec.host,
            userId: 'user',
            command: activeExec.command,
          });
        }
      }

      if (eventType === WS_EVENTS.TERMINAL_RESULT) {
        if (frame.chunk) {
          activeExec.outputBuffer += frame.chunk;
          const cleanChunk = frame.chunk.replace(/\u001b\[[0-9;]*[a-zA-Z]/g, '').trim().replace(/[\r\n]+/g, ' ');
          if (cleanChunk) {
            setStatusText(`Streaming terminal output: ${cleanChunk.substring(0, 40)}...`);
          }
        }

        if (frame.end) {
          await completeTerminalExecution(activeExec);
        }
      }

      if (eventType === WS_EVENTS.TERMINAL_ERROR) {
        const errorOutput = frame.chunk || '';

        // 1. BENIGN CASE: Terminal is already running
        // Good sign! Terminal is active and ready -> Proceed immediately to execute!
        if (errorOutput.includes('Terminal is already running')) {
          logger.info('AsrAiDashboardPage', 'handleIncomingFrame', 'Terminal already active; proceeding directly to execute.');
          if (activeExec && !activeExec.hasExecuted && activeExec.command) {
            activeExec.hasExecuted = true;
            setStatusText(`Executing ${activeExec.platform} command on ${activeExec.host}...`);
            globalWebSocket.send({
              'event-type': WS_EVENTS.TERMINAL_EXECUTE,
              terminalid: activeExec.terminalId,
              host: activeExec.host,
              userId: 'user',
              command: activeExec.command,
            });
          }
          return;
        }

        // 2. BENIGN CASE: Terminal is closed or not found
        // Good sign! Post-output close confirmed or clean slate -> Silently ignore!
        if (
          errorOutput.includes('Terminal is closed') ||
          errorOutput.includes('Terminal not found')
        ) {
          logger.info('AsrAiDashboardPage', 'handleIncomingFrame', 'Terminal close confirmed/clean; ignoring benign signal.');
          return;
        }

        // 3. RECOVERY CASE: Terminal is busy -> Stop lingering task and retry execute
        if (frame.errorCode === 'TERMINAL_BUSY') {
          logger.warn('AsrAiDashboardPage', 'handleIncomingFrame', 'Terminal was busy; interrupting previous command and retrying...');
          globalWebSocket.send({
            'event-type': WS_EVENTS.TERMINAL_STOP,
            terminalid: activeExec.terminalId,
            host: activeExec.host,
            userId: 'user',
          });
          setTimeout(() => {
            if (activeTerminalExecRef.current) {
              globalWebSocket.send({
                'event-type': WS_EVENTS.TERMINAL_EXECUTE,
                terminalid: activeExec.terminalId,
                host: activeExec.host,
                userId: 'user',
                command: activeExec.command,
              });
            }
          }, 400);
          return;
        }

        // 4. REAL INFRASTRUCTURE ERROR: Explicit agent protocol error code
        if (frame.errorCode && frame.errorCode !== 'null') {
          logger.error('AsrAiDashboardPage', 'handleIncomingFrame', 'Terminal agent error code received', {
            errorCode: frame.errorCode,
            errorOutput,
          });
          if (taskTimeoutTimerRef.current) {
            clearTimeout(taskTimeoutTimerRef.current);
            taskTimeoutTimerRef.current = null;
          }
          setIsLoading(false);
          setStatusText('');
          isExecutingRef.current = false;

          globalWebSocket.send({
            'event-type': WS_EVENTS.TERMINAL_STOP,
            terminalid: activeExec.terminalId,
            host: activeExec.host,
            userId: 'user',
          });
          globalWebSocket.send({
            'event-type': WS_EVENTS.TERMINAL_CLOSE,
            terminalid: activeExec.terminalId,
            host: activeExec.host,
            userId: 'user',
          });

          const currentConvId = activeConvIdRef.current;
          activeTerminalExecRef.current = null;

          const termErrMsg: AiChatMessageRecord = {
            id: `msg_${Date.now()}`,
            conversationId: currentConvId || '',
            sender: 'ai',
            text: `Terminal Infrastructure Error (${frame.errorCode}): ${errorOutput || 'Process or connection failed'}`,
            timestamp: Date.now(),
            status: 'error',
          };
          if (currentConvId) await saveAiChatMessage(termErrMsg);
          setMessages((prev) => [...prev, termErrMsg]);
          resetToAutoMode();
          return;
        }

        // 5. LIVE COMMAND STDERR OUTPUT (errorCode is null or omitted)
        // Stderr from commands (PowerShell/CMD warnings, syntax errors, etc.) is valid diagnostic output
        if (frame.chunk) {
          activeExec.outputBuffer += frame.chunk;
          const cleanChunk = frame.chunk.replace(/\u001b\[[0-9;]*[a-zA-Z]/g, '').trim().replace(/[\r\n]+/g, ' ');
          if (cleanChunk) {
            setStatusText(`Streaming terminal output: ${cleanChunk.substring(0, 40)}...`);
          }
        }

        if (frame.end) {
          await completeTerminalExecution(activeExec);
        }
      }
    }

    // 4. Isolated MSSQL Query Execution Stream Filtering
    const incomingDbTaskId = frame.taskId || frame.task_id;
    const activeDbExec = activeDatabaseExecRef.current;
    if (activeDbExec && incomingDbTaskId === activeDbExec.taskId) {
      const type = frame.type;
      if (type === 'connecting') {
        setStatusText(`Connecting to MSSQL (${activeDbExec.environment})...`);
      } else if (type === 'connected') {
        setStatusText(`Connected to MSSQL. Executing query...`);
      } else if (type === 'querySent') {
        setStatusText(`Query sent. Waiting for data...`);
      } else if (type === 'output') {
        const incomingRows: any[] = frame.data || [];
        if (incomingRows.length > 0) {
          activeDbExec.results.push(...incomingRows);
        }
        if (frame.rowCount !== undefined) {
          activeDbExec.rowCount = frame.rowCount;
        } else {
          activeDbExec.rowCount = activeDbExec.results.length;
        }
        if (frame.durationMs !== undefined) {
          activeDbExec.durationMs = frame.durationMs;
        }
        if (frame.error) {
          activeDbExec.error = frame.error;
        }
        setStatusText(`Received ${activeDbExec.results.length} rows...`);

        if (frame.end) {
          await completeDatabaseExecution(activeDbExec);
        }
      } else if (type === 'closed') {
        if (!frame.end && activeDbExec.error) {
          await completeDatabaseExecution(activeDbExec);
        }
      }
    }
  };


  const handleSendQuery = async (queryText: string) => {
    if (isLoading) return;

    setIsLoading(true);
    setStatusText('Analyzing query intent & routing...');

    const currentConvId = activeConvIdRef.current;
    lastUserQueryRef.current = queryText;

    // 1. Prepare user message
    const userMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: currentConvId || '',
      sender: 'user',
      text: queryText,
      timestamp: Date.now(),
    };

    if (currentConvId) {
      await saveAiChatMessage(userMsg);
    } else {
      pendingNewChatRef.current = {
        queryText,
        userMsg,
        targetHosts: selectedHosts,
      };
    }

    setMessages((prev) => [...prev, userMsg]);

    const isDashboardAuto = selectedDashboards.length === 0 || selectedDashboards.includes('auto');

    // If Dashboard is in AUTO mode, dispatch Feature Selection Prompt with accumulated context
    if (isDashboardAuto) {
      currentPipelineStepRef.current = 'FEATURE_SELECTION';
      setStatusText('Routing query intent & selecting dashboard feature...');

      const featurePrompt = PromptBuilder.buildFeatureSelectionPrompt(
        queryText,
        activeConvRef.current?.accumulatedContextData
      );

      // Refresh Runbook Root Path from AI Agent backend
      globalWebSocket.send({ 'event-type': WS_EVENTS.AI_RUNBOOKS_ROOT_PATH });

      const activeUser = await getActiveAuthUserInfo();
      const effectiveUserId = activeUser?.userId || 'sahoos';
      const rootPath = runbookRootPathRef.current || 'C:/Ranjan/AutomationSuite_v2/Runbooks';
      const runbookCatalogFiles = [
        `${rootPath}\\Contents.md`,
        `${rootPath}\\${effectiveUserId}_contents.md`
      ];

      const sent = globalWebSocket.send({
        'event-type': WS_EVENTS.AI_EXECUTE_TASK,
        conversationId: currentConvId || null,
        prompt: featurePrompt,
        query: queryText,
        targetHosts: selectedHosts,
        dashboardId: 'auto',
        userId: effectiveUserId,
        attach_runbook_catalog: true,
        files_needed: runbookCatalogFiles,
        files_path: runbookCatalogFiles,
      });

      if (!sent) {
        handleWebSocketSendError(currentConvId);
        return;
      }
      startTaskTimeoutGuard(currentConvId);
      return;
    }

    // Dashboard is in MANUAL mode (e.g., Database or Terminal manually locked)
    if (selectedDashboards.includes('database')) {
      triggerDatabaseQueryGeneration(queryText, currentConvId || undefined);
      return;
    }

    if (selectedHosts.length === 0) {
      triggerHostResolution(queryText, currentConvId || undefined);
    } else {
      triggerCommandCreation(selectedHosts, currentConvId || undefined);
    }
  };

  const handleStopExecution = () => {
    if (taskTimeoutTimerRef.current) {
      clearTimeout(taskTimeoutTimerRef.current);
      taskTimeoutTimerRef.current = null;
    }

    if (activeTerminalExecRef.current) {
      globalWebSocket.send({
        'event-type': WS_EVENTS.TERMINAL_STOP,
        terminalid: activeTerminalExecRef.current.terminalId,
        host: activeTerminalExecRef.current.host,
        userId: 'user',
      });
      globalWebSocket.send({
        'event-type': WS_EVENTS.TERMINAL_CLOSE,
        terminalid: activeTerminalExecRef.current.terminalId,
        host: activeTerminalExecRef.current.host,
        userId: 'user',
      });
      activeTerminalExecRef.current = null;
    }

    if (activeDatabaseExecRef.current) {
      activeDatabaseExecRef.current = null;
    }

    setIsLoading(false);
    setStatusText('');
    isExecutingRef.current = false;
    pendingNewChatRef.current = null;
    currentPipelineStepRef.current = null;
    setIsAwaitingHostReview(false);
    setIsAwaitingCommandReview(false);
    setIsAwaitingDatabaseReview(false);


    const stopMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: activeConvIdRef.current || '',
      sender: 'system',
      text: 'Execution stopped by user.',
      timestamp: Date.now(),
    };
    setMessages((prev) => [...prev, stopMsg]);
  };

  // Dedicated execution using reserved AI terminal IDs
  const executeTerminalCommand = async (msg: AiChatMessageRecord) => {
    if (!msg.command) return;

    setIsLoading(true);
    isExecutingRef.current = true;

    const targetHost = selectedHosts[0] || (msg.targetHosts && msg.targetHosts[0]) || 'localhost';
    const platform = msg.platform === 'CMD' ? 'CMD' : 'PS';
    const activeUser = await getActiveAuthUserInfo();
    if (!activeUser || !activeUser.userId) {
      alert('You are not authorized. Your session has expired, please log in.');
      setIsLoading(false);
      isExecutingRef.current = false;
      return;
    }
    const userId = activeUser.userId;
    const terminalId = `${userId}_ASR_AI_${platform}_${targetHost}`;
    const terminalName = `ASR_AI_${platform}`;

    setStatusText(`Starting ${platform} terminal on ${targetHost}...`);

    activeTerminalExecRef.current = {
      terminalId,
      host: targetHost,
      userId,
      platform,
      command: msg.command,
      outputBuffer: '',
      convId: activeConvIdRef.current || msg.conversationId,
      hasExecuted: false,
    };

    // Pre-execution clean slate: stop any lingering previous command on this terminal
    globalWebSocket.send({
      'event-type': WS_EVENTS.TERMINAL_STOP,
      terminalid: terminalId,
      host: targetHost,
      userId: userId,
    });

    // Arm 15s execution timeout guard: if command hangs or takes longer than 15s, stop and finalize
    if (taskTimeoutTimerRef.current) clearTimeout(taskTimeoutTimerRef.current);
    taskTimeoutTimerRef.current = setTimeout(() => {
      const current = activeTerminalExecRef.current;
      if (current && isExecutingRef.current) {
        logger.warn('AsrAiDashboardPage', 'executeTerminalCommand', 'Command reached 15s timeout limit; stopping process and finalizing output');
        globalWebSocket.send({
          'event-type': WS_EVENTS.TERMINAL_STOP,
          terminalid: current.terminalId,
          host: current.host,
          userId: current.userId || userId,
        });
        if (!current.outputBuffer.trim()) {
          current.outputBuffer = 'Command execution exceeded 15s timeout.';
        }
        completeTerminalExecution(current);
      }
    }, 15000);

    // STEP 1: Direct terminal_start
    globalWebSocket.send({
      'event-type': WS_EVENTS.TERMINAL_START,
      terminalid: terminalId,
      host: targetHost,
      userId: userId,
      platform: platform,
      name: terminalName,
    });

    // STEP 2: Execute command after brief spin-up (unless fast-pathed by 'TERMINAL_STATUS' or 'already running')
    setTimeout(() => {
      const current = activeTerminalExecRef.current;
      if (!isExecutingRef.current || !current || current.hasExecuted) return;
      current.hasExecuted = true;
      setStatusText(`Executing ${platform} command on ${targetHost}...`);
      globalWebSocket.send({
        'event-type': WS_EVENTS.TERMINAL_EXECUTE,
        terminalid: terminalId,
        host: targetHost,
        userId: current.userId || userId,
        command: msg.command!,
      });
    }, 400);
  };

  const handleApproveCommand = (msg: AiChatMessageRecord) => {
    setIsAwaitingCommandReview(false);
    executeTerminalCommand(msg);
  };

  const handleDeclineCommand = async (msg: AiChatMessageRecord) => {
    setIsAwaitingCommandReview(false);
    setIsLoading(false);
    setStatusText('');
    currentPipelineStepRef.current = null;

    const currentConvId = activeConvIdRef.current || msg.conversationId || '';
    const declineMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: currentConvId,
      sender: 'ai',
      text: 'Command execution declined. Please let me know how you would like to proceed or if you want to refine the command.',
      timestamp: Date.now(),
      status: 'success',
    };
    if (currentConvId) {
      await saveAiChatMessage(declineMsg);
    }
    setMessages((prev) => [...prev, declineMsg]);
  };

  // User clicked "Agree & Proceed (Yes)" on Host Review Banner
  const handleApproveHost = (msg: AiChatMessageRecord) => {
    setIsAwaitingHostReview(false);
    if (msg.targetHosts && msg.targetHosts.length > 0) {
      handleApplySelectedHosts(msg.targetHosts);
      triggerCommandCreation(msg.targetHosts, activeConvIdRef.current || msg.conversationId);
    }
  };

  // User clicked "Decline (No)" on Host Review Banner
  const handleDeclineHost = async (msg: AiChatMessageRecord) => {
    setIsAwaitingHostReview(false);
    setIsLoading(false);
    setStatusText('');
    currentPipelineStepRef.current = null;

    const currentConvId = activeConvIdRef.current || msg.conversationId || '';
    const declineMsg: AiChatMessageRecord = {
      id: `msg_${Date.now()}`,
      conversationId: currentConvId,
      sender: 'ai',
      text: 'Could you please share some details about the selection? I can help find the exact hosts for you, or you can choose the host manually from the dropdown above.',
      timestamp: Date.now(),
      status: 'success',
    };
    if (currentConvId) {
      await saveAiChatMessage(declineMsg);
    }
    setMessages((prev) => [...prev, declineMsg]);
  };

  return (
    <div style={{ display: 'flex', width: '100%', height: '100vh', background: '#09090b', overflow: 'hidden' }}>
      {/* Main Conversation & Control Area */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100%', minWidth: 0 }}>
        <TopControlBar
          servers={servers}
          selectedHosts={selectedHosts}
          onApplySelectedHosts={handleApplySelectedHosts}
          selectedDashboards={selectedDashboards}
          onApplySelectedDashboards={(dashboards) => setSelectedDashboards(dashboards)}
        />

        <ChatThread
          messages={messages}
          isLoading={isLoading}
          statusText={statusText}
          onApproveCommand={handleApproveCommand}
          onApproveHost={handleApproveHost}
          onDeclineHost={handleDeclineHost}
          onDeclineCommand={handleDeclineCommand}
          onApproveDatabase={handleApproveDatabase}
          onDeclineDatabase={handleDeclineDatabase}
        />

        <InputBox
          onSendQuery={handleSendQuery}
          onStopExecution={handleStopExecution}
          isLoading={isLoading}
          disabled={isAwaitingHostReview || isAwaitingCommandReview || isAwaitingDatabaseReview}
          disabledPlaceholder={
            isAwaitingHostReview
              ? "🔒 Please confirm or decline the target host above..."
              : isAwaitingCommandReview
              ? "🔒 Please review or decline the proposed command above..."
              : isAwaitingDatabaseReview
              ? "🔒 Please review or decline the proposed SQL query above..."
              : undefined
          }
        />

      </div>

      {/* Right-Side Conversation Drawer */}
      <ConversationList
        conversations={conversations}
        activeConvId={activeConvId}
        onSelectConversation={selectConversation}
        onNewChat={createNewChat}
        onRenameConversation={handleRenameConversation}
        onDeleteConversation={handleDeleteConversation}
      />
    </div>
  );
};
