import { baseUrl } from '../../../network/api';
import { logger } from '../../../utils/logger';
import { AuthUserInfo, saveAuthUserInfo, getActiveAuthUserInfo, clearAuthUserInfo } from '../repository/authRepo';

export interface AuthResponsePayload {
  userId: string;
  token: string;
  expireTimestamp: number;
  userName: string;
  userMail: string;
  isPasswordChangeRequired: boolean;
}

export async function loginApi(userId: string, password: string): Promise<AuthUserInfo> {
  logger.info('authService.ts', 'loginApi', 'Executing login request', { userId });
  const response = await fetch(`${baseUrl}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, password }),
    credentials: 'include', // Pushes token to HttpOnly Cookie automatically
  });

  if (!response.ok) {
    const errorText = await response.text();
    logger.error('authService.ts', 'loginApi', 'Login failed', errorText);
    throw new Error(errorText || 'Invalid credentials or login failed.');
  }

  const payload: AuthResponsePayload = await response.json();
  const userInfo: AuthUserInfo = {
    userId: payload.userId,
    userName: payload.userName,
    userMail: payload.userMail,
    expireTimestamp: payload.expireTimestamp,
    isPasswordChangeRequired: Boolean(payload.isPasswordChangeRequired),
  };

  await saveAuthUserInfo(userInfo);
  logger.info('authService.ts', 'loginApi', 'Login successful; updated user_info session metadata', userInfo);
  return userInfo;
}

export async function changePasswordApi(userId: string, oldPassword: string, newPassword: string): Promise<AuthUserInfo> {
  logger.info('authService.ts', 'changePasswordApi', 'Executing change password request', { userId });
  const response = await fetch(`${baseUrl}/api/v1/auth/change-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, oldPassword, newPassword }),
    credentials: 'include',
  });

  if (!response.ok) {
    const errorText = await response.text();
    logger.error('authService.ts', 'changePasswordApi', 'Change password failed', errorText);
    throw new Error(errorText || 'Failed to change password.');
  }

  const payload: AuthResponsePayload = await response.json();
  const userInfo: AuthUserInfo = {
    userId: payload.userId,
    userName: payload.userName,
    userMail: payload.userMail,
    expireTimestamp: payload.expireTimestamp,
    isPasswordChangeRequired: Boolean(payload.isPasswordChangeRequired),
  };

  await saveAuthUserInfo(userInfo);
  logger.info('authService.ts', 'changePasswordApi', 'Password changed successfully; user re-authenticated', userInfo);
  return userInfo;
}

export async function logoutUser(): Promise<void> {
  logger.info('authService.ts', 'logoutUser', 'Logging out user and clearing session');
  await clearAuthUserInfo();
}

/**
 * Monitors session expiration in background.
 * Triggers onWarning when 5 minutes or less remain before expireTimestamp.
 * Triggers onExpired when expireTimestamp <= Date.now().
 */
export function startSessionExpiryMonitor(
  onWarning: () => void,
  onExpired: () => void
): () => void {
  let warningTriggered = false;

  const intervalId = setInterval(async () => {
    try {
      const user = await getActiveAuthUserInfo();
      if (!user || !user.expireTimestamp) return;

      const timeRemaining = user.expireTimestamp - Date.now();

      if (timeRemaining <= 0) {
        logger.warn('authService.ts', 'startSessionExpiryMonitor', 'Session has expired');
        onExpired();
      } else if (timeRemaining <= 5 * 60 * 1000 && !warningTriggered) {
        warningTriggered = true;
        logger.warn('authService.ts', 'startSessionExpiryMonitor', 'Session expires in <= 5 minutes; triggering warning');
        onWarning();
      }
    } catch (err) {
      logger.error('authService.ts', 'startSessionExpiryMonitor', 'Error checking session expiry', err);
    }
  }, 10000);

  return () => clearInterval(intervalId);
}
