/**
 * IndexedDB Repository for Chat Messages.
 */

export interface AiChatMessageRecord {
  id: string;
  conversationId: string;
  sender: 'user' | 'ai' | 'system';
  text?: string;
  preResponse?: string;
  command?: string;
  platform?: 'PS' | 'CMD';
  reviewNeeded?: boolean;
  reviewRequiredHost?: boolean;
  reviewTitle?: string;
  reviewDescription?: string;
  criticalityLevel?: number;
  fileNeeded?: string[];
  sql?: string;
  environment?: 'prod' | 'test';
  reviewNeededDatabase?: boolean;
  finalOutcome?: string;
  accumulatedContextData?: string | null;
  targetHosts?: string[];
  timestamp: number;
  status?: 'pending' | 'success' | 'error';
}

const DB_NAME = 'AiStudioMessagesDB';
const DB_VERSION = 1;
const STORE_NAME = 'messages';

function openDb(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' });
        store.createIndex('conversationId', 'conversationId', { unique: false });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

export async function getAiMessagesByConversation(conversationId: string): Promise<AiChatMessageRecord[]> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly');
    const store = tx.objectStore(STORE_NAME);
    const index = store.index('conversationId');
    const req = index.getAll(conversationId);
    req.onsuccess = () => {
      const list = req.result as AiChatMessageRecord[];
      list.sort((a, b) => a.timestamp - b.timestamp);
      resolve(list);
    };
    req.onerror = () => reject(req.error);
  });
}

export async function saveAiChatMessage(msg: AiChatMessageRecord): Promise<void> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const req = store.put(msg);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}

export async function deleteAiMessagesByConversation(conversationId: string): Promise<void> {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const index = store.index('conversationId');
    const req = index.openCursor(IDBKeyRange.only(conversationId));
    req.onsuccess = () => {
      const cursor = req.result;
      if (cursor) {
        cursor.delete();
        cursor.continue();
      } else {
        resolve();
      }
    };
    req.onerror = () => reject(req.error);
  });
}
