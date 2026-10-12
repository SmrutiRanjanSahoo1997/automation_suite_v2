import React, { useRef, useEffect, useState } from 'react';
import { Bot, Sparkles, Terminal, AlertCircle, Copy, Check } from 'lucide-react';
import { AiChatMessageRecord } from '../repository/aiMessageRepo';
import { AiReviewBanner } from './AiReviewBanner';
import { MarkdownRenderer } from './MarkdownRenderer';

interface ChatThreadProps {
  messages: AiChatMessageRecord[];
  isLoading: boolean;
  statusText: string;
  onApproveCommand: (msg: AiChatMessageRecord) => void;
  onApproveHost: (msg: AiChatMessageRecord) => void;
  onDeclineHost?: (msg: AiChatMessageRecord) => void;
  onDeclineCommand?: (msg: AiChatMessageRecord) => void;
  onApproveDatabase?: (msg: AiChatMessageRecord) => void;
  onDeclineDatabase?: (msg: AiChatMessageRecord) => void;
}

const ExecutableCommandCard: React.FC<{
  command: string;
  platform?: 'PS' | 'CMD';
  criticalityLevel?: number;
}> = ({ command, platform = 'PS', criticalityLevel = 0 }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    if (navigator?.clipboard?.writeText) {
      navigator.clipboard.writeText(command);
    }
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const getCriticalityBadge = (level: number) => {
    switch (level) {
      case 1:
        return {
          label: 'Warning',
          color: '#f97316',
          bg: 'rgba(249, 115, 22, 0.15)',
          border: '#f97316',
        };
      case 2:
        return {
          label: 'Critical',
          color: '#ef4444',
          bg: 'rgba(239, 68, 68, 0.15)',
          border: '#ef4444',
        };
      case 3:
        return {
          label: 'Most Critical',
          color: '#f87171',
          bg: 'rgba(220, 38, 38, 0.25)',
          border: '#dc2626',
        };
      default:
        return {
          label: 'Normal',
          color: '#22c55e',
          bg: 'rgba(34, 197, 94, 0.15)',
          border: '#22c55e',
        };
    }
  };

  const badge = getCriticalityBadge(criticalityLevel);

  return (
    <div
      style={{
        background: '#09090b',
        border: '1px solid #27272a',
        borderRadius: '8px',
        padding: '12px',
        fontFamily: 'Consolas, monospace',
        fontSize: '13px',
        color: '#38bdf8',
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '8px',
          flexWrap: 'wrap',
          gap: '8px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '11px', color: '#71717a', textTransform: 'uppercase', fontWeight: 600 }}>
            Executable Command ({platform})
          </span>
          <span
            style={{
              fontSize: '11px',
              fontWeight: 700,
              padding: '2px 8px',
              borderRadius: '12px',
              background: badge.bg,
              color: badge.color,
              border: `1px solid ${badge.border}`,
            }}
          >
            {badge.label}
          </span>
        </div>
        <button
          onClick={handleCopy}
          title="Copy command to clipboard"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            background: copied ? 'rgba(34, 197, 94, 0.15)' : '#18181b',
            border: `1px solid ${copied ? '#22c55e' : '#3f3f46'}`,
            borderRadius: '4px',
            color: copied ? '#22c55e' : '#a1a1aa',
            fontSize: '11px',
            padding: '3px 8px',
            cursor: 'pointer',
            fontFamily: 'inherit',
          }}
        >
          {copied ? <Check style={{ width: '12px', height: '12px' }} /> : <Copy style={{ width: '12px', height: '12px' }} />}
          <span>{copied ? 'Copied!' : 'Copy'}</span>
        </button>
      </div>
      <div style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all', lineHeight: 1.4 }}>
        {command}
      </div>
    </div>
  );
};

export const ChatThread: React.FC<ChatThreadProps> = ({
  messages,
  isLoading,
  statusText,
  onApproveCommand,
  onApproveHost,
  onDeclineHost,
  onDeclineCommand,
  onApproveDatabase,
  onDeclineDatabase,
}) => {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading, statusText]);

  return (
    <div style={{ flex: 1, overflowY: 'auto', padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {messages.length === 0 ? (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#71717a', gap: '12px' }}>
          <Bot style={{ width: '48px', height: '48px', color: '#eab308' }} />
          <h3 style={{ margin: 0, color: '#ffffff', fontSize: '18px' }}>What would you like ASR_AI to automate today?</h3>
          <p style={{ margin: 0, fontSize: '13px', maxWidth: '480px', textAlign: 'center' }}>
            Type your query below to diagnose system issues, list files, inspect logs, or execute automation tasks on target servers.
          </p>
        </div>
      ) : (
        messages.map((msg) => (
          <div key={msg.id} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {/* User Message */}
            {msg.sender === 'user' && (
              <div style={{ alignSelf: 'flex-end', background: '#eab308', color: '#000000', padding: '12px 16px', borderRadius: '16px 16px 2px 16px', fontSize: '14px', maxWidth: '80%', fontWeight: 600 }}>
                {msg.text}
              </div>
            )}

            {/* AI Response Card */}
            {msg.sender === 'ai' && (
              msg.status === 'error' ? (
                <div style={{ alignSelf: 'flex-start', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid #ef4444', color: '#ef4444', padding: '12px 16px', borderRadius: '12px', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '10px', maxWidth: '85%' }}>
                  <AlertCircle style={{ width: '20px', height: '20px', flexShrink: 0 }} />
                  <span style={{ wordBreak: 'break-word', whiteSpace: 'pre-wrap' }}>
                    {msg.text || msg.preResponse || 'An error occurred during AI processing.'}
                  </span>
                </div>
              ) : (
                <div style={{ alignSelf: 'flex-start', background: '#18181b', border: '1px solid #27272a', padding: '16px', borderRadius: '16px 16px 16px 2px', maxWidth: '85%', color: '#ffffff', fontSize: '14px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {/* Thought Process */}
                  {msg.preResponse && (
                    <div style={{ background: '#0a0a0a', borderLeft: '3px solid #eab308', padding: '8px 12px', borderRadius: '4px', fontSize: '12px', color: '#a1a1aa', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Sparkles style={{ width: '14px', height: '14px', color: '#eab308', flexShrink: 0 }} />
                      <span><strong>Thought Process:</strong> {msg.preResponse}</span>
                    </div>
                  )}

                  {/* Executable Command Block with Criticality Badge & Copy Button */}
                  {msg.command && (
                    <ExecutableCommandCard
                      command={msg.command}
                      platform={msg.platform}
                      criticalityLevel={msg.criticalityLevel}
                    />
                  )}

                  {/* Final Outcome or Text Message */}
                  {msg.finalOutcome && (
                    <div style={{ lineHeight: 1.5, color: '#ffffff' }}>
                      <MarkdownRenderer content={msg.finalOutcome} />
                    </div>
                  )}
                  {msg.text && !msg.finalOutcome && (
                    <div style={{ lineHeight: 1.5, color: '#ffffff' }}>
                      <MarkdownRenderer content={msg.text} />
                    </div>
                  )}

                  {/* Host Review Banner */}
                  {msg.reviewRequiredHost && (
                    <AiReviewBanner
                      type="host"
                      title={msg.reviewTitle}
                      description={msg.reviewDescription}
                      targetHosts={msg.targetHosts}
                      onAgree={() => onApproveHost(msg)}
                      onDecline={() => onDeclineHost?.(msg)}
                    />
                  )}

                  {/* Command Review Banner */}
                  {msg.reviewNeeded && (
                    <AiReviewBanner
                      type="command"
                      title={msg.reviewTitle}
                      description={msg.reviewDescription}
                      command={msg.command}
                      platform={msg.platform}
                      criticalityLevel={msg.criticalityLevel}
                      onApproveExecute={() => onApproveCommand(msg)}
                      onDecline={() => onDeclineCommand ? onDeclineCommand(msg) : {}}
                    />
                  )}

                  {/* Executable SQL Query Block */}
                  {msg.sql && !msg.reviewNeededDatabase && (
                    <div
                      style={{
                        background: '#09090b',
                        border: '1px solid #27272a',
                        borderRadius: '8px',
                        padding: '12px',
                        fontFamily: 'Consolas, monospace',
                        fontSize: '13px',
                        color: '#60a5fa',
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
                        <span style={{ fontSize: '11px', color: '#71717a', textTransform: 'uppercase', fontWeight: 600 }}>
                          Executed SQL ({msg.environment?.toUpperCase() || 'PROD'})
                        </span>
                      </div>
                      <div style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all', lineHeight: 1.4 }}>
                        {msg.sql}
                      </div>
                    </div>
                  )}

                  {/* Database Query Review Banner */}
                  {msg.reviewNeededDatabase && (
                    <AiReviewBanner
                      type="database"
                      title={msg.reviewTitle}
                      description={msg.reviewDescription}
                      environment={msg.environment}
                      sql={msg.sql}
                      onApproveExecute={() => onApproveDatabase?.(msg)}
                      onDecline={() => onDeclineDatabase?.(msg)}
                    />
                  )}
                </div>
              )
            )}

            {/* System Output Message */}
            {msg.sender === 'system' && (
              <div style={{ alignSelf: 'center', background: '#0a0a0a', border: '1px solid #27272a', color: '#38bdf8', padding: '8px 16px', borderRadius: '20px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Terminal style={{ width: '14px', height: '14px' }} />
                <span>{msg.text}</span>
              </div>
            )}
          </div>
        ))
      )}

      {/* Dynamic Interactive Loading Indicator */}
      {isLoading && (
        <div style={{ alignSelf: 'flex-start', background: '#18181b', border: '1px solid #27272a', padding: '12px 16px', borderRadius: '16px', color: '#a1a1aa', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Sparkles style={{ width: '16px', height: '16px', color: '#eab308', animation: 'spin 2s linear infinite' }} />
          <span>{statusText || 'ASR_AI is processing query...'}</span>
        </div>
      )}

      <div ref={bottomRef} />
    </div>
  );
};
