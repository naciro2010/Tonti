import { createApp } from 'vue';

import App from './App.vue';
import { useAuthStore } from './composables/useAuthStore';
import { createI18nInstance } from './i18n';
import { createAppRouter } from './router';
import { enableWebAnalytics } from './services/analytics';
import { setUnauthorizedHandler } from './services/api';
import { setupNativeShell } from './services/platform';
import { tokenStore } from './services/storage';

import '@fontsource-variable/inter';
import '@fontsource/noto-kufi-arabic/400.css';
import '@fontsource/noto-kufi-arabic/600.css';
import './styles/tailwind.css';

async function bootstrap() {
  // Les jetons doivent être chargés (Keychain sur iOS) avant le premier rendu et la première requête
  await tokenStore.init();

  const app = createApp(App);
  const { i18n } = createI18nInstance();
  const router = createAppRouter();

  setUnauthorizedHandler(() => {
    useAuthStore().invalidate();
    const current = router.currentRoute.value;
    if (current.meta.requiresAuth) {
      void router.replace({ name: 'login', query: { redirect: current.fullPath } });
    }
  });

  app.use(i18n);
  app.use(router);

  enableWebAnalytics(router);

  await router.isReady();
  app.mount('#app');

  await setupNativeShell(router);
}

void bootstrap();
