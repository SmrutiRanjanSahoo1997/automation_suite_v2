import React, { useState, useEffect, useCallback, useRef } from 'react';
import { WindowTabBar } from './components/WindowTabBar';
import { DatabaseActionToolbar } from './components/DatabaseActionToolbar';
import { ObjectExplorerTree } from './components/ObjectExplorerTree';
import { SqlQueryEditor } from './components/SqlQueryEditor';
import { QueryResultsGrid } from './components/QueryResultsGrid';
import { QueryMessagesPane } from './components/QueryMessagesPane';
import { SaveQueryModal } from './components/SaveQueryModal';
import { SavedQueriesBrowser } from './components/SavedQueriesBrowser';
import { databaseMssqlService } from './services/databaseMssqlService';
import { databaseSavedQueryService } from './services/databaseSavedQueryService';
import { MssqlTab, MssqlEnvironment, SchemaObject, SavedQuery } from './types';
import { getActiveAuthUserInfo } from '../auth/repository/authRepo';
import '../../styles/databaseDashboard.css';

const DEFAULT_QUERY = `-- MSSQL Studio Default Query\nSELECT TOP (100) name, database_id, create_date FROM sys.databases;\n`;

export const DatabaseDashboardPage: React.FC = () => {
  const [userId, setUserId] = useState<string>('user');

  // Tabs state
  const [tabs, setTabs] = useState<MssqlTab[]>([
    {
      id: 'tab-1',
      title: 'Window 1',
      environment: 'prod',
      query: DEFAULT_QUERY,
      results: [],
      columns: [],
      totalRows: 0,
      status: 'idle',
      statusMessage: '',
      durationMs: 0,
      currentStep: 'idle',
      taskId: null,
      activeFilter: {},
      activeSort: null,
    },
  ]);
  const [activeTabId, setActiveTabId] = useState<string>('tab-1');

  // Active Bottom Tab: 'results' | 'messages'
  const [bottomTab, setBottomTab] = useState<'results' | 'messages'>('results');

  // Object Explorer Schema State
  const [schema, setSchema] = useState<SchemaObject>({
    databases: [],
    tablesByDb: {},
    expandedDbs: {},
    loadingDbs: {},
  });

  // Runbooks Modals
  const [isSaveModalOpen, setIsSaveModalOpen] = useState(false);
  const [isSavedQueriesOpen, setIsSavedQueriesOpen] = useState(false);
  const [savedQueriesList, setSavedQueriesList] = useState<SavedQuery[]>([]);

  // Resizable Editor / Results Split Height
  const [editorHeight, setEditorHeight] = useState<number>(240);
  const [isDraggingResizer, setIsDraggingResizer] = useState<boolean>(false);

  const handleResizerMouseDown = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsDraggingResizer(true);

    const startY = e.clientY;
    const startHeight = editorHeight;

    const onMouseMove = (moveEvent: MouseEvent) => {
      const deltaY = moveEvent.clientY - startY;
      const minHeight = 80;
      const maxHeight = window.innerHeight - 200;
      const newHeight = Math.max(minHeight, Math.min(maxHeight, startHeight + deltaY));
      setEditorHeight(newHeight);
    };

    const onMouseUp = () => {
      setIsDraggingResizer(false);
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('mouseup', onMouseUp);
    };

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
  };

  const handleResetHeight = () => {
    setEditorHeight(240);
  };

  // Fetch Auth User
  useEffect(() => {
    getActiveAuthUserInfo().then((info) => {
      if (info && info.userId) {
        setUserId(info.userId);
      }
    });
  }, []);

  const activeTab = tabs.find((t) => t.id === activeTabId) || tabs[0];

  // Helper to update active tab
  const updateTab = useCallback((tabId: string, updater: (prev: MssqlTab) => MssqlTab) => {
    setTabs((prevTabs) =>
      prevTabs.map((t) => (t.id === tabId ? updater(t) : t))
    );
  }, []);

  // Subscribe to Global WebSocket events for MSSQL
  useEffect(() => {
    const unsubscribe = databaseMssqlService.subscribe((frame) => {
      if (!frame || typeof frame !== 'object') return;
      const type = frame.type;
      const taskId = frame.taskId;

      // Handle Schema events
      if (type === 'databases' && Array.isArray(frame.databases)) {
        setSchema((prev) => ({
          ...prev,
          databases: frame.databases,
        }));
        return;
      }

      if (type === 'tables' && frame.database && Array.isArray(frame.tables)) {
        setSchema((prev) => ({
          ...prev,
          tablesByDb: {
            ...prev.tablesByDb,
            [frame.database]: frame.tables,
          },
          loadingDbs: {
            ...prev.loadingDbs,
            [frame.database]: false,
          },
        }));
        return;
      }

      // Handle Query Execution stream events
      if (taskId) {
        setTabs((prevTabs) =>
          prevTabs.map((t) => {
            if (t.taskId !== taskId) return t;

            if (type === 'connecting') {
              return { ...t, currentStep: 'connecting', status: 'executing' };
            }
            if (type === 'connected') {
              return { ...t, currentStep: 'connected' };
            }
            if (type === 'querySent') {
              return { ...t, currentStep: 'querySent' };
            }
            if (type === 'closed') {
              return { ...t, currentStep: 'closed' };
            }

            if (type === 'output') {
              const incomingRows: any[] = frame.data || [];
              const isEnd = Boolean(frame.end);
              const isSuccess = frame.success !== false && !frame.error;

              let nextCols = t.columns;
              if (incomingRows.length > 0 && nextCols.length === 0) {
                nextCols = Object.keys(incomingRows[0]);
              }

              const newResults = [...t.results, ...incomingRows];
              const totalCount = frame.rowCount !== undefined ? frame.rowCount : newResults.length;

              return {
                ...t,
                results: newResults,
                columns: nextCols,
                totalRows: totalCount,
                currentStep: 'output',
                durationMs: frame.durationMs !== undefined ? frame.durationMs : t.durationMs,
                status: isEnd ? (isSuccess ? 'success' : 'error') : 'executing',
                statusMessage: frame.error ? frame.error : isEnd ? 'Query executed successfully.' : t.statusMessage,
              };
            }

            return t;
          })
        );
      }
    });

    return () => unsubscribe();
  }, []);

  // Fetch initial databases on mount for active environment
  useEffect(() => {
    refreshDatabases();
  }, [activeTab.environment]);

  const refreshDatabases = () => {
    const taskId = 'db-refresh-' + Date.now();
    databaseMssqlService.getDatabases(taskId, userId, activeTab.environment);
  };

  const handleToggleDatabase = (db: string) => {
    setSchema((prev) => {
      const isCurrentlyExpanded = !!prev.expandedDbs[db];
      const nextExpanded = { ...prev.expandedDbs, [db]: !isCurrentlyExpanded };

      // If expanding and tables not yet loaded, fetch tables
      if (!isCurrentlyExpanded && !prev.tablesByDb[db]) {
        const taskId = 'tables-' + db + '-' + Date.now();
        databaseMssqlService.getTables(taskId, userId, activeTab.environment, db);
        return {
          ...prev,
          expandedDbs: nextExpanded,
          loadingDbs: { ...prev.loadingDbs, [db]: true },
        };
      }

      return {
        ...prev,
        expandedDbs: nextExpanded,
      };
    });
  };

  const handleSelectTop1000 = (db: string, schemaName: string, table: string) => {
    const top1000Query = `SELECT TOP (1000) * FROM [${db}].[${schemaName}].[${table}];\n`;
    updateTab(activeTabId, (prev) => ({
      ...prev,
      query: top1000Query,
    }));
    // Automatically trigger execute
    setTimeout(() => {
      executeActiveQuery(top1000Query);
    }, 50);
  };

  // Add / Close Tab
  const handleAddTab = () => {
    const nextNum = tabs.length + 1;
    const newTab: MssqlTab = {
      id: 'tab-' + Date.now(),
      title: `Window ${nextNum}`,
      environment: activeTab.environment,
      query: `-- Window ${nextNum}\nSELECT TOP (100) * FROM sys.databases;\n`,
      results: [],
      columns: [],
      totalRows: 0,
      status: 'idle',
      statusMessage: '',
      durationMs: 0,
      currentStep: 'idle',
      taskId: null,
      activeFilter: {},
      activeSort: null,
    };
    setTabs([...tabs, newTab]);
    setActiveTabId(newTab.id);
  };

  const handleCloseTab = (tabId: string) => {
    if (tabs.length <= 1) return;
    const filtered = tabs.filter((t) => t.id !== tabId);
    setTabs(filtered);
    if (activeTabId === tabId) {
      setActiveTabId(filtered[0].id);
    }
  };

  // Execute Active Query
  const executeActiveQuery = (queryOverride?: string) => {
    const sqlToRun = queryOverride || activeTab.query;
    if (!sqlToRun.trim()) return;

    const taskId = 'task-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7);

    updateTab(activeTabId, (prev) => ({
      ...prev,
      taskId,
      status: 'executing',
      statusMessage: '',
      results: [],
      columns: [],
      totalRows: 0,
      durationMs: 0,
      currentStep: 'connecting',
    }));

    setBottomTab('results');
    databaseMssqlService.executeQuery(taskId, userId, activeTab.environment, sqlToRun);
  };

  const handleCancelQuery = () => {
    updateTab(activeTabId, (prev) => ({
      ...prev,
      status: 'idle',
      currentStep: 'idle',
      statusMessage: 'Execution cancelled by user.',
    }));
  };

  // Saved Queries
  const handleOpenSavedQueries = async () => {
    try {
      const list = await databaseSavedQueryService.listSavedQueries();
      setSavedQueriesList(list);
      setIsSavedQueriesOpen(true);
    } catch (err) {
      console.error('Failed to load saved queries', err);
      setIsSavedQueriesOpen(true);
    }
  };

  const handleSaveQuery = async (fileName: string) => {
    await databaseSavedQueryService.saveQuery(fileName, activeTab.query);
  };

  const handleLoadSavedQuery = async (fileName: string) => {
    try {
      const content = await databaseSavedQueryService.getSavedQuery(fileName);
      updateTab(activeTabId, (prev) => ({
        ...prev,
        query: content,
        title: fileName.replace(/\.sql$/i, ''),
      }));
    } catch (err) {
      console.error('Failed to read query content', err);
    }
  };

  const handleDeleteSavedQuery = async (fileName: string) => {
    try {
      await databaseSavedQueryService.deleteQuery(fileName);
      const updated = await databaseSavedQueryService.listSavedQueries();
      setSavedQueriesList(updated);
    } catch (err) {
      console.error('Failed to delete query', err);
    }
  };

  // Collect all known table names for IntelliSense
  const knownTables = Object.values(schema.tablesByDb)
    .flat()
    .map((t) => t.name);

  return (
    <div className="db-studio-container">
      {/* Top Tabs Bar */}
      <WindowTabBar
        tabs={tabs}
        activeTabId={activeTabId}
        onSelectTab={setActiveTabId}
        onAddTab={handleAddTab}
        onCloseTab={handleCloseTab}
      />

      {/* Action Toolbar */}
      <DatabaseActionToolbar
        environment={activeTab.environment}
        isExecuting={activeTab.status === 'executing'}
        currentStep={activeTab.currentStep}
        durationMs={activeTab.durationMs}
        totalRows={activeTab.totalRows}
        onChangeEnvironment={(env) => updateTab(activeTabId, (t) => ({ ...t, environment: env }))}
        onExecute={() => executeActiveQuery()}
        onCancel={handleCancelQuery}
        onOpenSaveModal={() => setIsSaveModalOpen(true)}
        onOpenSavedQueries={handleOpenSavedQueries}
      />

      {/* Main Workspace Body */}
      <div className="db-workspace-body">
        {/* Main Content Pane: Editor + Results */}
        <div className="db-content-pane">
          {/* SQL Editor */}
          <SqlQueryEditor
            value={activeTab.query}
            knownDatabases={schema.databases}
            knownTables={knownTables}
            height={editorHeight}
            onChange={(val) => updateTab(activeTabId, (t) => ({ ...t, query: val }))}
            onExecute={() => executeActiveQuery()}
          />

          {/* Horizontal Resizer Bar */}
          <div
            className={`db-pane-resizer-horizontal ${isDraggingResizer ? 'active' : ''}`}
            onMouseDown={handleResizerMouseDown}
            onDoubleClick={handleResetHeight}
            title="Drag up or down to resize Query Editor and Results pane (Double click to reset)"
          >
            <div className="db-resizer-handle" />
          </div>

          {/* Results & Messages Container */}
          <div className="db-results-wrapper">
            <div className="db-results-tabbar">
              <button
                className={`db-results-tab ${bottomTab === 'results' ? 'active' : ''}`}
                onClick={() => setBottomTab('results')}
              >
                Results {activeTab.totalRows > 0 ? `(${activeTab.totalRows})` : ''}
              </button>
              <button
                className={`db-results-tab ${bottomTab === 'messages' ? 'active' : ''}`}
                onClick={() => setBottomTab('messages')}
              >
                Messages {activeTab.status === 'error' ? '(!)' : ''}
              </button>
            </div>

            {bottomTab === 'results' ? (
              <QueryResultsGrid
                columns={activeTab.columns}
                data={activeTab.results}
                durationMs={activeTab.durationMs}
                totalRows={activeTab.totalRows}
              />
            ) : (
              <QueryMessagesPane
                status={activeTab.status}
                statusMessage={activeTab.statusMessage}
                durationMs={activeTab.durationMs}
                totalRows={activeTab.totalRows}
              />
            )}
          </div>
        </div>

        {/* Right Object Explorer */}
        <ObjectExplorerTree
          environment={activeTab.environment}
          schema={schema}
          onRefreshDatabases={refreshDatabases}
          onToggleDatabase={handleToggleDatabase}
          onSelectTop1000={handleSelectTop1000}
        />
      </div>

      {/* Runbooks Modals */}
      <SaveQueryModal
        isOpen={isSaveModalOpen}
        onClose={() => setIsSaveModalOpen(false)}
        onSave={handleSaveQuery}
      />

      <SavedQueriesBrowser
        isOpen={isSavedQueriesOpen}
        queries={savedQueriesList}
        onClose={() => setIsSavedQueriesOpen(false)}
        onLoadQuery={handleLoadSavedQuery}
        onDeleteQuery={handleDeleteSavedQuery}
      />
    </div>
  );
};

export default DatabaseDashboardPage;
