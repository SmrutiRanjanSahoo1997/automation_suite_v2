import React, { useState } from 'react';
import { TerminalSession } from '../types/terminal.types';

interface Props {
  session: TerminalSession;
  onSave: (terminalId: string, newName: string) => void;
  onClose: () => void;
}

export const TerminalRenameModal: React.FC<Props> = ({ session, onSave, onClose }) => {
  const [name, setName] = useState(session.name);
  const isAsrAi = Boolean((session.name && session.name.toUpperCase().includes('ASR_AI')) || (session.terminalId && session.terminalId.toUpperCase().includes('ASR_AI')));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAsrAi && name.trim()) {
      onSave(session.terminalId, name.trim());
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="rename-title">Rename Terminal Tab</div>
        {isAsrAi && (
          <div style={{ color: '#ef4444', fontSize: '12px', marginBottom: '10px' }}>
            Protected terminal: ASR_AI session cannot be renamed.
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            className="rename-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter terminal name..."
            disabled={isAsrAi}
            autoFocus
          />
          <div className="dialog-actions">
            <button
              type="button"
              onClick={onClose}
              style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer' }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isAsrAi || !name.trim()}
              style={{ background: isAsrAi ? '#64748b' : '#3b82f6', color: '#ffffff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: isAsrAi ? 'not-allowed' : 'pointer' }}
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
