import React from 'react';
import { Terminal, Sliders } from 'lucide-react';

interface Props {
  selectedCount: number;
  totalCount: number;
  onOpenServerFilterDialog: () => void;
}

export const HeaderServerFilter: React.FC<Props> = ({
  selectedCount,
  totalCount,
  onOpenServerFilterDialog,
}) => {
  return (
    <header className="header-filter" style={{ justifyContent: 'flex-start', gap: '24px' }}>
      <div className="header-title">
        <Terminal className="text-blue-500" size={24} color="#3b82f6" />
        <span>Terminal Dashboard</span>
      </div>

      <button
        onClick={onOpenServerFilterDialog}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          background: 'rgba(59, 130, 246, 0.15)',
          border: '1px solid #3b82f6',
          color: '#f8fafc',
          padding: '6px 14px',
          borderRadius: '6px',
          fontWeight: 600,
          cursor: 'pointer',
          fontSize: '13px',
        }}
      >
        <Sliders size={14} color="#38bdf8" />
        Server Filter Tree ({selectedCount}/{totalCount})
      </button>
    </header>
  );
};
