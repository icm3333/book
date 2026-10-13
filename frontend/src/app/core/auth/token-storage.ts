import { Injectable } from '@angular/core';

export interface StoredSession {
  token: string;
  /** Epoch milliseconds after which the token is no longer accepted by the backend. */
  expiresAt: number;
}

const STORAGE_KEY = 'book-tracker.session';

/**
 * Persists the session in localStorage so a reload keeps the user signed in.
 *
 * Every access is guarded: storage can be unavailable (privacy modes, quota) or hold a value
 * written by an older build. In either case the app behaves as signed out rather than crashing.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorage {
  read(): StoredSession | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw === null) {
        return null;
      }
      const parsed: unknown = JSON.parse(raw);
      if (isStoredSession(parsed) && parsed.expiresAt > Date.now()) {
        return parsed;
      }
    } catch {
      // Corrupted value or storage unavailable: fall through and discard it.
    }
    this.clear();
    return null;
  }

  write(session: StoredSession): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    } catch {
      // Storage unavailable: the session still works for this tab, it just won't survive a reload.
    }
  }

  clear(): void {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // Nothing to clear.
    }
  }
}

function isStoredSession(value: unknown): value is StoredSession {
  const candidate = value as Partial<StoredSession> | null;
  return (
    typeof candidate === 'object' &&
    candidate !== null &&
    typeof candidate.token === 'string' &&
    typeof candidate.expiresAt === 'number'
  );
}
