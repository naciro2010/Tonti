import type { Router } from 'vue-router';

declare global {
  interface Window {
    plausible?: (event: string, options?: Record<string, unknown>) => void;
  }
}

export function usePlausible(router: Router) {
  router.afterEach(() => {
    if (typeof window !== 'undefined' && typeof window.plausible === 'function') {
      window.plausible('pageview');
    }
  });
}
