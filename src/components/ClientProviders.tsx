import type { ReactNode } from 'react';
import { useEffect } from 'react';
import { ToastProvider } from './ui/Toast';
import type { Locale } from '../lib/i18n';
import { defaultLocale } from '../lib/i18n';
import { setLocale as persistLocale } from '../lib/useTranslation';

function applyLocale(locale: Locale) {
  const dir = locale === 'ar' ? 'rtl' : 'ltr';
  document.documentElement.lang = locale;
  document.documentElement.dir = dir;
}

export function ClientProviders({ children }: { children: ReactNode }) {
  useEffect(() => {
    const stored = (window.localStorage.getItem('tonti-lang') as Locale | null) ?? defaultLocale;
    applyLocale(stored);

    const handler = (event: Event) => {
      const detail = (event as CustomEvent<Locale>).detail;
      if (detail === 'fr' || detail === 'ar') {
        applyLocale(detail);
      }
    };

    window.addEventListener('tonti:lang-changed', handler as EventListener);

    if (!window.localStorage.getItem('tonti-lang')) {
      persistLocale(stored);
    }

    return () => window.removeEventListener('tonti:lang-changed', handler as EventListener);
  }, []);

  return <ToastProvider>{children}</ToastProvider>;
}
