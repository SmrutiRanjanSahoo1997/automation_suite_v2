export type MssqlEnvironment = 'prod' | 'test';

export type ExecutionStep = 'idle' | 'connecting' | 'connected' | 'querySent' | 'output' | 'closed';

export interface MssqlTab {
  id: string;
  title: string;
  environment: MssqlEnvironment;
  query: string;
  results: Record<string, any>[];
  columns: string[];
  totalRows: number;
  status: 'idle' | 'executing' | 'success' | 'error';
  statusMessage: string;
  durationMs: number;
  currentStep: ExecutionStep;
  taskId: string | null;
  activeFilter: Record<string, string>;
  activeSort: { col: string; dir: 'asc' | 'desc' } | null;
}

export interface SchemaTable {
  schema: string;
  name: string;
}

export interface SchemaObject {
  databases: string[];
  tablesByDb: Record<string, SchemaTable[]>;
  expandedDbs: Record<string, boolean>;
  loadingDbs: Record<string, boolean>;
}

export interface SavedQuery {
  fileName: string;
  name: string;
  lastModified: number;
  size: number;
  content?: string;
}
