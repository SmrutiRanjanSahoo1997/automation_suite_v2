import React, { useState } from 'react';
import { Cpu, Layout, Server, Filter } from 'lucide-react';
import { ServerConfig } from '../../terminal/types/terminal.types';
import { ServerFilterDialog } from '../../terminal/components/ServerFilterDialog';
import { DashboardFilterDialog, DASHBOARD_OPTIONS } from './DashboardFilterDialog';

interface TopControlBarProps {
  servers: ServerConfig[];
  selectedHosts: string[];
  onApplySelectedHosts: (hosts: string[]) => void;
  selectedDashboards: string[];
  onApplySelectedDashboards: (dashboards: string[]) => void;
}

export const TopControlBar: React.FC<TopControlBarProps> = ({
  servers,
  selectedHosts,
  onApplySelectedHosts,
  selectedDashboards,
  onApplySelectedDashboards,
}) => {
  const [isHostFilterModalOpen, setIsHostFilterModalOpen] = useState(false);
  const [isDashboardModalOpen, setIsDashboardModalOpen] = useState(false);

  const isHostAuto = selectedHosts.length === 0;
  const isDashboardAuto = selectedDashboards.length === 0 || selectedDashboards.includes('auto');

  const getDashboardDisplayLabel = () => {
    if (isDashboardAuto) return 'Auto';
    return selectedDashboards
      .map((id) => DASHBOARD_OPTIONS.find((o) => o.id === id)?.label || id)
      .join(' + ');
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 24px', background: '#121212', borderBottom: '1px solid #27272a', color: '#ffffff' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        {/* Model Control (Disabled Auto) */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
          <Cpu style={{ width: '16px', height: '16px', color: '#eab308' }} />
          <span style={{ color: '#a1a1aa', fontWeight: 600 }}>Model:</span>
          <select disabled style={{ background: '#18181b', color: '#ffffff', border: '1px solid #27272a', borderRadius: '6px', padding: '4px 8px', fontSize: '12px', cursor: 'not-allowed' }}>
            <option value="auto">Auto (Default)</option>
          </select>
        </div>

        {/* Dashboard Control Pill & Selection Filter Launcher */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
          <Layout style={{ width: '16px', height: '16px', color: '#eab308' }} />
          <span style={{ color: '#a1a1aa', fontWeight: 600 }}>Dashboard:</span>
          <button
            onClick={() => setIsDashboardModalOpen(true)}
            style={{
              background: isDashboardAuto ? '#18181b' : 'rgba(234, 179, 8, 0.15)',
              border: isDashboardAuto ? '1px solid #27272a' : '1px solid #eab308',
              color: isDashboardAuto ? '#a1a1aa' : '#eab308',
              borderRadius: '6px',
              padding: '4px 10px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <Filter style={{ width: '12px', height: '12px' }} />
            <span>{getDashboardDisplayLabel()}</span>
          </button>
        </div>

        {/* Target Host Control Pill & Server Filter Launcher */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
          <Server style={{ width: '16px', height: '16px', color: '#eab308' }} />
          <span style={{ color: '#a1a1aa', fontWeight: 600 }}>TargetHost:</span>
          <button
            onClick={() => setIsHostFilterModalOpen(true)}
            style={{
              background: isHostAuto ? '#18181b' : 'rgba(234, 179, 8, 0.15)',
              border: isHostAuto ? '1px solid #27272a' : '1px solid #eab308',
              color: isHostAuto ? '#a1a1aa' : '#eab308',
              borderRadius: '6px',
              padding: '4px 10px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <Filter style={{ width: '12px', height: '12px' }} />
            <span>{isHostAuto ? 'Auto' : selectedHosts.join(' + ')}</span>
          </button>
        </div>
      </div>

      {/* Dashboard Selection Modal Window */}
      <DashboardFilterDialog
        open={isDashboardModalOpen}
        onClose={() => setIsDashboardModalOpen(false)}
        selectedDashboards={selectedDashboards}
        onApply={(dashboards) => {
          onApplySelectedDashboards(dashboards);
          setIsDashboardModalOpen(false);
        }}
      />

      {/* Server Filter Modal Window */}
      <ServerFilterDialog
        open={isHostFilterModalOpen}
        onClose={() => setIsHostFilterModalOpen(false)}
        servers={servers}
        selectedHosts={selectedHosts}
        onApply={(hosts) => {
          onApplySelectedHosts(hosts);
          setIsHostFilterModalOpen(false);
        }}
      />
    </div>
  );
};
