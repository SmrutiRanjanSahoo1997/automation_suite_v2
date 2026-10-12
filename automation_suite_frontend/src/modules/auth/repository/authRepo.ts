/**
 * Authentication Session User Info Repository.
 * Stores user metadata in IndexedDB (or local Session Store).
 * Note: JWT token is NEVER stored here; token is managed strictly via HttpOnly Cookies.
 */

export interface AuthUserInfo {
  userId: string;
  userName: string;
  userMail: string;
  expireTimestamp: number;
  isPasswordChangeRequired: boolean;
}

const DB_NAME = 'AuthSessionDB';
const DB_VERSION = 1;
const STORE_NAME = 'user_info';

function openAuthDb(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'userId' });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

export async function saveAuthUserInfo(info: AuthUserInfo): Promise<void> {
  const db = await openAuthDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const req = store.put(info);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}

export async function getActiveAuthUserInfo(): Promise<AuthUserInfo | null> {
  const db = await openAuthDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly');
    const store = tx.objectStore(STORE_NAME);
    const req = store.getAll();
    req.onsuccess = () => {
      const results = req.result as AuthUserInfo[];
      if (results && results.length > 0) {
        resolve(results[0]);
      } else {
        resolve(null);
      }
    };
    req.onerror = () => reject(req.error);
  });
}

export async function clearAuthUserInfo(): Promise<void> {
  const db = await openAuthDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const req = store.clear();
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}
