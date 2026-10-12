import React, { useState } from 'react';
import { Server, HelpCircle, CheckCircle2 } from 'lucide-react';

interface HostSelectionOptionModalProps {
  onClose: () => void;
  onChooseKnowHosts: () => void;
  onChooseDontKnowHosts: () => void;
}

export const HostSelectionOptionModal: React.FC<HostSelectionOptionModalProps> = ({
  onClose,
  onChooseKnowHosts,
  onChooseDontKnowHosts,
}) => {
  const [showSmartAiInfo, setShowSmartAiInfo] = useState(false);

  const handleDontKnow = () => {
    setShowSmartAiInfo(true);
  };

  const handleSmartAiOk = () => {
    setShowSmartAiInfo(false);
    onChooseDontKnowHosts();
    onClose();
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(15, 23, 42, 0.75)',
        backdropFilter: 'blur(4px)',
        zIndex: 1100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px',
      }}
    >
      <div
        style={{
          background: '#1e293b',
          border: '1px solid #334155',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '480px',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
          overflow: 'hidden',
          color: '#f8fafc',
        }}
      >
        <div style={{ padding: '20px 24px', borderBottom: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Server style={{ width: '20px', height: '20px', color: '#38bdf8' }} />
            Target Host Selection
          </h3>
          <button
            onClick={onClose}
            style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '18px' }}
          >
            ✕
          </button>
        </div>

        <div style={{ padding: '24px' }}>
          {!showSmartAiInfo ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <p style={{ margin: 0, color: '#94a3b8', fontSize: '14px' }}>
                Do you know which specific target host server(s) you want ASR_AI to interact with?
              </p>

              <button
                onClick={() => {
                  onChooseKnowHosts();
                  onClose();
                }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '14px 16px',
                  background: '#0f172a',
                  border: '1px solid #3b82f6',
                  borderRadius: '8px',
                  color: '#f8fafc',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.2s',
                }}
              >
                <Server style={{ width: '24px', height: '24px', color: '#60a5fa', flexShrink: 0 }} />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>I know the host(s)</div>
                  <div style={{ fontSize: '12px', color: '#94a3b8' }}>Select specific target servers from the server inventory tree</div>
                </div>
              </button>

              <button
                onClick={handleDontKnow}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '14px 16px',
                  background: '#0f172a',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  color: '#f8fafc',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'all 0.2s',
                }}
              >
                <HelpCircle style={{ width: '24px', height: '24px', color: '#a855f7', flexShrink: 0 }} />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>I don't know the host(s)</div>
                  <div style={{ fontSize: '12px', color: '#94a3b8' }}>Let ASR_AI automatically resolve target hosts based on your query</div>
                </div>
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: '16px', padding: '12px 0' }}>
              <div style={{ width: '48px', height: '48px', borderRadius: '50%', background: 'rgba(168, 85, 247, 0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <CheckCircle2 style={{ width: '28px', height: '28px', color: '#c084fc' }} />
              </div>
              <div style={{ fontSize: '15px', fontWeight: 500, color: '#f8fafc', lineHeight: 1.5 }}>
                Don't worry! Our <strong>ASR_AI</strong> is smart enough to help you resolve your issue/query and auto-determine the target host.
              </div>
              <button
                onClick={handleSmartAiOk}
                style={{
                  marginTop: '8px',
                  padding: '8px 24px',
                  background: '#8b5cf6',
                  border: 'none',
                  borderRadius: '6px',
                  color: '#ffffff',
                  fontWeight: 600,
                  fontSize: '14px',
                  cursor: 'pointer',
                }}
              >
                OK
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
