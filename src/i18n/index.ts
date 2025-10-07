import { createI18n } from 'vue-i18n';

import ar from './ar.json';
import fr from './fr.json';

const STORAGE_KEY = 'tonti:locale';

type MessageSchema = typeof fr;

declare module 'vue-i18n' {
  export interface DefineLocaleMessage extends MessageSchema {}
}

export function createI18nInstance() {
  const initialLocale =
    typeof window !== 'undefined' ? (window.localStorage.getItem(STORAGE_KEY) ?? 'fr') : 'fr';

  const i18n = createI18n({
    legacy: false,
    locale: initialLocale,
    fallbackLocale: 'fr',
    messages: {
      fr,
      ar,
    },
  });

  return { i18n, initialLocale };
}

export function persistLocale(locale: string) {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(STORAGE_KEY, locale);
  }
}
