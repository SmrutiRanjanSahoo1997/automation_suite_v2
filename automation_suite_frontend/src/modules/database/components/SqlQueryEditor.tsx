import React, { useState, useRef, useEffect, useMemo } from 'react';
import { SqlIntelliSensePopup } from './SqlIntelliSensePopup';
import {
  tokenizeSql,
  checkKeywordTypo,
  SQL_KEYWORDS,
  SQL_FUNCTIONS,
} from '../services/sqlSyntaxValidatorService';

interface SqlQueryEditorProps {
  value: string;
  knownDatabases?: string[];
  knownTables?: string[];
  height?: number;
  onChange: (val: string) => void;
  onExecute: () => void;
}

export const SqlQueryEditor: React.FC<SqlQueryEditorProps> = ({
  value,
  knownDatabases = [],
  knownTables = [],
  height,
  onChange,
  onExecute,
}) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const highlightRef = useRef<HTMLPreElement>(null);
  const lineNumbersRef = useRef<HTMLDivElement>(null);

  const [suggestions, setSuggestions] = useState<{ text: string; type: string }[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [popupPos, setPopupPos] = useState({ top: 30, left: 60 });
  const [wordPrefix, setWordPrefix] = useState('');

  // Tokenize and syntax highlight with compile-time typo validation
  const tokens = useMemo(() => {
    return tokenizeSql(value, knownTables, knownDatabases);
  }, [value, knownTables, knownDatabases]);

  const lines = value.split('\n');
  const lineCount = Math.max(lines.length, 1);

  const handleScroll = () => {
    if (textareaRef.current) {
      const top = textareaRef.current.scrollTop;
      const left = textareaRef.current.scrollLeft;

      if (lineNumbersRef.current) {
        lineNumbersRef.current.scrollTop = top;
      }
      if (highlightRef.current) {
        highlightRef.current.scrollTop = top;
        highlightRef.current.scrollLeft = left;
      }
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    // F5 or Ctrl+Enter to execute query (never restricted!)
    if (e.key === 'F5' || (e.ctrlKey && e.key === 'Enter')) {
      e.preventDefault();
      setSuggestions([]);
      onExecute();
      return;
    }

    // IntelliSense popup navigation
    if (suggestions.length > 0) {
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev + 1) % suggestions.length);
        return;
      }
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev - 1 + suggestions.length) % suggestions.length);
        return;
      }
      if (e.key === 'Enter' || e.key === 'Tab') {
        e.preventDefault();
        applySuggestion(suggestions[selectedIndex]);
        return;
      }
      if (e.key === 'Escape') {
        e.preventDefault();
        setSuggestions([]);
        return;
      }
    }

    // Tab key indent
    if (e.key === 'Tab') {
      e.preventDefault();
      const ta = textareaRef.current;
      if (!ta) return;
      const start = ta.selectionStart;
      const end = ta.selectionEnd;
      const nextVal = value.substring(0, start) + '  ' + value.substring(end);
      onChange(nextVal);
      setTimeout(() => {
        ta.selectionStart = ta.selectionEnd = start + 2;
      }, 0);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const nextVal = e.target.value;
    onChange(nextVal);

    // Compute cursor prefix for autocomplete and typo intelligence
    const ta = e.target;
    const cursorPos = ta.selectionStart;
    const textBefore = nextVal.substring(0, cursorPos);
    const match = textBefore.match(/([a-zA-Z0-9_#]+)$/);

    if (match && match[1].length >= 2) {
      const rawWord = match[1];
      const prefix = rawWord.toUpperCase();
      setWordPrefix(rawWord);

      const items: { text: string; type: string }[] = [];

      // Check if current word is a typo of a keyword
      const typoFix = checkKeywordTypo(rawWord);
      if (typoFix) {
        items.push({ text: typoFix, type: `FIX TYPO (${typoFix})` });
      }

      // Keyword matches
      Array.from(SQL_KEYWORDS)
        .filter((k) => k.startsWith(prefix))
        .slice(0, 5)
        .forEach((k) => items.push({ text: k, type: 'KEYWORD' }));

      // Function matches
      Array.from(SQL_FUNCTIONS)
        .filter((f) => f.startsWith(prefix))
        .slice(0, 4)
        .forEach((f) => items.push({ text: f, type: 'FUNCTION' }));

      // Database matches
      knownDatabases
        .filter((d) => d.toUpperCase().startsWith(prefix))
        .slice(0, 3)
        .forEach((d) => items.push({ text: d, type: 'DATABASE' }));

      // Table matches
      knownTables
        .filter((t) => t.toUpperCase().startsWith(prefix))
        .slice(0, 3)
        .forEach((t) => items.push({ text: t, type: 'TABLE' }));

      setSuggestions(items);
      setSelectedIndex(0);

      // Estimate popup position based on cursor line
      const currLine = textBefore.split('\n').length;
      setPopupPos({
        top: Math.min(currLine * 21 + 10, 180),
        left: 60 + Math.min(rawWord.length * 8, 300),
      });
    } else {
      setSuggestions([]);
    }
  };

  const applySuggestion = (item: { text: string; type: string }) => {
    const ta = textareaRef.current;
    if (!ta) return;
    const cursorPos = ta.selectionStart;
    const beforeWord = value.substring(0, cursorPos - wordPrefix.length);
    const afterWord = value.substring(cursorPos);
    const nextVal = beforeWord + item.text + ' ' + afterWord;
    onChange(nextVal);
    setSuggestions([]);
    setTimeout(() => {
      ta.focus();
      const nextPos = beforeWord.length + item.text.length + 1;
      ta.selectionStart = ta.selectionEnd = nextPos;
    }, 0);
  };

  return (
    <div className="db-editor-wrapper" style={{ height: height ? `${height}px` : undefined }}>
      {/* Line Numbers Column */}
      <div className="db-editor-linenumbers" ref={lineNumbersRef}>
        {Array.from({ length: lineCount }).map((_, i) => (
          <div key={i} style={{ height: 21, lineHeight: '21px' }}>
            {i + 1}
          </div>
        ))}
      </div>

      {/* Editor Canvas Container */}
      <div className="db-editor-canvas-container">
        {/* Background Colorful Syntax Highlight Layer */}
        <pre className="db-editor-highlight-layer" ref={highlightRef} aria-hidden="true">
          {tokens.map((token, idx) => (
            <span
              key={idx}
              className={`db-syntax-${token.type}`}
              title={token.suggestion ? `Typo detected! Suggested fix: ${token.suggestion}` : undefined}
            >
              {token.text}
            </span>
          ))}
          {value.endsWith('\n') && '\n'}
        </pre>

        {/* Foreground Interactive Textarea Overlay */}
        <textarea
          ref={textareaRef}
          className="db-editor-textarea-overlay"
          value={value}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          onScroll={handleScroll}
          placeholder="-- Type your MSSQL query here (e.g. SELECT TOP 100 * FROM [table])&#10;-- Press F5 or Ctrl+Enter to execute"
          spellCheck={false}
        />
      </div>

      {/* Autocomplete / Typo Correction Popup */}
      {suggestions.length > 0 && (
        <SqlIntelliSensePopup
          suggestions={suggestions}
          selectedIndex={selectedIndex}
          position={popupPos}
          onSelect={applySuggestion}
        />
      )}
    </div>
  );
};
