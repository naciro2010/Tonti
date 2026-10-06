import { SecureStorage } from '@aparajita/capacitor-secure-storage';
import { Capacitor } from '@capacitor/core';

/**
 * Stockage des jetons de session.
 *
 * - iOS / Android : Keychain / Keystore via SecureStorage (le localStorage d'une WebView
 *   peut être purgé par le système et n'est pas chiffré).
 * - Web : localStorage.
 *
 * Les valeurs sont gardées en mémoire après `init()` pour que le client HTTP y accède
 * de façon synchrone.
 */
const KEYS = ['accessToken', 'refreshToken', 'user'] as const;
type Key = (typeof KEYS)[number];

const PREFIX = 'tonti:';
const cache = new Map<Key, string>();
const native = Capacitor.isNativePlatform();

async function readPersisted(key: Key): Promise<string | null> {
  if (native) return SecureStorage.getItem(key);
  return window.localStorage.getItem(PREFIX + key);
}

function writePersisted(key: Key, value: string | null): Promise<void> {
  if (native) {
    return value === null ? SecureStorage.removeItem(key) : SecureStorage.setItem(key, value);
  }
  if (value === null) window.localStorage.removeItem(PREFIX + key);
  else window.localStorage.setItem(PREFIX + key, value);
  return Promise.resolve();
}

export const tokenStore = {
  async init(): Promise<void> {
    if (native) await SecureStorage.setKeyPrefix(PREFIX);
    await Promise.all(
      KEYS.map(async (key) => {
        try {
          const value = await readPersisted(key);
          if (value !== null) cache.set(key, value);
        } catch {
          // Donnée illisible (ex : Keychain réinitialisé) : on repart d'une session vide
        }
      }),
    );
  },

  get(key: Key): string | null {
    return cache.get(key) ?? null;
  },

  set(key: Key, value: string | null): void {
    if (value === null) cache.delete(key);
    else cache.set(key, value);
    void writePersisted(key, value).catch(() => undefined);
  },

  clear(): void {
    KEYS.forEach((key) => tokenStore.set(key, null));
  },
};
