import type { Router } from 'vue-router';

import { isNative } from './platform';

declare global {
  interface Window {
    plausible?: (event: string, options?: Record<string, unknown>) => void;
  }
}

/**
 * Mesure d'audience Plausible (sans cookie, sans donnée personnelle), uniquement sur le web
 * et seulement si VITE_PLAUSIBLE_DOMAIN est défini. Jamais chargée dans l'application native.
 */
export function enableWebAnalytics(router: Router): void {
  const domain = import.meta.env.VITE_PLAUSIBLE_DOMAIN;
  if (isNative || !domain) return;

  const script = document.createElement('script');
  script.defer = true;
  script.dataset.domain = domain;
  script.dataset.manual = 'true';
  script.src = 'https://plausible.io/js/script.manual.js';
  document.head.appendChild(script);

  router.afterEach(() => window.plausible?.('pageview'));
}
