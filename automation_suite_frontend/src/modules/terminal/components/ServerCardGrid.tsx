import React from 'react';
import { ServerConfig, AgentConnectionStatus, TerminalSession } from '../types/terminal.types';
import { Server, Wifi, WifiOff, Plus, X } from 'lucide-react';

interface Props {
  servers: ServerConfig[];
  statusMap: Record<string, AgentConnectionStatus>;
  sessions: TerminalSession[];
  onOpenStartModal: (server: ServerConfig) => void;
  onOpenDetailsModal: (server: ServerConfig) => void;
  onSelectTerminal: (terminalId: string) => void;
  onCloseTerminal: (terminalId: string) => void;
}

const ENV_ORDER: Record<string, number> = {
  PROD: 0,
  T01: 1,
  T02: 2,
  T03: 3,
  T04: 4,
  DEV: 5,
};

const CAT_ORDER: Record<string, number> = {
  INSPIRE_AUTOMATION: 0,
  INSPIRE_SCALER: 1,
  IPS_POOL: 2,
  INTERACTIVE: 3,
};

export const ServerCardGrid: React.FC<Props> = ({
  servers,
  statusMap,
  sessions,
  onOpenStartModal,
  onOpenDetailsModal,
  onSelectTerminal,
  onCloseTerminal,
}) => {
  // Sort servers by Environment then Category
  const sortedServers = [...servers].sort((a, b) => {
    const envA = ENV_ORDER[a.env || a.environment] ?? 99;
    const envB = ENV_ORDER[b.env || b.environment] ?? 99;
    if (envA !== envB) return envA - envB;

    const catA = CAT_ORDER[a.serverType || a.category] ?? 99;
    const catB = CAT_ORDER[b.serverType || b.category] ?? 99;
    if (catA !== catB) return catA - catB;

    return a.host.localeCompare(b.host);
  });

  // Group sorted servers by Environment
  const envGroups: { env: string; servers: ServerConfig[] }[] = [];
  sortedServers.forEach((server) => {
    const envName = server.env || server.environment || 'DEV';
    let group = envGroups.find((g) => g.env === envName);
    if (!group) {
      group = { env: envName, servers: [] };
      envGroups.push(group);
    }
    group.servers.push(server);
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', padding: '24px' }}>
      {envGroups.map((group) => (
        <div key={group.env} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div
            style={{
              fontSize: '15px',
              fontWeight: 700,
              color: '#38bdf8',
              letterSpacing: '0.05em',
              borderBottom: '1px solid #334155',
              paddingBottom: '6px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <span>Environment: {group.env}</span>
            <span style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>
              ({group.servers.length} {group.servers.length === 1 ? 'host' : 'hosts'})
            </span>
          </div>

          <div className="grid-container" style={{ padding: 0 }}>
            {group.servers.map((server) => {
              const status = statusMap[server.host];
              const isConnected = status ? status.connected : false;
              const allHostSessions = sessions
                .filter((s) => s.host === server.host)
                .sort((a, b) => (b.lastActivityAt || 0) - (a.lastActivityAt || 0));
              const recentSessions = allHostSessions.slice(0, 5);
              const openHostSessions = allHostSessions.filter((s) => s.state !== 'CLOSED');
              const openManualSessions = openHostSessions.filter((s) => !s.terminalId.includes('_ASR_AI_') && !s.name.startsWith('ASR_AI'));
              const isMaxReached = openManualSessions.length >= 2 || openHostSessions.length >= 4;

              return (
                <div
                  key={server.host}
                  className="server-card"
                  onClick={() => onOpenDetailsModal(server)}
                >
                  <div className="card-header" style={{ marginBottom: '8px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', overflow: 'hidden' }}>
                      <Server size={18} color="#3b82f6" />
                      <span className="host-name" style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                        {server.host}
                      </span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '11px', color: '#94a3b8', fontWeight: 600 }}>
                        {server.serverType || server.category}
                      </span>
                      {isConnected ? (
                        <span title="Connected" style={{ display: 'flex', alignItems: 'center' }}>
                          <Wifi size={16} color="#22c55e" />
                        </span>
                      ) : (
                        <span title="Disconnected" style={{ display: 'flex', alignItems: 'center' }}>
                          <WifiOff size={16} color="#ef4444" />
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="card-body" style={{ marginTop: '8px' }}>
                    <div style={{ fontSize: '11px', fontWeight: 600, color: '#64748b', marginBottom: '4px' }}>
                      Terminals ({allHostSessions.length})
                    </div>

                    {recentSessions.length === 0 ? (
                      <div style={{ fontSize: '12px', color: '#64748b', fontStyle: 'italic', padding: '4px 0' }}>
                        No terminals yet.
                      </div>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                        {recentSessions.map((sess) => (
                          <div
                            key={sess.terminalId}
                            style={{
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              background: 'rgba(255,255,255,0.04)',
                              border: '1px solid #334155',
                              borderRadius: '6px',
                              padding: '5px 8px',
                              fontSize: '11px',
                            }}
                          >
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', overflow: 'hidden' }}>
                              <span
                                style={{
                                  width: '8px',
                                  height: '8px',
                                  borderRadius: '50%',
                                  background:
                                    sess.state === 'RUNNING'
                                      ? '#eab308'
                                      : sess.state === 'IDLE'
                                      ? '#22c55e'
                                      : sess.state === 'CLOSING'
                                      ? '#f97316'
                                      : '#ef4444',
                                }}
                              />
                              <span style={{ color: '#f8fafc', fontWeight: 600, textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                                {sess.elevated ? 'Admin: ' : ''}{sess.name} ({sess.platform})
                              </span>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                              {sess.state === 'CLOSED' && (
                                <span style={{ color: '#ef4444', fontSize: '10px', fontWeight: 600, paddingRight: '2px' }}>CLOSED</span>
                              )}
                              {sess.state === 'CLOSING' && (
                                <span style={{ color: '#f97316', fontSize: '10px', fontWeight: 600, paddingRight: '2px' }}>CLOSING</span>
                              )}
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  onSelectTerminal(sess.terminalId);
                                }}
                                style={{
                                  background: sess.state === 'CLOSED' ? '#059669' : '#3b82f6',
                                  color: '#ffffff',
                                  border: 'none',
                                  borderRadius: '4px',
                                  padding: '2px 8px',
                                  fontSize: '10px',
                                  fontWeight: 600,
                                  cursor: 'pointer',
                                }}
                              >
                                Resume
                              </button>
                              {sess.state !== 'CLOSED' && (
                                <button
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    onCloseTerminal(sess.terminalId);
                                  }}
                                  style={{
                                    background: 'transparent',
                                    color: '#ef4444',
                                    border: 'none',
                                    padding: '2px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                  }}
                                  title="Close Terminal"
                                >
                                  <X size={12} />
                                </button>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className="card-footer" style={{ marginTop: '10px' }}>
                    <button
                      className="btn-open-terminal"
                      disabled={isMaxReached}
                      title={isMaxReached ? "Maximum limit of 4 terminals (2 AI + 2 Manual) reached for this host. Close an existing terminal to launch a new one." : undefined}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        fontSize: '11px',
                        padding: '4px 10px',
                        cursor: isMaxReached ? 'not-allowed' : 'pointer',
                        opacity: isMaxReached ? 0.5 : 1,
                      }}
                      onClick={(e) => {
                        e.stopPropagation();
                        if (!isMaxReached) {
                          onOpenStartModal(server);
                        }
                      }}
                    >
                      <Plus size={12} /> + New Terminal
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
};
