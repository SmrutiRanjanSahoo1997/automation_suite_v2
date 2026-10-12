import React, { useState } from 'react';
import { Send, Square, Sparkles } from 'lucide-react';

interface InputBoxProps {
  onSendQuery: (text: string) => void;
  onStopExecution: () => void;
  isLoading: boolean;
  disabled?: boolean;
  disabledPlaceholder?: string;
}

export const InputBox: React.FC<InputBoxProps> = ({
  onSendQuery,
  onStopExecution,
  isLoading,
  disabled = false,
  disabledPlaceholder,
}) => {
  const [queryText, setQueryText] = useState('');

  const isLocked = disabled || isLoading;

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!queryText.trim() || isLocked) return;
    onSendQuery(queryText.trim());
    setQueryText('');
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <div style={{ padding: '16px 24px', background: '#121212', borderTop: '1px solid #27272a' }}>
      <form onSubmit={handleSubmit} style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
        <textarea
          rows={2}
          value={queryText}
          onChange={(e) => !disabled && setQueryText(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={disabled}
          placeholder={
            disabled
              ? (disabledPlaceholder || '🔒 Action required: Please make a decision above before continuing...')
              : "Ask ASR_AI in layman language (e.g. 'Check high CPU usage on IA PROD' or 'Find latest resume')..."
          }
          style={{
            width: '100%',
            background: disabled ? '#0f0f12' : '#18181b',
            border: disabled ? '1px dashed #6366f1' : '1px solid #27272a',
            borderRadius: '12px',
            padding: '12px 50px 12px 16px',
            color: disabled ? '#a1a1aa' : '#ffffff',
            fontSize: '14px',
            lineHeight: 1.4,
            outline: 'none',
            resize: 'none',
            fontFamily: 'inherit',
            boxSizing: 'border-box',
            cursor: disabled ? 'not-allowed' : 'text',
            opacity: disabled ? 0.7 : 1,
          }}
        />

        <div style={{ position: 'absolute', right: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          {isLoading ? (
            <button
              type="button"
              onClick={onStopExecution}
              title="Stop Execution"
              style={{
                background: '#ef4444',
                color: '#ffffff',
                border: 'none',
                borderRadius: '8px',
                padding: '8px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Square style={{ width: '16px', height: '16px' }} />
            </button>
          ) : (
            <button
              type="submit"
              disabled={disabled || !queryText.trim()}
              title="Send Message"
              style={{
                background: (!disabled && queryText.trim()) ? '#eab308' : '#27272a',
                color: (!disabled && queryText.trim()) ? '#000000' : '#71717a',
                border: 'none',
                borderRadius: '8px',
                padding: '8px',
                cursor: (!disabled && queryText.trim()) ? 'pointer' : 'not-allowed',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Send style={{ width: '16px', height: '16px' }} />
            </button>
          )}
        </div>
      </form>
      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '11px', color: '#71717a', marginTop: '8px', justifyContent: 'center' }}>
        <Sparkles style={{ width: '12px', height: '12px', color: '#eab308' }} />
        <span>ASR_AI Assistant converts natural language queries into automated terminal diagnostics & remediations.</span>
      </div>
    </div>
  );
};
