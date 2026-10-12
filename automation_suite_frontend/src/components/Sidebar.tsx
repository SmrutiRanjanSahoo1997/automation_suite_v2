import React from 'react';
import { Bot, Terminal, Database, FileText, Activity, Network, Shield, Cloud, Zap, Settings, User, KeyRound, LogOut } from 'lucide-react';
import { SIDEBAR_DASHBOARD_CONFIG, DashboardConfigItem } from '../config/sidebar_dashboard_config';
import { AuthUserInfo } from '../modules/auth/repository/authRepo';
import { logoutUser } from '../modules/auth/services/authService';

interface SidebarProps {
  activeDashboardId: string;
  onSelectDashboard: (id: string) => void;
  userInfo: AuthUserInfo | null;
  onOpenChangePassword: () => void;
  onLogout: () => void;
}

const ICON_MAP: Record<string, React.FC<{ style?: React.CSSProperties }>> = {
  Bot,
  Terminal,
  Database,
  FileText,
  Activity,
  Network,
  Shield,
  Cloud,
  Zap,
  Settings,
};

export const Sidebar: React.FC<SidebarProps> = ({
  activeDashboardId,
  onSelectDashboard,
  userInfo,
  onOpenChangePassword,
  onLogout,
}) => {
  const visibleDashboards = SIDEBAR_DASHBOARD_CONFIG.filter((d) => !d.hide).sort((a, b) => a.sequence - b.sequence);

  return (
    <aside
      style={{
        width: '260px',
        height: '100vh',
        background: '#121212',
        borderRight: '1px solid #27272a',
        display: 'flex',
        flexDirection: 'column',
        boxSizing: 'border-box',
        userSelect: 'none',
      }}
    >
      {/* Top Profile Section */}
      <div style={{ padding: '20px 16px', borderBottom: '1px solid #27272a' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
          <div
            style={{
              width: '40px',
              height: '40px',
              borderRadius: '50%',
              background: '#27272a',
              border: '2px solid #eab308',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#eab308',
              fontWeight: 700,
              fontSize: '16px',
            }}
          >
            {userInfo?.userName ? userInfo.userName.charAt(0).toUpperCase() : <User style={{ width: '20px', height: '20px' }} />}
          </div>
          <div style={{ overflow: 'hidden' }}>
            <div style={{ color: '#ffffff', fontWeight: 700, fontSize: '14px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {userInfo?.userName || 'Guest User'}
            </div>
            <div style={{ color: '#a1a1aa', fontSize: '11px', fontFamily: 'monospace' }}>
              ID: {userInfo?.userId || 'N/A'}
            </div>
            <div style={{ color: '#71717a', fontSize: '11px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {userInfo?.userMail || 'user@automation.com'}
            </div>
          </div>
        </div>

        {/* Profile Action Links */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: '8px' }}>
          <button
            onClick={onOpenChangePassword}
            style={{
              background: 'none',
              border: 'none',
              color: '#eab308',
              fontSize: '11px',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: 0,
            }}
          >
            <KeyRound style={{ width: '12px', height: '12px' }} />
            Change Password
          </button>

          <button
            onClick={onLogout}
            title="Logout"
            style={{
              background: 'none',
              border: 'none',
              color: '#ef4444',
              fontSize: '11px',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              padding: 0,
            }}
          >
            <LogOut style={{ width: '12px', height: '12px' }} />
            Logout
          </button>
        </div>
      </div>

      {/* Dashboard List */}
      <div style={{ flex: 1, padding: '16px 12px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '6px' }}>
        <div style={{ fontSize: '11px', textTransform: 'uppercase', color: '#71717a', fontWeight: 700, letterSpacing: '0.5px', marginBottom: '8px', paddingLeft: '8px' }}>
          Dashboards
        </div>

        {visibleDashboards.map((item) => {
          const IconComp = ICON_MAP[item.iconName] || Bot;
          const isActive = activeDashboardId === item.id;

          return (
            <button
              key={item.id}
              onClick={() => onSelectDashboard(item.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '10px 14px',
                borderRadius: '8px',
                background: isActive ? '#27272a' : 'transparent',
                borderLeft: isActive ? '3px solid #eab308' : '3px solid transparent',
                color: isActive ? '#ffffff' : '#a1a1aa',
                fontWeight: isActive ? 700 : 500,
                fontSize: '13px',
                cursor: 'pointer',
                textAlign: 'left',
                width: '100%',
                transition: 'all 0.15s ease',
              }}
            >
              <IconComp style={{ width: '18px', height: '18px', color: isActive ? '#eab308' : '#71717a' }} />
              <span>{item.name}</span>
            </button>
          );
        })}
      </div>

      {/* Footer Branding */}
      <div style={{ padding: '12px 16px', borderTop: '1px solid #27272a', fontSize: '11px', color: '#71717a', textAlign: 'center' }}>
        AutomationSuite v2.0
      </div>
    </aside>
  );
};
