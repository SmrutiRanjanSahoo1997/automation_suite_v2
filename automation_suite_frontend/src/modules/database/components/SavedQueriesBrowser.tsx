import React, { useState } from 'react';
import { X, FolderOpen, Trash2, FileText, Download } from 'lucide-react';
import { SavedQuery } from '../types';

interface SavedQueriesBrowserProps {
  isOpen: boolean;
  queries: SavedQuery[];
  onClose: () => void;
  onLoadQuery: (fileName: string) => void;
  onDeleteQuery: (fileName: string) => void;
}

export const SavedQueriesBrowser: React.FC<SavedQueriesBrowserProps> = ({
  isOpen,
  queries,
  onClose,
  onLoadQuery,
  onDeleteQuery,
}) => {
  const [search, setSearch] = useState('');

  if (!isOpen) return null;

  const filtered = queries.filter((q) =>
    q.name.toLowerCase().includes(search.toLowerCase()) ||
    q.fileName.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="db-modal-overlay" onClick={onClose}>
      <div className="db-modal-content" style={{ width: 560 }} onClick={(e) => e.stopPropagation()}>
        <div className="db-modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <FolderOpen size={16} />
            <span>Runbooks Saved Queries</span>
          </div>
          <button className="db-tab-close-btn" onClick={onClose}>
            <X size={16} />
          </button>
        </div>

        <div style={{ padding: '12px 16px 4px 16px' }}>
          <input
            type="text"
            className="db-input"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search saved queries..."
            autoFocus
          />
        </div>

        <div className="db-modal-body" style={{ maxHeight: 320, overflowY: 'auto' }}>
          {filtered.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '24px 0', color: '#71717a', fontSize: 13 }}>
              No saved queries found.
            </div>
          ) : (
            filtered.map((item) => (
              <div
                key={item.fileName}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '8px 12px',
                  background: '#121215',
                  border: '1px solid #27272a',
                  borderRadius: 6,
                  marginBottom: 8,
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <FileText size={16} color="#60a5fa" />
                  <div>
                    <div style={{ fontWeight: 500, fontSize: 13, color: '#f4f4f5' }}>
                      {item.name}
                    </div>
                    <div style={{ fontSize: 11, color: '#71717a', marginTop: 2 }}>
                      {item.fileName} &bull; {new Date(item.lastModified).toLocaleDateString()}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <button
                    className="db-btn db-btn-execute"
                    style={{ fontSize: 12, padding: '4px 10px' }}
                    onClick={() => {
                      onLoadQuery(item.fileName);
                      onClose();
                    }}
                    title="Load into Editor"
                  >
                    Load
                  </button>
                  <button
                    className="db-btn db-btn-secondary"
                    style={{ padding: '4px 8px' }}
                    onClick={() => onDeleteQuery(item.fileName)}
                    title="Delete Query"
                  >
                    <Trash2 size={13} color="#ef4444" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        <div className="db-modal-footer">
          <button className="db-btn db-btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
