import React, { useState } from 'react';
import { Lock, LogIn, AlertCircle } from 'lucide-react';
import { loginApi } from '../services/authService';
import { AuthUserInfo } from '../repository/authRepo';
import { logger } from '../../../utils/logger';

interface LoginModalProps {
  isOpen: boolean;
  message?: string;
  onLoginSuccess: (userInfo: AuthUserInfo) => void;
}

export const LoginModal: React.FC<LoginModalProps> = ({
  isOpen,
  message = 'Please login to continue.',
  onLoginSuccess,
}) => {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!userId.trim() || !password.trim()) {
      setErrorMessage('User ID and Password are required.');
      return;
    }

    setLoading(true);
    logger.info('LoginModal.tsx', 'handleSubmit', 'Submitting login credentials', { userId });

    try {
      const userInfo = await loginApi(userId.trim(), password);
      setLoading(false);
      onLoginSuccess(userInfo);
    } catch (err) {
      setLoading(false);
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(msg);
      logger.error('LoginModal.tsx', 'handleSubmit', 'Login submission error', err);
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
          border: '1px solid #eab308',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '400px',
          padding: '24px',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.8)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
          <Lock style={{ width: '24px', height: '24px', color: '#eab308' }} />
          <h2 style={{ margin: 0, fontSize: '18px', color: '#ffffff', fontWeight: 700 }}>Login Required</h2>
        </div>

        <p style={{ fontSize: '13px', color: '#a1a1aa', marginBottom: '20px', lineHeight: 1.4 }}>
          {message}
        </p>

        {errorMessage && (
          <div
            style={{
              background: 'rgba(239, 68, 68, 0.1)',
              border: '1px solid #ef4444',
              borderRadius: '6px',
              padding: '10px',
              fontSize: '12px',
              color: '#ef4444',
              marginBottom: '16px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <AlertCircle style={{ width: '16px', height: '16px', flexShrink: 0 }} />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', color: '#a1a1aa', marginBottom: '6px', fontWeight: 600 }}>
              User ID
            </label>
            <input
              type="text"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              placeholder="Enter User ID"
              style={{
                width: '100%',
                background: '#0a0a0a',
                border: '1px solid #27272a',
                borderRadius: '6px',
                padding: '10px 12px',
                color: '#ffffff',
                fontSize: '14px',
                outline: 'none',
                boxSizing: 'border-box',
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', color: '#a1a1aa', marginBottom: '6px', fontWeight: 600 }}>
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter Password"
              style={{
                width: '100%',
                background: '#0a0a0a',
                border: '1px solid #27272a',
                borderRadius: '6px',
                padding: '10px 12px',
                color: '#ffffff',
                fontSize: '14px',
                outline: 'none',
                boxSizing: 'border-box',
              }}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            style={{
              marginTop: '8px',
              background: '#eab308',
              color: '#000000',
              border: 'none',
              borderRadius: '6px',
              padding: '10px 16px',
              fontSize: '14px',
              fontWeight: 700,
              cursor: loading ? 'wait' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
            }}
          >
            <LogIn style={{ width: '16px', height: '16px' }} />
            {loading ? 'Authenticating...' : 'Login'}
          </button>
        </form>
      </div>
    </div>
  );
};
