import { logger } from '../utils/logger';

const envBaseUrl = (import.meta as any).env?.VITE_BACKEND_URL;
export const baseUrl: string = envBaseUrl || (
  typeof window !== 'undefined' && window.location.hostname
    ? (window.location.port === '9011'
        ? window.location.origin
        : `${window.location.protocol}//${window.location.hostname}:9011`)
    : 'http://localhost:9011'
);


/**
 * Executes a REST fetch request to SpringBoot backend.
 * Automatically attaches credentials and handles 401 Unauthorized errors.
 */
async function fetchWithAuth(endpoint: string, options: RequestInit = {}): Promise<Response> {
  const url = `${baseUrl}${endpoint}`;
  const headers = new Headers(options.headers || {});
  headers.set('Content-Type', 'application/json');

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: 'include', // Ensures HttpOnly cookies are attached automatically
  });

  if (response.status === 401) {
    logger.warn('api.ts', 'fetchWithAuth', 'Received HTTP 401 Unauthorized response from server');
    // Dispatch global event for auth failure / session expiry
    window.dispatchEvent(new CustomEvent('auth:session_expired'));
  }

  return response;
}

export async function fetchServerInventory(): Promise<any[]> {
  try {
    const res = await fetchWithAuth('/api/v1/terminal/inventory');
    if (!res.ok) throw new Error(`HTTP error ${res.status}`);
    return await res.json();
  } catch (err) {
    logger.error('api.ts', 'fetchServerInventory', 'Failed to fetch server inventory', err);
    throw err;
  }
}

export async function fetchServerStatus(): Promise<Record<string, any>> {
  try {
    const res = await fetchWithAuth('/api/v1/terminal/status');
    if (!res.ok) throw new Error(`HTTP error ${res.status}`);
    return await res.json();
  } catch (err) {
    logger.error('api.ts', 'fetchServerStatus', 'Failed to fetch server status', err);
    return {};
  }
}

export async function renameTerminalApi(terminalId: string, newName: string): Promise<boolean> {
  try {
    const res = await fetchWithAuth('/api/v1/terminal/rename', {
      method: 'POST',
      body: JSON.stringify({ terminalId, newName }),
    });
    return res.ok;
  } catch (err) {
    logger.error('api.ts', 'renameTerminalApi', 'Failed to rename terminal', err);
    return false;
  }
}
