import { baseUrl } from '../../../network/api';
import { SavedQuery } from '../types';

export const databaseSavedQueryService = {
  async listSavedQueries(): Promise<SavedQuery[]> {
    const res = await fetch(`${baseUrl}/api/v2/database/saved-queries`, {
      credentials: 'include',
    });
    if (!res.ok) {
      throw new Error(`Failed to list saved queries: ${res.statusText}`);
    }
    return await res.json();
  },

  async getSavedQuery(fileName: string): Promise<string> {
    const encoded = encodeURIComponent(fileName);
    const res = await fetch(`${baseUrl}/api/v2/database/saved-queries/${encoded}`, {
      credentials: 'include',
    });
    if (!res.ok) {
      throw new Error(`Failed to get query content: ${res.statusText}`);
    }
    const data = await res.json();
    return data.content || '';
  },

  async saveQuery(fileName: string, content: string): Promise<boolean> {
    const res = await fetch(`${baseUrl}/api/v2/database/saved-queries`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ fileName, content }),
    });
    return res.ok;
  },

  async deleteQuery(fileName: string): Promise<boolean> {
    const encoded = encodeURIComponent(fileName);
    const res = await fetch(`${baseUrl}/api/v2/database/saved-queries/${encoded}`, {
      method: 'DELETE',
      credentials: 'include',
    });
    return res.ok;
  },
};
