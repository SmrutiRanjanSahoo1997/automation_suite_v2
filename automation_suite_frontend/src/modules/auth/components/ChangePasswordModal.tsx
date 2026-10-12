import React, { useState } from 'react';
import { KeyRound, Check, X, AlertCircle } from 'lucide-react';
import { changePasswordApi } from '../services/authService';
import { validatePasswordRules } from '../utils/passwordValidator';
import { AuthUserInfo } from '../repository/authRepo';
import { logger } from '../../../utils/logger';

interface ChangePasswordModalProps {
  isOpen: boolean;
  userId: string;
  isForced?: boolean; // When true: dismissable = false and Cancel button is hidden
  onClose?: () => void;
  onSuccess: (userInfo: AuthUserInfo) => void;
}

export const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({
  isOpen,
  userId,
  isForced = false,
  onClose,
  onSuccess,
}) => {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!oldPassword || !newPassword || !confirmPassword) {
      setErrorMessage('All fields are required.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage('New Password and Confirm Password do not match.');
      return;
    }

    const validation = validatePasswordRules(newPassword);
    if (!validation.isValid) {
      setErrorMessage(validation.errorReason || 'Password does not meet rule requirements.');
      return;
    }

    setLoading(true);
    logger.info('ChangePasswordModal.tsx', 'handleSubmit', 'Submitting password change request', { userId });

    try {
      const userInfo = await changePasswordApi(userId, oldPassword, newPassword);
      setLoading(false);
      onSuccess(userInfo);
    } catch (err) {
      setLoading(false);
      const msg = err instanceof Error ? err.message : String(err);
      setErrorMessage(msg);
      logger.error('ChangePasswordModal.tsx', 'handleSubmit', 'Change password error', err);
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
        zIndex: 2100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px',
      }}
    >
      <div
        style={{
          background: '#18181b',
          border: isForced ? '2px solid #ef4444' : '1px solid #eab308',
          borderRadius: '12px',
          width: '100%',
          maxWidth: '440px',
          padding: '24px',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.8)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <KeyRound style={{ width: '22px', height: '22px', color: isForced ? '#ef4444' : '#eab308' }} />
            <h2 style={{ margin: 0, fontSize: '17px', color: '#ffffff', fontWeight: 700 }}>
              {isForced ? 'Password Change Required' : 'Change Password'}
            </h2>
          </div>
          {!isForced && onClose && (
            <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#71717a', cursor: 'pointer' }}>
              <X style={{ width: '18px', height: '18px' }} />
            </button>
          )}
        </div>

        {isForced && (
          <p style={{ fontSize: '12px', color: '#f87171', marginBottom: '16px', lineHeight: 1.4 }}>
            Your account requires an immediate password update before you can continue using the application.
          </p>
        )}

        {errorMessage && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid #ef4444', borderRadius: '6px', padding: '10px', fontSize: '12px', color: '#ef4444', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <AlertCircle style={{ width: '16px', height: '16px', flexShrink: 0 }} />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', color: '#a1a1aa', marginBottom: '4px', fontWeight: 600 }}>Old Password</label>
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              placeholder="Enter current password"
              style={{ width: '100%', background: '#0a0a0a', border: '1px solid #27272a', borderRadius: '6px', padding: '9px 12px', color: '#ffffff', fontSize: '13px', outline: 'none', boxSizing: 'border-box' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', color: '#a1a1aa', marginBottom: '4px', fontWeight: 600 }}>New Password</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Enter new password"
              style={{ width: '100%', background: '#0a0a0a', border: '1px solid #27272a', borderRadius: '6px', padding: '9px 12px', color: '#ffffff', fontSize: '13px', outline: 'none', boxSizing: 'border-box' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', color: '#a1a1aa', marginBottom: '4px', fontWeight: 600 }}>Confirm New Password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Re-enter new password"
              style={{ width: '100%', background: '#0a0a0a', border: '1px solid #27272a', borderRadius: '6px', padding: '9px 12px', color: '#ffffff', fontSize: '13px', outline: 'none', boxSizing: 'border-box' }}
            />
          </div>

          <div style={{ background: '#0a0a0a', border: '1px solid #27272a', borderRadius: '6px', padding: '10px', fontSize: '11px', color: '#71717a', lineHeight: 1.5 }}>
            <div style={{ fontWeight: 600, color: '#a1a1aa', marginBottom: '4px' }}>Password Rules:</div>
            • Minimum 8 characters long<br />
            • At least 1 uppercase (A-Z) & 1 lowercase (a-z)<br />
            • At least 1 digit (0-9)<br />
            • At least 1 symbol (~!@#$%^&*-_+=.?:)<br />
            • No spaces or tabs allowed
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '8px' }}>
            {!isForced && onClose && (
              <button
                type="button"
                onClick={onClose}
                style={{ background: '#27272a', color: '#ffffff', border: 'none', borderRadius: '6px', padding: '9px 16px', fontSize: '13px', cursor: 'pointer' }}
              >
                Cancel
              </button>
            )}
            <button
              type="submit"
              disabled={loading}
              style={{ background: '#eab308', color: '#000000', border: 'none', borderRadius: '6px', padding: '9px 18px', fontSize: '13px', fontWeight: 700, cursor: loading ? 'wait' : 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <Check style={{ width: '14px', height: '14px' }} />
              {loading ? 'Updating...' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
