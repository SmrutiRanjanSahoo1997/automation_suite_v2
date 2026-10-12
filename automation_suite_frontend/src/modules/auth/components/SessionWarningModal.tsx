import React, { useState } from 'react';
import { Clock, LogIn, X } from 'lucide-react';
import { loginApi } from '../services/authService';
import { AuthUserInfo } from '../repository/authRepo';
import { logger } from '../../../utils/logger';

interface SessionWarningModalProps {
  isOpen: boolean;
  activeUserId?: string;
  onClose: () => void;
  onLoginSuccess: (userInfo: AuthUserInfo) => void;
}

export const SessionWarningModal: React.FC<SessionWarningModalProps> = ({
  isOpen,
  activeUserId = '',
  onClose,
  onLoginSuccess,
}) => {
  const [showForm, setShowForm] = useState(false);
  const [userId, setUserId] = useState(activeUserId);
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleProceedLogin = () => {
    setShowForm(true);
  };

  const handleSubmitLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    const uid = userId.trim() || activeUserId;
    if (!uid || !password.trim()) {
      setErrorMessage('User ID and Password are required.');
      return;
    }

    setLoading(true);
    logger.info('SessionWarningModal.tsx', 'handleSubmitLogin', 'Submitting session refresh login', { userId: uid });

    try {
      const userInfo = await loginApi(uid, password);
      setLoading(false);
      setShowForm(false);
      setPassword('');
      onLoginSuccess(userInfo);
    } catch (err) {
      setLoading(false);
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(msg);
      logger.error('SessionWarningModal.tsx', 'handleSubmitLogin', 'Session refresh login error', err);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        background: 'rgba(10, 10, 10, 0.85)',
        backdropFilter: 'blur(6px)',
        zIndex: 2000,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px',
      }}
    >
      <div
        style={{
          background: '#18181b',
          border: '1px solid #f97316',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '420px',
          padding: '24px',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.8)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
          <Clock style={{ width: '24px', height: '24px', color: '#f97316' }} />
          <h2 style={{ margin: 0, fontSize: '18px', color: '#ffffff', fontWeight: 700 }}>Session Expiring Soon</h2>
        </div>

        <p style={{ fontSize: '13px', color: '#a1a1aa', marginBottom: '20px', lineHeight: 1.5 }}>
          Session is going to expire in 5 min please login again for uninterrupted service.
        </p>

        {errorMessage && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid #ef4444', borderRadius: '6px', padding: '10px', fontSize: '12px', color: '#ef4444', marginBottom: '16px' }}>
            {errorMessage}
          </div>
        )}

        {!showForm ? (
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
            <button
              onClick={onClose}
              style={{
                background: '#27272a',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 16px',
                fontSize: '13px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              Cancel
            </button>
            <button
              onClick={handleProceedLogin}
              style={{
                background: '#eab308',
                color: '#000000',
                border: 'none',
                borderRadius: '6px',
                padding: '8px 16px',
                fontSize: '13px',
                fontWeight: 700,
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
              }}
            >
              <LogIn style={{ width: '14px', height: '14px' }} />
              Login
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmitLogin} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', color: '#a1a1aa', marginBottom: '4px', fontWeight: 600 }}>User ID</label>
              <input
                type="text"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                style={{ width: '100%', background: '#0a0a0a', border: '1px solid #27272a', borderRadius: '6px', padding: '8px 12px', color: '#ffffff', fontSize: '13px', boxSizing: 'border-box' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12px', color: '#a1a1aa', marginBottom: '4px', fontWeight: 600 }}>Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ width: '100%', background: '#0a0a0a', border: '1px solid #27272a', borderRadius: '6px', padding: '8px 12px', color: '#ffffff', fontSize: '13px', boxSizing: 'border-box' }}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '8px' }}>
              <button
                type="button"
                onClick={() => setShowForm(false)}
                style={{ background: '#27272a', color: '#ffffff', border: 'none', borderRadius: '6px', padding: '8px 14px', fontSize: '13px', cursor: 'pointer' }}
              >
                Back
              </button>
              <button
                type="submit"
                disabled={loading}
                style={{ background: '#eab308', color: '#000000', border: 'none', borderRadius: '6px', padding: '8px 16px', fontSize: '13px', fontWeight: 700, cursor: loading ? 'wait' : 'pointer' }}
              >
                {loading ? 'Logging in...' : 'Login Now'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
