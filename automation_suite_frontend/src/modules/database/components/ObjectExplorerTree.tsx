import React, { useState } from 'react';
import { Database, ChevronRight, ChevronDown, Table as TableIcon, RefreshCw, Server, Loader2 } from 'lucide-react';
import { MssqlEnvironment, SchemaObject, SchemaTable } from '../types';

interface ObjectExplorerTreeProps {
  environment: MssqlEnvironment;
  schema: SchemaObject;
  onRefreshDatabases: () => void;
  onToggleDatabase: (db: string) => void;
  onSelectTop1000: (db: string, schema: string, table: string) => void;
}

export const ObjectExplorerTree: React.FC<ObjectExplorerTreeProps> = ({
  environment,
  schema,
  onRefreshDatabases,
  onToggleDatabase,
  onSelectTop1000,
}) => {
  const [contextMenu, setContextMenu] = useState<{
    visible: boolean;
    x: number;
    y: number;
    db: string;
    schema: string;
    table: string;
  } | null>(null);

  const serverName = environment === 'prod' ? 'PAG0035L (Prod)' : 'TAG0026L (Test)';

  const handleTableContextMenu = (
    e: React.MouseEvent,
    db: string,
    tSchema: string,
    tName: string
  ) => {
    e.preventDefault();
    setContextMenu({
      visible: true,
      x: e.clientX,
      y: e.clientY,
      db,
      schema: tSchema,
      table: tName,
    });
  };

  const closeContextMenu = () => {
    if (contextMenu) setContextMenu(null);
  };

  return (
    <div className="db-explorer-pane" onClick={closeContextMenu}>
      <div className="db-explorer-header">
        <span>Object Explorer</span>
        <button
          className="db-tab-close-btn"
          onClick={onRefreshDatabases}
          title="Refresh Databases"
        >
          <RefreshCw size={13} />
        </button>
      </div>

      <div className="db-explorer-tree">
        {/* Server Root Node */}
        <div className="db-tree-node" style={{ fontWeight: 600, color: '#60a5fa' }}>
          <Server size={14} />
          <span>{serverName}</span>
        </div>

        {/* Databases List */}
        {schema.databases.length === 0 ? (
          <div style={{ padding: '8px 16px', fontSize: 12, color: '#71717a' }}>
            No databases loaded. Click refresh above to load.
          </div>
        ) : (
          schema.databases.map((db) => {
            const isExpanded = !!schema.expandedDbs[db];
            const isLoading = !!schema.loadingDbs[db];
            const tables = schema.tablesByDb[db] || [];

            return (
              <div key={db}>
                <div
                  className="db-tree-node db-tree-indent"
                  onClick={() => onToggleDatabase(db)}
                >
                  {isLoading ? (
                    <Loader2 size={12} className="animate-spin" />
                  ) : isExpanded ? (
                    <ChevronDown size={12} />
                  ) : (
                    <ChevronRight size={12} />
                  )}
                  <Database size={13} color="#a1a1aa" />
                  <span>{db}</span>
                </div>

                {/* Tables in Database */}
                {isExpanded && (
                  <div>
                    {tables.length === 0 && !isLoading ? (
                      <div
                        style={{
                          paddingLeft: 48,
                          fontSize: 12,
                          color: '#71717a',
                          paddingTop: 2,
                          paddingBottom: 2,
                        }}
                      >
                        (No base tables found)
                      </div>
                    ) : (
                      tables.map((t: SchemaTable) => (
                        <div
                          key={`${db}-${t.schema}-${t.name}`}
                          className="db-tree-node"
                          style={{ paddingLeft: 46 }}
                          onContextMenu={(e) =>
                            handleTableContextMenu(e, db, t.schema, t.name)
                          }
                          onDoubleClick={() => onSelectTop1000(db, t.schema, t.name)}
                          title="Double-click or right-click to Select Top 1000 Rows"
                        >
                          <TableIcon size={12} color="#93c5fd" />
                          <span style={{ fontSize: 12 }}>
                            {t.schema}.{t.name}
                          </span>
                        </div>
                      ))
                    )}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* Right-click Context Menu */}
      {contextMenu && (
        <div
          className="db-context-menu"
          style={{ top: contextMenu.y, left: contextMenu.x }}
          onClick={(e) => e.stopPropagation()}
        >
          <div
            className="db-context-menu-item"
            onClick={() => {
              onSelectTop1000(contextMenu.db, contextMenu.schema, contextMenu.table);
              setContextMenu(null);
            }}
          >
            <TableIcon size={13} />
            <span>Select Top 1000 Rows</span>
          </div>
        </div>
      )}
    </div>
  );
};
