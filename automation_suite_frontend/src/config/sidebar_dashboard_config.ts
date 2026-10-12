/**
 * Central Dashboard Configuration for Sidebar Navigation.
 * Defines ordering, names, icons, sequence, and hide/unhide flags.
 */

export interface DashboardConfigItem {
  id: string;
  name: string;
  iconName: 'Bot' | 'Terminal' | 'Database' | 'FileText' | 'Activity' | 'Network' | 'Shield' | 'Cloud' | 'Zap' | 'Settings';
  sequence: number;
  hide: boolean;
}

export const SIDEBAR_DASHBOARD_CONFIG: DashboardConfigItem[] = [
  { id: 'asr_ai', name: 'ASR_AI Dashboard', iconName: 'Bot', sequence: 1, hide: false },
  { id: 'terminal', name: 'Terminal Dashboard', iconName: 'Terminal', sequence: 2, hide: false },
  { id: 'db_dashboard', name: 'Database Dashboard', iconName: 'Database', sequence: 3, hide: false },
  { id: 'logs_dashboard', name: 'Log Analytics', iconName: 'FileText', sequence: 4, hide: true },
  { id: 'metrics_dashboard', name: 'Metrics & Monitoring', iconName: 'Activity', sequence: 5, hide: true },
  { id: 'network_dashboard', name: 'Network Inspector', iconName: 'Network', sequence: 6, hide: true },
  { id: 'security_dashboard', name: 'Security Audit', iconName: 'Shield', sequence: 7, hide: true },
  { id: 'cloud_dashboard', name: 'Cloud Resources', iconName: 'Cloud', sequence: 8, hide: true },
  { id: 'automation_dashboard', name: 'Automation Workflows', iconName: 'Zap', sequence: 9, hide: true },
  { id: 'settings_dashboard', name: 'System Settings', iconName: 'Settings', sequence: 10, hide: true },
];
