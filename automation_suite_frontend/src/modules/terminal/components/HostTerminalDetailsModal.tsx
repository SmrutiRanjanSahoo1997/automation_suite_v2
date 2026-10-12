import React from 'react';
import { ServerConfig, AgentConnectionStatus, TerminalSession } from '../types/terminal.types';
import { Server, Wifi, WifiOff, Plus, Play, X, RotateCcw } from 'lucide-react';

interface Props {
  server: ServerConfig;
  statusMap: Record<string, AgentConnectionStatus>;
  sessions: TerminalSession[];
  onStartNewTerminal: (server: ServerConfig) => void;
  onSelectTerminal: (terminalId: string) => void;
  onCloseTerminal: (terminalId: string) => void;
  onClose: () => void;
}

export const HostTerminalDetailsModal: React.FC<Props> = ({
  server,
  statusMap,
  sessions,
  onStartNewTerminal,
  onSelectTerminal,
  onCloseTerminal,
  onClose,
}) => {
  const isConnected = statusMap[server.host]?.connected || false;
  const hostSessions = sessions
    .filter((s) => s.host === server.host)
    .sort((a, b) => (b.lastActivityAt || 0) - (a.lastActivityAt || 0));

  const openHostSessions = hostSessions.filter((s) => s.state !== 'CLOSED');
  const openManualSessions = openHostSessions.filter((s) => !s.terminalId.includes('_ASR_AI_') && !s.name.startsWith('ASR_AI'));
  const isMaxReached = openManualSessions.length >= 2 || openHostSessions.length >= 4;

  const getStateBadge = (state: string) => {
    switch (state) {
      case 'RUNNING': return <span style={{ color: '#eab308', background: 'rgba(234, 179, 8, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>RUNNING</span>;
      case 'IDLE': return <span style={{ color: '#22c55e', background: 'rgba(34, 197, 94, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>IDLE</span>;
      case 'CLOSING': return <span style={{ color: '#f97316', background: 'rgba(249, 115, 22, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>CLOSING</span>;
      case 'CLOSED': return <span style={{ color: '#ef4444', background: 'rgba(239, 68, 68, 0.15)', padding: '2px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 600 }}>CLOSED</span>;
      default: return null;
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" style={{ width: '640px', maxHeight: '85vh', display: 'flex', flexDirection: 'column' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '12px', borderBottom: '1px solid #334155' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Server size={22} color="#3b82f6" />
            <div>
              <div style={{ fontSize: '18px', fontWeight: 700, color: '#f8fafc' }}>{server.host}</div>
              <div style={{ fontSize: '12px', color: '#94a3b8' }}>
                {server.serverType || server.category} | {server.env || server.environment}
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {isConnected ? (
              <span title="Connected" style={{ display: 'flex', alignItems: 'center' }}>
                <Wifi size={18} color="#22c55e" />
              </span>
            ) : (
              <span title="Disconnected" style={{ display: 'flex', alignItems: 'center' }}>
                <WifiOff size={18} color="#ef4444" />
              </span>
            )}
            <button
              onClick={() => !isMaxReached && onStartNewTerminal(server)}
              disabled={isMaxReached}
              title={isMaxReached ? "Maximum limit of 4 terminals (2 AI + 2 Manual) reached for this host. Close an existing terminal to launch a new one." : undefined}
              style={{
                background: isMaxReached ? '#334155' : '#3b82f6',
                color: isMaxReached ? '#94a3b8' : '#ffffff',
                border: 'none',
                borderRadius: '6px',
                padding: '6px 12px',
                fontSize: '13px',
                fontWeight: 600,
                cursor: isMaxReached ? 'not-allowed' : 'pointer',
                opacity: isMaxReached ? 0.6 : 1,
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
              }}
            >
              <Plus size={14} /> + New Terminal
            </button>
          </div>
        </div>

        <div style={{ flex: 1, overflowY: 'auto', padding: '14px 0' }}>
          <div style={{ fontSize: '13px', fontWeight: 700, color: '#f8fafc', marginBottom: '10px' }}>
            All Terminals for Host ({hostSessions.length})
          </div>

          {hostSessions.length === 0 ? (
            <div style={{ fontSize: '13px', color: '#94a3b8', fontStyle: 'italic', padding: '16px 0', textAlign: 'center' }}>
              No terminal sessions created for this host yet. Click "+ New Terminal" to launch one.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {hostSessions.map((sess) => (
                <div
                  key={sess.terminalId}
                  style={{
                    background: 'rgba(255,255,255,0.03)',
                    border: '1px solid #334155',
                    borderRadius: '8px',
                    padding: '12px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <span style={{ fontWeight: 700, color: '#f8fafc', fontSize: '14px' }}>
                        {sess.elevated ? 'Administrator: ' : ''}{sess.name}
                      </span>
                      <span style={{ fontSize: '12px', color: '#94a3b8', fontWeight: 600 }}>({sess.platform})</span>
                      {getStateBadge(sess.state)}
                    </div>
                    <div style={{ fontSize: '11px', color: '#64748b', fontFamily: 'monospace' }}>
                      ID: {sess.terminalId} | Path: {sess.currentPath || 'C:\\'}
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <button
                      onClick={() => {
                        onSelectTerminal(sess.terminalId);
                        onClose();
                      }}
                      style={{
                        background: sess.state === 'CLOSED' ? '#059669' : '#3b82f6',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: '4px',
                        padding: '6px 12px',
                        fontSize: '12px',
                        fontWeight: 600,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                      }}
                    >
                      <Play size={12} /> Resume
                    </button>

                    {sess.state !== 'CLOSED' && (
                      <button
                        onClick={() => onCloseTerminal(sess.terminalId)}
                        style={{
                          background: 'rgba(239, 68, 68, 0.15)',
                          color: '#ef4444',
                          border: '1px solid #ef4444',
                          borderRadius: '4px',
                          padding: '5px 10px',
                          fontSize: '12px',
                          cursor: 'pointer',
                        }}
                      >
                        Close
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="dialog-actions" style={{ paddingTop: '12px', borderTop: '1px solid #334155' }}>
          <button onClick={onClose} style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer' }}>
            Close Dialog
          </button>
        </div>
      </div>
    </div>
  );
};
