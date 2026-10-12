import React from 'react';
import { Play, Square, Save, FolderOpen, Loader2 } from 'lucide-react';
import { ExecutionStep, MssqlEnvironment } from '../types';

interface DatabaseActionToolbarProps {
  environment: MssqlEnvironment;
  isExecuting: boolean;
  currentStep: ExecutionStep;
  durationMs: number;
  totalRows: number;
  onChangeEnvironment: (env: MssqlEnvironment) => void;
  onExecute: () => void;
  onCancel: () => void;
  onOpenSaveModal: () => void;
  onOpenSavedQueries: () => void;
}

export const DatabaseActionToolbar: React.FC<DatabaseActionToolbarProps> = ({
  environment,
  isExecuting,
  currentStep,
  durationMs,
  totalRows,
  onChangeEnvironment,
  onExecute,
  onCancel,
  onOpenSaveModal,
  onOpenSavedQueries,
}) => {
  return (
    <div className="db-action-toolbar">
      <div className="db-toolbar-left">
        <label style={{ fontSize: 13, fontWeight: 600, color: '#a1a1aa' }}>Env:</label>
        <select
          className="db-env-select"
          value={environment}
          onChange={(e) => onChangeEnvironment(e.target.value as MssqlEnvironment)}
          disabled={isExecuting}
        >
          <option value="prod">Prod (PAG0035L)</option>
          <option value="test">Test (TAG0026L)</option>
        </select>

        <button
          className="db-btn db-btn-execute"
          onClick={onExecute}
          disabled={isExecuting}
          title="Execute Query (F5 or Ctrl+Enter)"
        >
          {isExecuting ? <Loader2 size={14} className="animate-spin" /> : <Play size={14} fill="currentColor" />}
          <span>Execute</span>
        </button>

        <button
          className="db-btn db-btn-cancel"
          onClick={onCancel}
          disabled={!isExecuting}
          title="Cancel Execution"
        >
          <Square size={14} fill="currentColor" />
          <span>Cancel</span>
        </button>

        <button
          className="db-btn db-btn-secondary"
          onClick={onOpenSaveModal}
          title="Save Query to Runbooks"
        >
          <Save size={14} />
          <span>Save Query</span>
        </button>

        <button
          className="db-btn db-btn-secondary"
          onClick={onOpenSavedQueries}
          title="Open Runbooks Saved Queries"
        >
          <FolderOpen size={14} />
          <span>Saved Queries</span>
        </button>
      </div>

      <div className="db-toolbar-right">
        {currentStep !== 'idle' && (
          <span className={`db-step-badge db-step-${currentStep}`}>
            {currentStep === 'querySent' ? 'Query Sent' : currentStep}
          </span>
        )}
        {durationMs > 0 && (
          <span style={{ fontSize: 12, color: '#a1a1aa' }}>
            {durationMs} ms
          </span>
        )}
        {totalRows > 0 && (
          <span style={{ fontSize: 12, color: '#a1a1aa' }}>
            ({totalRows} rows)
          </span>
        )}
      </div>
    </div>
  );
};
