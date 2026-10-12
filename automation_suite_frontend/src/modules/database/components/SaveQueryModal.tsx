import React, { useState } from 'react';
import { X, Save, AlertCircle } from 'lucide-react';

interface SaveQueryModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (fileName: string) => Promise<void>;
}

export const SaveQueryModal: React.FC<SaveQueryModalProps> = ({
  isOpen,
  onClose,
  onSave,
}) => {
  const [fileName, setFileName] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fileName.trim()) {
      setError('Please provide a file name.');
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      await onSave(fileName.trim());
      setFileName('');
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to save query file.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="db-modal-overlay" onClick={onClose}>
      <div className="db-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="db-modal-header">
          <span>Save Query to Runbooks</span>
          <button className="db-tab-close-btn" onClick={onClose}>
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="db-modal-body">
            <label style={{ display: 'block', marginBottom: 6, fontSize: 13, color: '#a1a1aa' }}>
              Query File Name (.sql will be appended automatically):
            </label>
            <input
              type="text"
              className="db-input"
              value={fileName}
              onChange={(e) => setFileName(e.target.value)}
              placeholder="e.g. Daily_User_Summary"
              autoFocus
            />

            {error && (
              <div style={{ color: '#ef4444', fontSize: 12, marginTop: 8, display: 'flex', alignItems: 'center', gap: 6 }}>
                <AlertCircle size={14} />
                <span>{error}</span>
              </div>
            )}
          </div>

          <div className="db-modal-footer">
            <button
              type="button"
              className="db-btn db-btn-secondary"
              onClick={onClose}
              disabled={isSaving}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="db-btn db-btn-execute"
              disabled={isSaving || !fileName.trim()}
            >
              <Save size={14} />
              <span>{isSaving ? 'Saving...' : 'Save Query'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
