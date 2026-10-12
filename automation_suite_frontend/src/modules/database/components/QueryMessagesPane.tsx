import React from 'react';
import { AlertCircle, CheckCircle2, Clock } from 'lucide-react';

interface QueryMessagesPaneProps {
  status: 'idle' | 'executing' | 'success' | 'error';
  statusMessage: string;
  durationMs: number;
  totalRows: number;
}

export const QueryMessagesPane: React.FC<QueryMessagesPaneProps> = ({
  status,
  statusMessage,
  durationMs,
  totalRows,
}) => {
  if (status === 'idle') {
    return (
      <div className="db-messages-pane db-msg-info">
        Query output messages will appear here after execution.
      </div>
    );
  }

  return (
    <div className="db-messages-pane">
      {status === 'error' && (
        <div className="db-msg-error" style={{ display: 'flex', alignItems: 'flex-start', gap: 8, marginBottom: 8 }}>
          <AlertCircle size={16} style={{ marginTop: 2, flexShrink: 0 }} />
          <div>
            <strong>Query Execution Failed:</strong>
            <div style={{ marginTop: 4 }}>{statusMessage}</div>
          </div>
        </div>
      )}

      {status === 'success' && (
        <div className="db-msg-success" style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
          <CheckCircle2 size={16} />
          <span>({totalRows} rows affected)</span>
        </div>
      )}

      {status === 'executing' && (
        <div className="db-msg-info" style={{ marginBottom: 8 }}>
          Query is currently executing...
        </div>
      )}

      {durationMs > 0 && (
        <div className="db-msg-info" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Clock size={13} />
          <span>Execution time: {durationMs} ms</span>
        </div>
      )}
    </div>
  );
};
