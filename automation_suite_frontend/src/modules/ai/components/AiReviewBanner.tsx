import React from 'react';
import { AlertTriangle, CheckCircle, ShieldAlert, Terminal, Database, Play } from 'lucide-react';

interface AiHostReviewBannerProps {
  type: 'host';
  title?: string;
  description?: string;
  targetHosts?: string[];
  onAgree: () => void;
  onDecline: () => void;
}

interface AiCommandReviewBannerProps {
  type: 'command';
  title?: string;
  description?: string;
  command?: string;
  platform?: 'PS' | 'CMD';
  criticalityLevel?: number;
  onApproveExecute: () => void;
  onDecline: () => void;
}

interface AiDatabaseQueryReviewBannerProps {
  type: 'database';
  title?: string;
  description?: string;
  environment?: 'prod' | 'test';
  sql?: string;
  onApproveExecute: () => void;
  onDecline: () => void;
}

type AiReviewBannerProps =
  | AiHostReviewBannerProps
  | AiCommandReviewBannerProps
  | AiDatabaseQueryReviewBannerProps;

export const AiReviewBanner: React.FC<AiReviewBannerProps> = (props) => {
  if (props.type === 'host') {
    const { title, description, targetHosts, onAgree, onDecline } = props;
    const hasHosts = Boolean(targetHosts && targetHosts.length > 0);

    return (
      <div
        style={{
          background: '#1e1b4b',
          border: '1px solid #6366f1',
          borderRadius: '8px',
          padding: '16px',
          marginTop: '12px',
          marginBottom: '12px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
          <AlertTriangle style={{ width: '20px', height: '20px', color: '#818cf8' }} />
          <h4 style={{ margin: 0, fontSize: '15px', color: '#e0e7ff', fontWeight: 600 }}>
            {title || 'Host Resolution Confirmation Required'}
          </h4>
        </div>

        {description && (
          <p style={{ margin: '0 0 12px 0', fontSize: '13px', color: '#c7d2fe', lineHeight: 1.4 }}>
            {description}
          </p>
        )}

        {hasHosts && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px', flexWrap: 'wrap' }}>
            <span style={{ fontSize: '12px', color: '#a5b4fc', fontWeight: 500 }}>Target Hosts:</span>
            {targetHosts!.map((h) => (
              <span
                key={h}
                style={{
                  background: '#312e81',
                  border: '1px solid #4f46e5',
                  color: '#e0e7ff',
                  fontSize: '12px',
                  fontWeight: 600,
                  padding: '2px 8px',
                  borderRadius: '4px',
                }}
              >
                {h}
              </span>
            ))}
          </div>
        )}

        <div style={{ display: 'flex', gap: '12px' }}>
          {hasHosts && (
            <button
              onClick={onAgree}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '6px 14px',
                background: '#4f46e5',
                border: 'none',
                borderRadius: '6px',
                color: '#ffffff',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              <CheckCircle style={{ width: '15px', height: '15px' }} />
              Agree & Proceed (Yes)
            </button>
          )}
          <button
            onClick={onDecline}
            style={{
              padding: '6px 14px',
              background: '#312e81',
              border: '1px solid #4338ca',
              borderRadius: '6px',
              color: '#c7d2fe',
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            Decline (No)
          </button>
        </div>
      </div>
    );
  }

  // Database Query Review
  if (props.type === 'database') {
    const { title, description, environment = 'prod', sql, onApproveExecute, onDecline } = props;
    const isProd = environment.toLowerCase() === 'prod';

    return (
      <div
        style={{
          background: '#18181b',
          border: isProd ? '1px solid #f59e0b' : '1px solid #3b82f6',
          borderRadius: '8px',
          padding: '16px',
          marginTop: '12px',
          marginBottom: '12px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Database style={{ width: '20px', height: '20px', color: isProd ? '#f59e0b' : '#60a5fa' }} />
            <h4 style={{ margin: 0, fontSize: '15px', color: '#f4f4f5', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span>{title || 'Database Query Approval Required'}</span>
              <span
                style={{
                  background: isProd ? 'rgba(245, 158, 11, 0.2)' : 'rgba(59, 130, 246, 0.2)',
                  border: isProd ? '1px solid #f59e0b' : '1px solid #3b82f6',
                  color: isProd ? '#fcd34d' : '#93c5fd',
                  fontSize: '11px',
                  fontWeight: 700,
                  padding: '2px 8px',
                  borderRadius: '12px',
                  textTransform: 'uppercase',
                }}
              >
                {environment} ({isProd ? 'PAG0035L' : 'TAG0026L'})
              </span>
            </h4>
          </div>
          <span style={{ fontSize: '11px', color: '#a1a1aa', fontWeight: 500 }}>
            {isProd ? 'High Impact (Production)' : 'Test Environment'}
          </span>
        </div>

        {description && (
          <p style={{ margin: '0 0 12px 0', fontSize: '13px', color: '#a1a1aa', lineHeight: 1.4 }}>
            {description}
          </p>
        )}

        {sql && (
          <div
            style={{
              background: '#09090b',
              border: '1px solid #27272a',
              borderRadius: '6px',
              padding: '10px 14px',
              fontFamily: 'Consolas, monospace',
              fontSize: '13px',
              color: '#60a5fa',
              marginBottom: '14px',
              whiteSpace: 'pre-wrap',
              overflowX: 'auto',
            }}
          >
            {sql}
          </div>
        )}

        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            onClick={onApproveExecute}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '8px 16px',
              background: '#2563eb',
              border: 'none',
              borderRadius: '6px',
              color: '#ffffff',
              fontSize: '13px',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            <Play style={{ width: '14px', height: '14px' }} fill="currentColor" />
            Approve & Execute Query
          </button>
          <button
            onClick={onDecline}
            style={{
              padding: '8px 16px',
              background: '#27272a',
              border: '1px solid #3f3f46',
              borderRadius: '6px',
              color: '#d4d4d8',
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            Decline / Cancel
          </button>
        </div>
      </div>
    );
  }

  // Command Execution Review
  const { title, description, criticalityLevel = 0, onApproveExecute, onDecline } = props;

  const getCriticalityBadge = (level: number) => {
    switch (level) {
      case 1:
        return {
          tag: '(Warning)',
          label: 'Level 1: Warning / Process Action',
          gradient: 'linear-gradient(135deg, #d97706 0%, #ea580c 100%)',
          borderColor: '#f59e0b',
        };
      case 2:
        return {
          tag: '(Critical)',
          label: 'Level 2: Critical State Change',
          gradient: 'linear-gradient(135deg, #ea580c 0%, #dc2626 100%)',
          borderColor: '#ef4444',
        };
      case 3:
        return {
          tag: '(High Critical)',
          label: 'Level 3: Severe State Change / Deletion',
          gradient: 'linear-gradient(135deg, #b91c1c 0%, #7f1d1d 100%)',
          borderColor: '#f87171',
        };
      default:
        return {
          tag: '(Normal)',
          label: 'Level 0: Inspection Action',
          gradient: 'linear-gradient(135deg, #059669 0%, #0d9488 100%)',
          borderColor: '#10b981',
        };
    }
  };

  const badge = getCriticalityBadge(criticalityLevel);

  return (
    <div
      style={{
        background: '#18181b',
        border: `1px solid ${badge.borderColor}`,
        borderRadius: '8px',
        padding: '16px',
        marginTop: '12px',
        marginBottom: '12px',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ShieldAlert style={{ width: '20px', height: '20px', color: badge.borderColor }} />
          <h4 style={{ margin: 0, fontSize: '15px', color: '#f4f4f5', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>{title || 'Command Review & Approval Required'}</span>
            <span
              style={{
                background: badge.gradient,
                border: `1px solid ${badge.borderColor}`,
                color: '#ffffff',
                fontSize: '12px',
                fontWeight: 700,
                padding: '2px 8px',
                borderRadius: '12px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.3)',
              }}
            >
              {badge.tag}
            </span>
          </h4>
        </div>
        <span style={{ fontSize: '11px', color: '#a1a1aa', fontWeight: 500 }}>
          {badge.label}
        </span>
      </div>

      {description && (
        <p style={{ margin: '0 0 12px 0', fontSize: '13px', color: '#a1a1aa', lineHeight: 1.4 }}>
          {description}
        </p>
      )}

      <div style={{ display: 'flex', gap: '12px' }}>
        <button
          onClick={onApproveExecute}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '8px 16px',
            background: '#16a34a',
            border: 'none',
            borderRadius: '6px',
            color: '#ffffff',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          <Terminal style={{ width: '15px', height: '15px' }} />
          Approve & Execute Command
        </button>
        <button
          onClick={onDecline}
          style={{
            padding: '8px 16px',
            background: '#27272a',
            border: '1px solid #3f3f46',
            borderRadius: '6px',
            color: '#d4d4d8',
            fontSize: '13px',
            cursor: 'pointer',
          }}
        >
          Decline / Refine
        </button>
      </div>
    </div>
  );
};
