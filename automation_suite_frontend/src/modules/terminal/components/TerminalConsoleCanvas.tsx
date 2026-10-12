import React, { useState, useRef, useEffect } from 'react';
import { TerminalSession } from '../types/terminal.types';
import { Copy, Check } from 'lucide-react';

interface Props {
  session: TerminalSession;
  onExecute: (terminalId: string, host: string, command: string) => void;
  onStop: (terminalId: string, host: string) => void;
}

function stripAnsi(str: string): string {
  if (!str) return '';
  return str
    .replace(/\u001b\][^\u0007\u001b]*(?:\u0007|\u001b\\)/g, '')
    .replace(/\u0007/g, '')
    .replace(/\u001b\[[0-9;?]*[ -/]*[@-~]/g, '')
    .replace(/\u001b[@-_]/g, '');
}

function isBarePrompt(line: string): boolean {
  const clean = stripAnsi(line).trim();
  return /^(?:PS\s+)?[A-Za-z]:\\[^<>\"|?*]*>$/.test(clean);
}

export const TerminalConsoleCanvas: React.FC<Props> = ({ session, onExecute, onStop }) => {
  const [commandInput, setCommandInput] = useState('');
  const PAGE_SIZE = 25;
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE);

  const [selectedText, setSelectedText] = useState('');
  const [copied, setCopied] = useState(false);

  const containerRef = useRef<HTMLDivElement>(null);
  const outputEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const prevScrollHeightRef = useRef<number>(0);
  const isAutoScrollBottomRef = useRef<boolean>(true);

  // Track text selection globally so Copy button appears reliably and doesn't lose text
  useEffect(() => {
    const handleSelectionChange = () => {
      const selection = window.getSelection();
      if (selection) {
        const text = selection.toString().trim();
        if (text.length > 0) {
          setSelectedText(text);
        } else if (!copied) {
          setSelectedText('');
        }
      }
    };

    document.addEventListener('selectionchange', handleSelectionChange);
    return () => {
      document.removeEventListener('selectionchange', handleSelectionChange);
    };
  }, [copied]);

  const fallbackCopy = (text: string) => {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    try {
      document.execCommand('copy');
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.warn('Fallback copy failed:', err);
    } finally {
      document.body.removeChild(textarea);
    }
  };

  const handleCopySelection = (e: React.MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
    const selText = selectedText || (window.getSelection() ? window.getSelection()!.toString().trim() : '');
    if (selText) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(selText).then(() => {
          setCopied(true);
          setTimeout(() => setCopied(false), 2000);
        }).catch(() => fallbackCopy(selText));
      } else {
        fallbackCopy(selText);
      }
    }
  };

  const handleContainerClick = (e: React.MouseEvent) => {
    const selection = window.getSelection();
    const activeSel = selection ? selection.toString().trim() : '';
    if (activeSel.length > 0 || selectedText.length > 0) {
      // User has selected text — preserve selection and don't steal focus
      return;
    }
    inputRef.current?.focus();
  };

  // Keep visible count aligned with total output lines when live lines arrive
  useEffect(() => {
    if (isAutoScrollBottomRef.current) {
      setVisibleCount((prev) => Math.max(prev, Math.min(session.outputLines.length, prev + 5)));
      outputEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [session.outputLines]);

  const handleScroll = () => {
    if (!containerRef.current) return;
    const { scrollTop, scrollHeight, clientHeight } = containerRef.current;
    
    // Check if user is scrolled near bottom
    const isAtBottom = scrollHeight - (scrollTop + clientHeight) < 40;
    isAutoScrollBottomRef.current = isAtBottom;

    // When scrolled near top, load next page of older output lines
    if (scrollTop < 30 && visibleCount < session.outputLines.length) {
      prevScrollHeightRef.current = scrollHeight;
      setVisibleCount((prev) => Math.min(session.outputLines.length, prev + PAGE_SIZE));
    }
  };

  // Adjust scroll position after expanding older lines at the top
  useEffect(() => {
    if (containerRef.current && prevScrollHeightRef.current > 0) {
      const newScrollHeight = containerRef.current.scrollHeight;
      const diff = newScrollHeight - prevScrollHeightRef.current;
      containerRef.current.scrollTop += diff;
      prevScrollHeightRef.current = 0;
    }
  }, [visibleCount]);

  const isAiReserved = Boolean(session.terminalId && session.terminalId.startsWith('ASR_AI_'));

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (isAiReserved) {
      e.preventDefault();
      return;
    }
    if (e.key === 'Enter') {
      e.preventDefault();
      onExecute(session.terminalId, session.host, commandInput);
      setCommandInput('');
      isAutoScrollBottomRef.current = true;
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (session.history.length > 0) {
        const nextIdx = Math.max(0, session.historyIndex - 1);
        session.historyIndex = nextIdx;
        setCommandInput(session.history[nextIdx] || '');
      }
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      if (session.history.length > 0) {
        const nextIdx = Math.min(session.history.length, session.historyIndex + 1);
        session.historyIndex = nextIdx;
        setCommandInput(session.history[nextIdx] || '');
      }
    } else if (e.ctrlKey && e.key === 'c') {
      e.preventDefault();
      onStop(session.terminalId, session.host);
    }
  };

  const currentPath = session.currentPath && session.currentPath.trim() ? session.currentPath : 'C:\\';
  const promptPrefix = session.platform === 'PS' ? `PS ${currentPath}>` : `${currentPath}>`;

  const flattenLines = (lines: string[]): string[] => {
    const res: string[] = [];
    lines.forEach((l) => {
      if (l === undefined || l === null) return;
      if (typeof l === 'string' && (l.includes('\n') || l.includes('\r'))) {
        const parts = l.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n');
        if (parts.length > 1 && parts[parts.length - 1] === '') {
          parts.pop();
        }
        res.push(...parts);
      } else {
        res.push(String(l));
      }
    });
    return res;
  };

  const allLines = flattenLines(session.outputLines);
  const totalLines = allLines.length;
  const startIndex = Math.max(0, totalLines - visibleCount);
  const hiddenCount = startIndex;
  const displayedLines = allLines.slice(startIndex);

  const loadMoreOlder = () => {
    if (containerRef.current) {
      prevScrollHeightRef.current = containerRef.current.scrollHeight;
    }
    setVisibleCount((prev) => Math.min(totalLines, prev + PAGE_SIZE));
  };

  return (
    <div className="console-container" onClick={handleContainerClick} style={{ position: 'relative' }}>
      {selectedText && (
        <button
          onMouseDown={(e) => e.preventDefault()}
          onClick={handleCopySelection}
          style={{
            position: 'absolute',
            top: '12px',
            right: '24px',
            zIndex: 100,
            background: copied ? '#166534' : '#3b82f6',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            padding: '6px 12px',
            fontSize: '12px',
            fontWeight: 600,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.4)',
          }}
        >
          {copied ? <Check size={14} color="#4ade80" /> : <Copy size={14} />}
          {copied ? 'Copied!' : 'Copy Selected'}
        </button>
      )}

      <div className="console-output" ref={containerRef} onScroll={handleScroll}>
        {hiddenCount > 0 && (
          <div
            onClick={loadMoreOlder}
            style={{
              padding: '4px 8px',
              margin: '4px 0 8px 0',
              background: 'rgba(59, 130, 246, 0.12)',
              border: '1px solid rgba(59, 130, 246, 0.3)',
              borderRadius: '4px',
              color: '#93c5fd',
              fontSize: '11px',
              textAlign: 'center',
              cursor: 'pointer',
              userSelect: 'none',
            }}
          >
            ↑ {hiddenCount} older lines hidden — scroll up or click to load more
          </div>
        )}

        {displayedLines.map((line, idx) => {
          const cleanLine = stripAnsi(line);
          if (isBarePrompt(cleanLine)) {
            return null; // drop bare prompt lines from history list so only active prompt renders
          }
          return (
            <div key={startIndex + idx} style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all', minHeight: '18px', userSelect: 'text' }}>
              {cleanLine}
            </div>
          );
        })}

        {/* Real Terminal Prompt Line rendered inline at the bottom of output stream */}
        <div className="console-prompt-line" style={{ display: 'flex', alignItems: 'center', gap: '6px', width: '100%', marginTop: '6px' }}>
          <span className="prompt-path" style={{ whiteSpace: 'nowrap', flexShrink: 0, fontWeight: 'bold', color: isAiReserved ? '#eab308' : '#38bdf8' }}>
            {promptPrefix}
          </span>
          <input
            ref={inputRef}
            type="text"
            className="console-input"
            style={{
              flex: 1,
              minWidth: 0,
              border: 'none',
              outline: 'none',
              background: 'transparent',
              color: isAiReserved ? '#a1a1aa' : '#f8fafc',
              fontFamily: 'monospace',
              fontSize: '13px',
              fontStyle: isAiReserved ? 'italic' : 'normal',
            }}
            value={isAiReserved ? '🔒 Reserved for AI Executions (Read-Only)' : commandInput}
            onChange={(e) => !isAiReserved && setCommandInput(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={isAiReserved || session.state === 'CLOSING' || session.state === 'CLOSED'}
            autoFocus={!isAiReserved}
          />
        </div>

        <div ref={outputEndRef} />
      </div>
    </div>
  );
};
