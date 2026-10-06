import { createI18n } from 'vue-i18n';

import ar from './ar.json';
import fr from './fr.json';

export const SUPPORTED_LOCALES = ['fr', 'ar'] as const;
export type AppLocale = (typeof SUPPORTED_LOCALES)[number];

const STORAGE_KEY = 'tonti:locale';

type MessageSchema = typeof fr;

declare module 'vue-i18n' {
  export interface DefineLocaleMessage extends MessageSchema {}
}

function detectLocale(): AppLocale {
  const stored = window.localStorage.getItem(STORAGE_KEY);
  if (stored && (SUPPORTED_LOCALES as readonly string[]).includes(stored)) return stored as AppLocale;
  return navigator.language?.toLowerCase().startsWith('ar') ? 'ar' : 'fr';
}

export function createI18nInstance() {
  const initialLocale = detectLocale();
  const i18n = createI18n<[MessageSchema], AppLocale, false>({
    legacy: false,
    locale: initialLocale,
    fallbackLocale: 'fr',
    messages: { fr, ar },
  });
  return { i18n, initialLocale };
}

export function persistLocale(locale: string) {
  window.localStorage.setItem(STORAGE_KEY, locale);
}
