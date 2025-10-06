import { useEffect, useState } from 'react';
import type { Locale, TranslationKey } from './i18n';
import { defaultLocale, translate } from './i18n';

const STORAGE_KEY = 'tonti-lang';

function getInitialLocale(): Locale {
  if (typeof window === 'undefined') {
    return defaultLocale;
  }
  const stored = window.localStorage.getItem(STORAGE_KEY) as Locale | null;
  if (stored === 'fr' || stored === 'ar') {
    return stored;
  }
  const browser = window.navigator.language.startsWith('ar') ? 'ar' : 'fr';
  return browser as Locale;
}

export function setLocale(locale: Locale) {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEY, locale);
  window.dispatchEvent(new CustomEvent('tonti:lang-changed', { detail: locale }));
}

export function useTranslation() {
  const [locale, setLocaleState] = useState<Locale>(getInitialLocale);

  useEffect(() => {
    const handler = (event: Event) => {
      const detail = (event as CustomEvent<Locale>).detail;
      if (detail === 'fr' || detail === 'ar') {
        setLocaleState(detail);
      }
    };
    window.addEventListener('tonti:lang-changed', handler as EventListener);
    return () => window.removeEventListener('tonti:lang-changed', handler as EventListener);
  }, []);

  const t = (key: TranslationKey) => translate(key, locale);

  return {
    locale,
    t,
    setLocale: (next: Locale) => {
      setLocale(next);
    },
  };
}
