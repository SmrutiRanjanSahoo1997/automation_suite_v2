import React, { useState } from 'react';
import { ServerConfig, TerminalPlatform } from '../types/terminal.types';

interface Props {
  server: ServerConfig;
  nextSequence?: { PS: number; CMD: number };
  onStart: (server: ServerConfig, platform: TerminalPlatform) => void;
  onClose: () => void;
}

export const StartTerminalModal: React.FC<Props> = ({ server, nextSequence, onStart, onClose }) => {
  const [platform, setPlatform] = useState<TerminalPlatform>('PS');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onStart(server, platform);
  };

  const previewSequence = nextSequence ? nextSequence[platform] : 1;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" style={{ width: '380px' }} onClick={(e) => e.stopPropagation()}>
        <div className="rename-title">Start Terminal - {server.host}</div>
        <div style={{ fontSize: '12px', color: '#94a3b8', marginBottom: '14px' }}>
          {server.displayName} ({server.serverType || server.category} | {server.env || server.environment})
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '14px', background: 'rgba(255, 255, 255, 0.04)', padding: '10px 12px', borderRadius: '6px', border: '1px solid #334155' }}>
            <div style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '4px' }}>
              Auto-Generated Terminal Name
            </div>
            <div style={{ fontSize: '14px', fontWeight: 700, color: '#38bdf8', fontFamily: 'monospace' }}>
              TERMINAL_{platform}_{previewSequence}
            </div>
          </div>

          <div style={{ marginBottom: '18px' }}>
            <label style={{ display: 'block', fontSize: '13px', marginBottom: '8px', color: '#f8fafc', fontWeight: 600 }}>
              Shell Platform
            </label>
            <div style={{ display: 'flex', gap: '16px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', fontSize: '13px', color: '#f8fafc' }}>
                <input
                  type="radio"
                  name="platform"
                  value="PS"
                  checked={platform === 'PS'}
                  onChange={() => setPlatform('PS')}
                />
                PowerShell (PS)
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', fontSize: '13px', color: '#f8fafc' }}>
                <input
                  type="radio"
                  name="platform"
                  value="CMD"
                  checked={platform === 'CMD'}
                  onChange={() => setPlatform('CMD')}
                />
                Command Prompt (CMD)
              </label>
            </div>
          </div>

          <div className="dialog-actions">
            <button
              type="button"
              onClick={onClose}
              style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '7px 14px', borderRadius: '4px', cursor: 'pointer' }}
            >
              Cancel
            </button>
            <button
              type="submit"
              style={{ background: '#3b82f6', color: '#ffffff', border: 'none', padding: '7px 14px', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 }}
            >
              Start Terminal
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
