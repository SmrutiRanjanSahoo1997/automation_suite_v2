import React from 'react';
import { TerminalSession } from '../types/terminal.types';
import { X, Maximize2, Minimize2, Minus, ExternalLink } from 'lucide-react';

interface Props {
  sessions: TerminalSession[];
  activeTerminalId: string | null;
  onSelectTab: (terminalId: string) => void;
  onCloseTab: (terminalId: string) => void;
  onToggleMinimize: () => void;
  onToggleFullscreen: () => void;
  onCloseWindow: () => void;
  onPopOut?: (terminalId: string) => void;
  isFullscreen: boolean;
}

export const TerminalTabHeader: React.FC<Props> = ({
  sessions,
  activeTerminalId,
  onSelectTab,
  onCloseTab,
  onToggleMinimize,
  onToggleFullscreen,
  onCloseWindow,
  onPopOut,
  isFullscreen,
}) => {
  const getStateColor = (state: string) => {
    switch (state) {
      case 'RUNNING': return '#eab308';
      case 'IDLE': return '#22c55e';
      case 'CLOSING': return '#f97316';
      case 'CLOSED': return '#ef4444';
      default: return '#94a3b8';
    }
  };

  return (
    <div className="window-header">
      <div className="tabs-container">
        {sessions.map((sess) => {
          const isActive = sess.terminalId === activeTerminalId;
          const stateColor = getStateColor(sess.state);

          return (
            <div
              key={sess.terminalId}
              className={`tab-item ${isActive ? 'active' : ''}`}
              onClick={() => onSelectTab(sess.terminalId)}
              title={`ID: ${sess.terminalId}`}
            >
              <span
                style={{
                  width: '7px',
                  height: '7px',
                  borderRadius: '50%',
                  background: stateColor,
                  display: 'inline-block',
                }}
              />
              <span>
                {sess.elevated ? 'Administrator: ' : ''}{sess.name} ({sess.platform}) [{sess.state}]
              </span>
              <span
                className="tab-close"
                onClick={(e) => {
                  e.stopPropagation();
                  onCloseTab(sess.terminalId);
                }}
              >
                <X size={12} />
              </span>
            </div>
          );
        })}
      </div>

      <div className="window-controls">
        {activeTerminalId && onPopOut && (
          <button
            className="window-btn"
            onClick={() => onPopOut(activeTerminalId)}
            title="Open in new window"
          >
            <ExternalLink size={14} />
          </button>
        )}
        <button className="window-btn" onClick={onToggleMinimize} title="Minimize">
          <Minus size={14} />
        </button>
        <button className="window-btn" onClick={onToggleFullscreen} title={isFullscreen ? 'Restore' : 'Maximize'}>
          <Maximize2 size={14} />
        </button>
        <button className="window-btn close" onClick={onCloseWindow} title="Close Window">
          <X size={14} />
        </button>
      </div>
    </div>
  );
};
