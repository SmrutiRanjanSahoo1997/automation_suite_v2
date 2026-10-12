import React, { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import { AsrAiDashboardPage } from './modules/ai/AsrAiDashboardPage';
import { TerminalDashboardPage } from './modules/terminal/TerminalDashboardPage';
import { DatabaseDashboardPage } from './modules/database/DatabaseDashboardPage';
import { AuthUserInfo, getActiveAuthUserInfo } from './modules/auth/repository/authRepo';
import { startSessionExpiryMonitor, logoutUser } from './modules/auth/services/authService';
import { LoginModal } from './modules/auth/components/LoginModal';
import { SessionWarningModal } from './modules/auth/components/SessionWarningModal';
import { ChangePasswordModal } from './modules/auth/components/ChangePasswordModal';

import './styles/theme.css';
import './styles/terminalDashboard.css';
import './styles/asrAiDashboard.css';

export const App: React.FC = () => {
  const [activeDashboardId, setActiveDashboardId] = useState<string>('asr_ai');
  const [userInfo, setUserInfo] = useState<AuthUserInfo | null>(null);

  const [isLoginOpen, setIsLoginOpen] = useState(false);
  const [isWarningOpen, setIsWarningOpen] = useState(false);
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false);
  const [isForcedPasswordChange, setIsForcedPasswordChange] = useState(false);

  useEffect(() => {
    // 1. Initial auth state check
    getActiveAuthUserInfo().then(async (info) => {
      if (info) {
        if (info.expireTimestamp && info.expireTimestamp <= Date.now()) {
          // Session expired: Clear stale IndexedDB metadata & prompt for login
          await logoutUser();
          setUserInfo(null);
          setIsLoginOpen(true);
        } else {
          setUserInfo(info);
          if (info.isPasswordChangeRequired) {
            setIsForcedPasswordChange(true);
            setIsChangePasswordOpen(true);
          }
        }
      } else {
        setIsLoginOpen(true);
      }
    });

    // 2. Start pre-expiry monitor
    const stopMonitor = startSessionExpiryMonitor(
      () => setIsWarningOpen(true),
      () => setIsLoginOpen(true)
    );

    return () => stopMonitor();
  }, []);

  const handleAuthSuccess = (info: AuthUserInfo) => {
    setUserInfo(info);
    setIsLoginOpen(false);
    setIsWarningOpen(false);

    if (info.isPasswordChangeRequired) {
      setIsForcedPasswordChange(true);
      setIsChangePasswordOpen(true);
    } else {
      setIsForcedPasswordChange(false);
      setIsChangePasswordOpen(false);
    }
  };

  const handleLogout = async () => {
    await logoutUser();
    setUserInfo(null);
    setIsLoginOpen(true);
  };

  const handleOpenVoluntaryChangePassword = () => {
    setIsForcedPasswordChange(false);
    setIsChangePasswordOpen(true);
  };

  return (
    <div style={{ display: 'flex', width: '100vw', height: '100vh', background: '#09090b', color: '#ffffff', overflow: 'hidden' }}>
      {/* Left Navigation Sidebar */}
      <Sidebar
        activeDashboardId={activeDashboardId}
        onSelectDashboard={(id) => setActiveDashboardId(id)}
        userInfo={userInfo}
        onOpenChangePassword={handleOpenVoluntaryChangePassword}
        onLogout={handleLogout}
      />

      {/* Main Workspace View */}
      <main style={{ flex: 1, position: 'relative', height: '100vh', overflow: 'hidden' }}>
        {activeDashboardId === 'asr_ai' && <AsrAiDashboardPage />}
        {activeDashboardId === 'terminal' && <TerminalDashboardPage />}
        {activeDashboardId === 'db_dashboard' && <DatabaseDashboardPage />}
      </main>

      {/* Global Authentication Modals Mount */}
      <LoginModal
        isOpen={isLoginOpen}
        onLoginSuccess={handleAuthSuccess}
      />

      <SessionWarningModal
        isOpen={isWarningOpen}
        activeUserId={userInfo?.userId}
        onClose={() => setIsWarningOpen(false)}
        onLoginSuccess={handleAuthSuccess}
      />

      <ChangePasswordModal
        isOpen={isChangePasswordOpen}
        userId={userInfo?.userId || 'user'}
        isForced={isForcedPasswordChange}
        onClose={() => setIsChangePasswordOpen(false)}
        onSuccess={handleAuthSuccess}
      />
    </div>
  );
};

export default App;
