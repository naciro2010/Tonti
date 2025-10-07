import { ViteSSG } from 'vite-ssg';

import App from './App.vue';
import { createI18nInstance } from './i18n';
import routes from './router';
import { usePlausible } from './composables/usePlausible';

import './styles/tailwind.css';

export const createApp = ViteSSG(App, { routes, base: import.meta.env.BASE_URL }, ({ app, router, isClient }) => {
  const { i18n } = createI18nInstance();
  app.use(i18n);

  if (isClient) {
    usePlausible(router);
  }
});
