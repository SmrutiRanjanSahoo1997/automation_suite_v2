import React, { useState, useRef, useEffect, useMemo } from 'react';
import { Copy, FileSpreadsheet, FileText, Filter, ArrowUp, ArrowDown, Check, X } from 'lucide-react';

interface QueryResultsGridProps {
  columns: string[];
  data: Record<string, any>[];
  durationMs: number;
  totalRows: number;
}

interface SelectionRange {
  startRow: number;
  startCol: number;
  endRow: number;
  endCol: number;
}

export const QueryResultsGrid: React.FC<QueryResultsGridProps> = ({
  columns,
  data,
  durationMs,
  totalRows,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);

  // Sorting
  const [sortCol, setSortCol] = useState<string | null>(null);
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');

  // Filtering
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [openFilterCol, setOpenFilterCol] = useState<string | null>(null);
  const [filterSearch, setFilterSearch] = useState('');

  // Selection
  const [selection, setSelection] = useState<SelectionRange | null>(null);
  const [isMouseDown, setIsMouseDown] = useState(false);

  // Context Menu
  const [contextMenu, setContextMenu] = useState<{ x: number; y: number } | null>(null);

  // Process data with sorting and filtering
  const processedData = useMemo(() => {
    let list = [...data];

    // Apply column filters
    Object.keys(filters).forEach((col) => {
      const val = filters[col]?.toLowerCase();
      if (val) {
        list = list.filter((row) => {
          const cell = row[col];
          return cell !== null && cell !== undefined && String(cell).toLowerCase().includes(val);
        });
      }
    });

    // Apply sorting
    if (sortCol) {
      list.sort((a, b) => {
        const valA = a[sortCol];
        const valB = b[sortCol];
        if (valA === valB) return 0;
        if (valA === null || valA === undefined) return 1;
        if (valB === null || valB === undefined) return -1;

        const numA = Number(valA);
        const numB = Number(valB);
        if (!isNaN(numA) && !isNaN(numB)) {
          return sortDir === 'asc' ? numA - numB : numB - numA;
        }

        const cmp = String(valA).localeCompare(String(valB));
        return sortDir === 'asc' ? cmp : -cmp;
      });
    }

    return list;
  }, [data, filters, sortCol, sortDir]);

  // Normalized selection rectangle
  const normalizedSelection = useMemo(() => {
    if (!selection) return null;
    return {
      minRow: Math.min(selection.startRow, selection.endRow),
      maxRow: Math.max(selection.startRow, selection.endRow),
      minCol: Math.min(selection.startCol, selection.endCol),
      maxCol: Math.max(selection.startCol, selection.endCol),
    };
  }, [selection]);

  // Check if cell is within current selection
  const isCellSelected = (r: number, c: number) => {
    if (!normalizedSelection) return false;
    return (
      r >= normalizedSelection.minRow &&
      r <= normalizedSelection.maxRow &&
      c >= normalizedSelection.minCol &&
      c <= normalizedSelection.maxCol
    );
  };

  // Keyboard navigation for Excel grid (Ctrl+A, Shift+Arrows)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Don't intercept if user is typing in filter search or other input
      if ((e.target as HTMLElement)?.tagName === 'INPUT' || (e.target as HTMLElement)?.tagName === 'TEXTAREA') {
        return;
      }

      // Ctrl+A -> Select All
      if (e.ctrlKey && e.key.toLowerCase() === 'a') {
        e.preventDefault();
        selectAll();
        return;
      }

      // Shift + Arrows
      if (e.shiftKey && selection && (e.key.startsWith('Arrow'))) {
        e.preventDefault();
        let { startRow, startCol, endRow, endCol } = selection;
        const maxR = processedData.length - 1;
        const maxC = columns.length - 1;

        if (e.key === 'ArrowUp' && endRow > 0) endRow--;
        if (e.key === 'ArrowDown' && endRow < maxR) endRow++;
        if (e.key === 'ArrowLeft' && endCol > 0) endCol--;
        if (e.key === 'ArrowRight' && endCol < maxC) endCol++;

        setSelection({ startRow, startCol, endRow, endCol });
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [selection, processedData.length, columns.length]);

  // Select All via A1 corner cell or Ctrl+A
  const selectAll = () => {
    if (processedData.length === 0 || columns.length === 0) return;
    setSelection({
      startRow: 0,
      startCol: 0,
      endRow: processedData.length - 1,
      endCol: columns.length - 1,
    });
  };

  // Select entire row
  const selectRow = (rIndex: number) => {
    if (columns.length === 0) return;
    setSelection({
      startRow: rIndex,
      startCol: 0,
      endRow: rIndex,
      endCol: columns.length - 1,
    });
  };

  // Select entire column
  const selectCol = (cIndex: number) => {
    if (processedData.length === 0) return;
    setSelection({
      startRow: 0,
      startCol: cIndex,
      endRow: processedData.length - 1,
      endCol: cIndex,
    });
  };

  // Handle mouse drag selection
  const handleCellMouseDown = (r: number, c: number, e: React.MouseEvent) => {
    if (e.button !== 0) return; // Only left click starts drag
    setIsMouseDown(true);
    setContextMenu(null);

    if (e.shiftKey && selection) {
      setSelection({
        ...selection,
        endRow: r,
        endCol: c,
      });
    } else {
      setSelection({
        startRow: r,
        startCol: c,
        endRow: r,
        endCol: c,
      });
    }
  };

  const handleCellMouseEnter = (r: number, c: number) => {
    if (isMouseDown && selection) {
      setSelection((prev) => (prev ? { ...prev, endRow: r, endCol: c } : null));
    }
  };

  const handleMouseUp = () => {
    setIsMouseDown(false);
  };

  // Right-click context menu
  const handleContextMenu = (e: React.MouseEvent, r?: number, c?: number) => {
    e.preventDefault();
    if (r !== undefined && c !== undefined && !isCellSelected(r, c)) {
      // Right-clicked outside current selection -> select that single cell
      setSelection({
        startRow: r,
        startCol: c,
        endRow: r,
        endCol: c,
      });
    }
    setContextMenu({ x: e.clientX, y: e.clientY });
  };

  // Copy operations
  const copySelection = (includeHeaders: boolean) => {
    if (!normalizedSelection || columns.length === 0) return;
    const { minRow, maxRow, minCol, maxCol } = normalizedSelection;

    const lines: string[] = [];
    if (includeHeaders) {
      const headerLine = columns.slice(minCol, maxCol + 1).join('\t');
      lines.push(headerLine);
    }

    for (let r = minRow; r <= maxRow; r++) {
      const rowObj = processedData[r];
      if (!rowObj) continue;
      const rowCells = columns.slice(minCol, maxCol + 1).map((col) => {
        const val = rowObj[col];
        return val !== null && val !== undefined ? String(val) : '';
      });
      lines.push(rowCells.join('\t'));
    }

    const tsv = lines.join('\n');
    navigator.clipboard.writeText(tsv).then(() => {
      setContextMenu(null);
    });
  };

  // Export CSV
  const exportCsv = () => {
    const lines: string[] = [];
    lines.push(columns.map((c) => `"${c.replace(/"/g, '""')}"`).join(','));

    processedData.forEach((row) => {
      const cells = columns.map((col) => {
        const val = row[col];
        const str = val !== null && val !== undefined ? String(val) : '';
        return `"${str.replace(/"/g, '""')}"`;
      });
      lines.push(cells.join(','));
    });

    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `query_results_${Date.now()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    setContextMenu(null);
  };

  // Export TXT
  const exportTxt = () => {
    const lines: string[] = [];
    lines.push(columns.join('\t'));
    processedData.forEach((row) => {
      const cells = columns.map((col) => {
        const val = row[col];
        return val !== null && val !== undefined ? String(val) : '';
      });
      lines.push(cells.join('\t'));
    });

    const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `query_results_${Date.now()}.txt`;
    a.click();
    URL.revokeObjectURL(url);
    setContextMenu(null);
  };

  // Toggle sorting
  const handleSort = (col: string) => {
    if (sortCol === col) {
      if (sortDir === 'asc') setSortDir('desc');
      else {
        setSortCol(null);
        setSortDir('asc');
      }
    } else {
      setSortCol(col);
      setSortDir('asc');
    }
  };

  // Selected cell count
  const selectedCellCount = useMemo(() => {
    if (!normalizedSelection) return 0;
    const rows = normalizedSelection.maxRow - normalizedSelection.minRow + 1;
    const cols = normalizedSelection.maxCol - normalizedSelection.minCol + 1;
    return rows * cols;
  }, [normalizedSelection]);

  if (columns.length === 0) {
    return (
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#71717a', fontSize: 13 }}>
        No query results yet. Execute a query above to view results.
      </div>
    );
  }

  return (
    <div
      className="db-grid-container"
      ref={containerRef}
      onMouseUp={handleMouseUp}
      onClick={() => {
        setContextMenu(null);
        setOpenFilterCol(null);
      }}
    >
      <table className="db-grid-table">
        <thead>
          <tr>
            {/* A1 Cell: Select All */}
            <th
              className="db-grid-a1-corner"
              onClick={selectAll}
              title="Click A1 to Select All cells (Ctrl+A)"
            >
              A1
            </th>
            {columns.map((col, cIndex) => (
              <th
                key={col}
                onClick={() => selectCol(cIndex)}
                title={`Click to select column. Click header icon to filter/sort.`}
              >
                <div className="db-grid-header-cell">
                  <span
                    onClick={(e) => {
                      e.stopPropagation();
                      handleSort(col);
                    }}
                    style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
                  >
                    {col}
                    {sortCol === col && (sortDir === 'asc' ? <ArrowUp size={11} /> : <ArrowDown size={11} />)}
                  </span>
                  <button
                    className="db-tab-close-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      setOpenFilterCol(openFilterCol === col ? null : col);
                      setFilterSearch(filters[col] || '');
                    }}
                    title="Excel Column Filter"
                  >
                    <Filter size={11} color={filters[col] ? '#3b82f6' : '#71717a'} />
                  </button>
                </div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {processedData.map((row, rIndex) => (
            <tr key={rIndex}>
              {/* Row Number */}
              <td
                className="db-grid-rownum"
                onClick={() => selectRow(rIndex)}
                title={`Row ${rIndex + 1}. Click to select entire row.`}
              >
                {rIndex + 1}
              </td>
              {columns.map((col, cIndex) => {
                const isSelected = isCellSelected(rIndex, cIndex);
                const cellVal = row[col];
                const displayVal = cellVal !== null && cellVal !== undefined ? String(cellVal) : '(null)';

                return (
                  <td
                    key={col}
                    className={`db-grid-cell ${isSelected ? 'selected' : ''}`}
                    onMouseDown={(e) => handleCellMouseDown(rIndex, cIndex, e)}
                    onMouseEnter={() => handleCellMouseEnter(rIndex, cIndex)}
                    onContextMenu={(e) => handleContextMenu(e, rIndex, cIndex)}
                    style={{ color: cellVal === null ? '#71717a' : 'inherit' }}
                  >
                    {displayVal}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>

      {/* Excel Column Filter Popover */}
      {openFilterCol && (
        <div
          className="db-intellisense-popup"
          style={{ position: 'fixed', top: 120, left: 300, width: 220 }}
          onClick={(e) => e.stopPropagation()}
        >
          <div style={{ padding: '6px 8px', fontSize: 12, fontWeight: 600, color: '#f4f4f5' }}>
            Filter: {openFilterCol}
          </div>
          <div style={{ padding: '0 8px 6px' }}>
            <input
              type="text"
              className="db-input"
              style={{ fontSize: 12, padding: '4px 8px' }}
              placeholder="Search / filter value..."
              value={filterSearch}
              onChange={(e) => setFilterSearch(e.target.value)}
              autoFocus
            />
          </div>
          <div style={{ display: 'flex', gap: 6, padding: '0 8px 6px', justifyContent: 'flex-end' }}>
            <button
              className="db-btn db-btn-secondary"
              style={{ fontSize: 11, padding: '3px 8px' }}
              onClick={() => {
                const next = { ...filters };
                delete next[openFilterCol];
                setFilters(next);
                setOpenFilterCol(null);
              }}
            >
              Clear
            </button>
            <button
              className="db-btn db-btn-execute"
              style={{ fontSize: 11, padding: '3px 8px' }}
              onClick={() => {
                setFilters({ ...filters, [openFilterCol]: filterSearch });
                setOpenFilterCol(null);
              }}
            >
              Apply
            </button>
          </div>
        </div>
      )}

      {/* Context Menu on Right Click */}
      {contextMenu && (
        <div
          className="db-context-menu"
          style={{ top: contextMenu.y, left: contextMenu.x }}
          onClick={(e) => e.stopPropagation()}
        >
          <div className="db-context-menu-item" onClick={() => copySelection(false)}>
            <Copy size={13} />
            <span>Copy</span>
          </div>
          <div className="db-context-menu-item" onClick={() => copySelection(true)}>
            <Copy size={13} />
            <span>Copy with Headers</span>
          </div>
          <div className="db-context-divider" />
          <div className="db-context-menu-item" onClick={exportCsv}>
            <FileSpreadsheet size={13} />
            <span>Export as CSV</span>
          </div>
          <div className="db-context-menu-item" onClick={exportCsv}>
            <FileSpreadsheet size={13} />
            <span>Export as Excel</span>
          </div>
          <div className="db-context-menu-item" onClick={exportTxt}>
            <FileText size={13} />
            <span>Export as TXT</span>
          </div>
        </div>
      )}

      {/* Status Bar */}
      <div className="db-grid-statusbar">
        <div>
          <span>Total Rows: {totalRows}</span>
          {processedData.length !== totalRows && (
            <span style={{ color: '#60a5fa', marginLeft: 8 }}>
              (Filtered: {processedData.length})
            </span>
          )}
        </div>
        <div>
          {selectedCellCount > 0 && (
            <span style={{ marginRight: 16 }}>
              Selected Cells: {selectedCellCount}
            </span>
          )}
          {durationMs > 0 && <span>Duration: {durationMs} ms</span>}
        </div>
      </div>
    </div>
  );
};
