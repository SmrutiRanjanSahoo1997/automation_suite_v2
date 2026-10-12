import React, { useState, useEffect, useRef } from 'react';
import { ServerConfig, AgentConnectionStatus, TerminalSession, ServerCategory, Environment, TerminalPlatform } from './types/terminal.types';
import { fetchServerInventory, fetchServerStatus } from '../../network/api';
import { globalWebSocket } from '../../network/globalWebSocket';
import { HeaderServerFilter } from './components/HeaderServerFilter';
import { ServerCardGrid } from './components/ServerCardGrid';
import { TerminalModalWindow } from './components/TerminalModalWindow';
import { StartTerminalModal } from './components/StartTerminalModal';
import { ServerFilterDialog } from './components/ServerFilterDialog';
import { HostTerminalDetailsModal } from './components/HostTerminalDetailsModal';
import { TerminalConsoleCanvas } from './components/TerminalConsoleCanvas';
import { getTerminalCache, setTerminalCache, appendTerminalCache, clearTerminalCache } from './services/terminalCache';
import { AiConversationRecord, getAllAiConversations } from '../ai/repository/aiConversationRepo';
import { getActiveAuthUserInfo } from '../auth/repository/authRepo';
import { logger } from '../../utils/logger';

interface ChunkTracker {
  lastCount: number;
  ended: boolean;
}

function shouldAcceptChunk(
  trackers: Map<string, ChunkTracker>,
  key: string,
  count: number,
  end: boolean
): boolean {
  const t = trackers.get(key);
  if (!t || t.ended) {
    trackers.set(key, { lastCount: count, ended: end });
    return true;
  }
  if (count > t.lastCount) {
    trackers.set(key, { lastCount: count, ended: end });
    return true;
  }
  return false;
}

export const TerminalDashboardPage: React.FC = () => {
  const [servers, setServers] = useState<ServerConfig[]>([]);
  const [statusMap, setStatusMap] = useState<Record<string, AgentConnectionStatus>>({});
  const [categoryFilter] = useState<ServerCategory | 'ALL'>('ALL');
  const [envFilter] = useState<Environment | 'ALL'>('ALL');
  const [searchHost] = useState('');

  // Selected hosts from V1 ServerFilterDialog
  const [selectedTreeHosts, setSelectedTreeHosts] = useState<string[]>([]);
  const [isFilterDialogOpen, setIsFilterDialogOpen] = useState(false);
  const hasInitializedFilterRef = useRef(false);

  // Start Terminal pop-up target server
  const [startModalServer, setStartModalServer] = useState<ServerConfig | null>(null);

  // Host Terminal Details pop-up target server
  const [detailsModalServer, setDetailsModalServer] = useState<ServerConfig | null>(null);

  // Authenticated User ID
  const [currentUserId, setCurrentUserId] = useState<string>('');

  // AI Assistant States
  const [isAiOnline, setIsAiOnline] = useState(false);
  const [aiConversations, setAiConversations] = useState<AiConversationRecord[]>([]);

  const [sessions, setSessions] = useState<TerminalSession[]>([]);
  const [activeTerminalId, setActiveTerminalId] = useState<string | null>(null);
  const frameTrackerRef = useRef(new Map<string, ChunkTracker>());

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, []);

  const loadData = async () => {
    try {
      const activeUser = await getActiveAuthUserInfo();
      if (activeUser && activeUser.userId) {
        setCurrentUserId(activeUser.userId);
      }

      const inv = await fetchServerInventory();
      setServers(inv);
      if (!hasInitializedFilterRef.current && inv.length > 0) {
        setSelectedTreeHosts(inv.map((s) => s.host));
        hasInitializedFilterRef.current = true;
      }
      const st = await fetchServerStatus();
      setStatusMap(st);

      // Load AI conversations from IndexedDB
      getAllAiConversations().then(setAiConversations).catch(() => {});

      // Send metadata_list request for all connected hosts to pull active terminal sessions
      inv.forEach((srv) => {
        globalWebSocket.send({
          'event-type': 'metadata_list',
          host: srv.host,
        });
      });
    } catch (err) {
      console.warn('Error fetching inventory/status:', err);
    }
  };

  useEffect(() => {
    const unsubscribe = globalWebSocket.subscribe((frame: any) => {
      handleIncomingFrame(frame);
    });
    return unsubscribe;
  }, []);

  const handleIncomingFrame = (frame: any) => {
    if (frame['event-type'] === 'ai_health_status') {
      setIsAiOnline(frame.status === 'OK' && Boolean(frame.extsnConnected));
      return;
    }

    if (frame['event-type'] === 'terminal_history') {
      const tid = frame.terminalid || frame.terminalId;
      const lines = (frame as any).lines;
      if (tid && Array.isArray(lines)) {
        const fullLines = lines.length > 0 ? lines : [`[System] Loaded history for ${tid.substring(0, 8)}...`];
        setTerminalCache(tid, fullLines);
        setSessions((prev) =>
          prev.map((s) => (s.terminalId === tid ? { ...s, outputLines: fullLines } : s))
        );
      }
      return;
    }

    if (frame['event-type'] === 'metadata_list') {
      const rawTerminals = frame.terminals || (frame as any).terminals;
      if (Array.isArray(rawTerminals)) {
        const historyReqs: string[] = [];

        rawTerminals.forEach((item: any) => {
          const tid = item.terminalId || item.terminalid;
          if (!tid) return;
          const cachedLines = getTerminalCache(tid);
          if (!cachedLines || cachedLines.length === 0) {
            const restoredLines = [`[System] Loaded session ${item.name || tid.substring(0, 8)} (${item.platform || 'PS'})...`];
            setTerminalCache(tid, restoredLines);
            historyReqs.push(tid);
          }
        });

        historyReqs.forEach((tid) => {
          globalWebSocket.send({
            'event-type': 'terminal_history',
            terminalid: tid,
          });
        });

        setSessions((prev) => {
          const next = [...prev];
          rawTerminals.forEach((item: any) => {
            const tid = item.terminalId || item.terminalid;
            if (!tid) return;
            const itemHost = item.host || (frame as any).host || 'localhost';

            // Filter out legacy non-canonical rogue ASR_AI terminals (e.g. sahoos_ASR_AI_1a2b3c)
            if (tid.includes('_ASR_AI_')) {
              const isCanonicalAi = /^.+_ASR_AI_(?:PS|CMD)_.+$/.test(tid);
              if (!isCanonicalAi) {
                return; // skip rogue historical AI terminal
              }
              item.name = tid.includes('_ASR_AI_CMD_') ? 'ASR_AI_CMD' : 'ASR_AI_PS';
            }

            const existingIdx = next.findIndex((s) => s.terminalId === tid);
            const cachedLines = getTerminalCache(tid);

            if (existingIdx >= 0) {
              const s = next[existingIdx];
              const updatedLines = cachedLines && cachedLines.length > 0
                ? cachedLines
                : s.outputLines;

              next[existingIdx] = {
                ...s,
                state: item.state || s.state,
                currentPath: item.currentPath || s.currentPath,
                elevated: item.elevated !== undefined ? Boolean(item.elevated) : s.elevated,
                platform: item.platform || s.platform,
                name: item.name || s.name,
                lastCommand: item.lastCommand !== undefined ? item.lastCommand : s.lastCommand,
                lastResult: item.lastResult !== undefined ? item.lastResult : s.lastResult,
                lastError: item.lastError !== undefined ? item.lastError : s.lastError,
                outputLines: updatedLines,
              };
            } else {
              const restoredLines = cachedLines && cachedLines.length > 0
                ? cachedLines
                : [`[System] Loaded session ${item.name || tid.substring(0, 8)} (${item.platform || 'PS'})...`];

              next.push({
                terminalId: tid,
                name: item.name || `Terminal (${tid.substring(0, 8)})`,
                host: itemHost,
                platform: item.platform || 'PS',
                elevated: Boolean(item.elevated),
                state: item.state || 'CLOSED',
                currentPath: item.currentPath || 'C:\\',
                lastCommand: item.lastCommand || null,
                lastResult: item.lastResult || null,
                lastError: item.lastError || null,
                outputLines: restoredLines,
                history: item.lastCommand ? [item.lastCommand] : [],
                historyIndex: item.lastCommand ? 1 : -1,
              });
            }
          });
          return next;
        });
      }
      return;
    }

    const terminalId = frame.terminalid || frame.terminalId;
    if (!terminalId) return;

    const frameType = frame['event-type'];
    if (frameType === 'terminal_result' || frameType === 'terminal_error' || frameType === 'terminal_status') {
      logger.info('TerminalDashboardPage.tsx', 'handleIncomingFrame', `Received [${frameType}] frame`, { terminalId, chunk: frame.chunk, end: frame.end, state: frame.state, frame });
    }
    const isStreamChunk = (frameType === 'terminal_result' || frameType === 'terminal_error') && Boolean(frame.chunk);
    const chunkCount = typeof frame.count === 'number' ? frame.count : -1;
    if (isStreamChunk && chunkCount >= 0) {
      const key = `${frameType}:${terminalId}`;
      if (!shouldAcceptChunk(frameTrackerRef.current, key, chunkCount, Boolean(frame.end))) {
        return;
      }
    }
    if (frameType === 'terminal_status' && frame.state === 'RUNNING') {
      frameTrackerRef.current.delete(`terminal_result:${terminalId}`);
      frameTrackerRef.current.delete(`terminal_error:${terminalId}`);
    }

    let updatedLinesForChunk: string[] | null = null;
    if (frame['event-type'] === 'terminal_result' && frame.chunk) {
      appendTerminalCache(terminalId, frame.chunk);
      updatedLinesForChunk = getTerminalCache(terminalId) || null;
    } else if (frame['event-type'] === 'terminal_error' && frame.chunk) {
      const errChunk = `[ERROR] ${frame.chunk}`;
      appendTerminalCache(terminalId, errChunk);
      updatedLinesForChunk = getTerminalCache(terminalId) || null;
    }

    setSessions((prev) =>
      prev.map((s) => {
        if (s.terminalId !== terminalId) return s;

        const next = { ...s, lastActivityAt: Date.now() };

        if (updatedLinesForChunk) {
          next.outputLines = updatedLinesForChunk;
        }

        if (frame['event-type'] === 'terminal_status') {
          if (frame.state) next.state = frame.state;
          if (frame.currentPath) next.currentPath = frame.currentPath;
          if (frame.elevated !== undefined) next.elevated = frame.elevated;
        }

        return next;
      })
    );
  };

  const handleStartTerminalSubmit = async (server: ServerConfig, platform: TerminalPlatform) => {
    let userId = currentUserId;
    if (!userId) {
      const activeUser = await getActiveAuthUserInfo();
      if (activeUser && activeUser.userId) {
        userId = activeUser.userId;
        setCurrentUserId(userId);
      }
    }
    if (!userId) {
      alert('You are not authorized. Your session has expired, please log in.');
      return;
    }

    const hostSessions = sessions.filter((s) => s.host === server.host && s.state !== 'CLOSED');
    const manualHostSessions = hostSessions.filter((s) => !s.terminalId.includes('_ASR_AI_') && !s.name.startsWith('ASR_AI'));

    if (manualHostSessions.length >= 2 || hostSessions.length >= 4) {
      alert(`Maximum limit of 4 active terminals (2 AI + 2 Manual) reached for host ${server.host}. Please close an existing terminal first.`);
      return;
    }

    const usedSeqs = sessions
      .filter((s) => s.host === server.host && s.platform === platform)
      .map((s) => {
        const m = s.name.match(new RegExp(`^TERMINAL_${platform}_(\\d+)$`));
        return m ? parseInt(m[1], 10) : 0;
      })
      .filter((n) => n > 0);

    let sequence = 1;
    while (usedSeqs.includes(sequence)) {
      sequence++;
    }

    const name = `TERMINAL_${platform}_${sequence}`;
    const terminalId = `${userId}_TERMINAL_${platform}_${server.host}_${sequence}`;

    const newSession: TerminalSession = {
      terminalId,
      name,
      host: server.host,
      platform,
      state: 'IDLE',
      currentPath: 'C:\\',
      lastCommand: null,
      lastResult: null,
      lastError: null,
      outputLines: [`[System] Connecting to ${server.host} (${platform})...`],
      history: [],
      historyIndex: -1,
      lastActivityAt: Date.now(),
    };

    setTerminalCache(terminalId, newSession.outputLines);

    setSessions((prev) => [...prev, newSession]);
    setActiveTerminalId(terminalId);
    setStartModalServer(null);

    globalWebSocket.send({
      'event-type': 'terminal_start',
      terminalid: terminalId,
      host: server.host,
      userId: userId,
      platform: platform,
      name: name,
    });
  };

  const handleExecute = (terminalId: string, host: string, command: string) => {
    const target = sessions.find((s) => s.terminalId === terminalId);
    if (!target) return;

    const currentPath = target.currentPath && target.currentPath.trim() ? target.currentPath : 'C:\\';
    const promptPrefix = target.platform === 'PS' ? `PS ${currentPath}>` : `${currentPath}>`;
    const commandLine = command ? `${promptPrefix} ${command}` : promptPrefix;
    const isNonEmpty = Boolean(command && command.trim());

    appendTerminalCache(terminalId, commandLine);
    const updatedLines = getTerminalCache(terminalId) || [...target.outputLines, commandLine];

    setSessions((prev) =>
      prev.map((s) => {
        if (s.terminalId !== terminalId) return s;

        return {
          ...s,
          outputLines: updatedLines,
          history: isNonEmpty ? [...s.history, command] : s.history,
          historyIndex: isNonEmpty ? s.history.length + 1 : s.historyIndex,
          state: isNonEmpty ? 'RUNNING' : s.state,
          lastActivityAt: Date.now(),
        };
      })
    );

    if (command && command.trim()) {
      globalWebSocket.send({
        'event-type': 'terminal_execute',
        terminalid: terminalId,
        host: host,
        userId: currentUserId || 'user',
        command: command,
      });
    }
  };

  const handleStop = (terminalId: string, host: string) => {
    globalWebSocket.send({
      'event-type': 'terminal_stop',
      terminalid: terminalId,
      host: host,
    });
  };

  const handleSelectTerminal = (terminalId: string) => {
    const target = sessions.find((s) => s.terminalId === terminalId);
    const cachedLines = getTerminalCache(terminalId);

    if (target && target.state === 'CLOSED') {
      globalWebSocket.send({
        'event-type': 'terminal_start',
        terminalid: target.terminalId,
        host: target.host,
        userId: currentUserId || 'user',
        platform: target.platform,
        name: target.name,
        path: target.currentPath,
      });

      setSessions((prev) =>
        prev.map((s) =>
          s.terminalId === terminalId
            ? {
                ...s,
                state: 'IDLE',
                outputLines: cachedLines || [...s.outputLines, `[System] Re-opening terminal session ${s.name} (${s.platform})...`],
                lastActivityAt: Date.now(),
              }
            : s
        )
      );
    } else {
      setSessions((prev) =>
        prev.map((s) =>
          s.terminalId === terminalId
            ? {
                ...s,
                outputLines: cachedLines || s.outputLines,
                lastActivityAt: Date.now(),
              }
            : s
        )
      );
    }
    setActiveTerminalId(terminalId);
  };

  const handleCloseTab = (terminalId: string) => {
    const target = sessions.find((s) => s.terminalId === terminalId);
    if (target) {
      globalWebSocket.send({
        'event-type': 'terminal_close',
        terminalid: terminalId,
        host: target.host,
      });
    }

    clearTerminalCache(terminalId);
    frameTrackerRef.current.delete(`terminal_result:${terminalId}`);
    frameTrackerRef.current.delete(`terminal_error:${terminalId}`);

    setSessions((prev) =>
      prev.map((s) => (s.terminalId === terminalId ? { ...s, state: 'CLOSED', lastActivityAt: Date.now() } : s))
    );

    const remainingOpen = sessions.filter((s) => s.terminalId !== terminalId && s.state !== 'CLOSED');
    if (activeTerminalId === terminalId) {
      setActiveTerminalId(remainingOpen.length > 0 ? remainingOpen[remainingOpen.length - 1].terminalId : null);
    }
  };

  const filteredServers = servers.filter((s) => {
    const category = s.serverType || s.category;
    const env = s.env || s.environment;

    if (categoryFilter !== 'ALL' && category !== categoryFilter) return false;
    if (envFilter !== 'ALL' && env !== envFilter) return false;
    if (selectedTreeHosts.length > 0 && !selectedTreeHosts.includes(s.host)) return false;
    if (searchHost && !s.host.toLowerCase().includes(searchHost.toLowerCase()) && !s.displayName.toLowerCase().includes(searchHost.toLowerCase())) {
      return false;
    }
    return true;
  });

  const urlParams = new URLSearchParams(window.location.search);
  const standaloneTerminalId = urlParams.get('terminalId');

  if (standaloneTerminalId) {
    const standaloneSession = sessions.find((s) => s.terminalId === standaloneTerminalId) || {
      terminalId: standaloneTerminalId,
      name: `Terminal (${standaloneTerminalId.substring(0, 8)})`,
      host: 'localhost',
      platform: 'PS' as TerminalPlatform,
      state: 'IDLE' as const,
      currentPath: 'C:\\',
      lastCommand: null,
      lastResult: null,
      lastError: null,
      outputLines: getTerminalCache(standaloneTerminalId) || [`[System] Loaded terminal ${standaloneTerminalId}...`],
      history: [],
      historyIndex: -1,
    };

    return (
      <div style={{ width: '100vw', height: '100vh', background: '#0f172a', display: 'flex', flexDirection: 'column' }}>
        <div style={{ padding: '8px 16px', background: '#1e293b', borderBottom: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#f8fafc' }}>
          <div style={{ fontWeight: 600, fontSize: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: standaloneSession.state === 'RUNNING' ? '#eab308' : '#22c55e' }} />
            {standaloneSession.name} ({standaloneSession.platform}) - Standalone View
          </div>
          <button
            onClick={() => window.close()}
            style={{ background: '#334155', border: 'none', color: '#f8fafc', padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}
          >
            Close Window
          </button>
        </div>
        <div style={{ flex: 1, minHeight: 0 }}>
          <TerminalConsoleCanvas
            session={standaloneSession}
            onExecute={handleExecute}
            onStop={handleStop}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <HeaderServerFilter
        selectedCount={selectedTreeHosts.length}
        totalCount={servers.length}
        onOpenServerFilterDialog={() => setIsFilterDialogOpen(true)}
      />

      <ServerCardGrid
        servers={filteredServers}
        statusMap={statusMap}
        sessions={sessions}
        onOpenStartModal={(server) => setStartModalServer(server)}
        onOpenDetailsModal={(server) => setDetailsModalServer(server)}
        onSelectTerminal={handleSelectTerminal}
        onCloseTerminal={handleCloseTab}
      />

      <TerminalModalWindow
        sessions={sessions}
        activeTerminalId={activeTerminalId}
        onSelectTab={handleSelectTerminal}
        onCloseTab={handleCloseTab}
        onExecute={handleExecute}
        onStop={handleStop}
        onCloseWindow={() => {
          sessions.filter((s) => s.state !== 'CLOSED').forEach((s) => handleCloseTab(s.terminalId));
          setActiveTerminalId(null);
        }}
      />

      {startModalServer && (
        <StartTerminalModal
          server={startModalServer}
          nextSequence={{
            PS: (() => {
              const used = sessions
                .filter((s) => s.host === startModalServer.host && s.platform === 'PS')
                .map((s) => {
                  const m = s.name.match(/^TERMINAL_PS_(\d+)$/);
                  return m ? parseInt(m[1], 10) : 0;
                })
                .filter((n) => n > 0);
              let seq = 1;
              while (used.includes(seq)) seq++;
              return seq;
            })(),
            CMD: (() => {
              const used = sessions
                .filter((s) => s.host === startModalServer.host && s.platform === 'CMD')
                .map((s) => {
                  const m = s.name.match(/^TERMINAL_CMD_(\d+)$/);
                  return m ? parseInt(m[1], 10) : 0;
                })
                .filter((n) => n > 0);
              let seq = 1;
              while (used.includes(seq)) seq++;
              return seq;
            })(),
          }}
          onStart={handleStartTerminalSubmit}
          onClose={() => setStartModalServer(null)}
        />
      )}

      {detailsModalServer && (
        <HostTerminalDetailsModal
          server={detailsModalServer}
          statusMap={statusMap}
          sessions={sessions}
          onStartNewTerminal={(server) => {
            setDetailsModalServer(null);
            setStartModalServer(server);
          }}
          onSelectTerminal={handleSelectTerminal}
          onCloseTerminal={handleCloseTab}
          onClose={() => setDetailsModalServer(null)}
        />
      )}

      <ServerFilterDialog
        open={isFilterDialogOpen}
        onClose={() => setIsFilterDialogOpen(false)}
        servers={servers}
        selectedHosts={selectedTreeHosts}
        onApply={(hosts) => setSelectedTreeHosts(hosts)}
      />
    </div>
  );
};
