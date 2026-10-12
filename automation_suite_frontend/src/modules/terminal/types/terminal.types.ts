export type ServerCategory = 'INSPIRE_AUTOMATION' | 'IPS_POOL' | 'INSPIRE_SCALER' | 'INTERACTIVE';
export type Environment = 'PROD' | 'T01' | 'T02' | 'T03' | 'T04' | 'DEV';
export type TerminalPlatform = 'PS' | 'CMD';
export type TerminalState = 'IDLE' | 'RUNNING' | 'CLOSING' | 'CLOSED';

export interface ServerConfig {
  host: string;
  displayName: string;
  category: ServerCategory;
  serverType?: ServerCategory;
  environment: Environment;
  env?: Environment;
  port: number;
}

export interface AgentConnectionStatus {
  host: string;
  connected: boolean;
  lastConnectedAt: number | null;
  reconnectAttempts: number;
}

export interface TerminalSession {
  terminalId: string; // <userId>_<uuid>
  name: string;
  host: string;
  platform: TerminalPlatform;
  elevated?: boolean;
  state: TerminalState;
  currentPath: string;
  lastCommand: string | null;
  lastResult: string | null;
  lastError: string | null;
  outputLines: string[];
  history: string[];
  historyIndex: number;
  lastActivityAt?: number;
  isAiSession?: boolean;
}

export interface TerminalFrame {
  'event-type': string;
  terminalid: string;
  terminalId?: string;
  chunk?: string;
  count?: number;
  end?: boolean;
  errorCode?: string;
  state?: TerminalState;
  currentPath?: string;
  lastCommand?: string;
  pid?: number;
  platform?: TerminalPlatform;
  elevated?: boolean;
  lastInputTimestamp?: number;
  lastOutputTimestamp?: number;
  terminals?: TerminalFrame[];
}
