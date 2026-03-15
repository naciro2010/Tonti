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

    // Navigation guard for auth
    router.beforeEach((to, _from, next) => {
      const isAuthenticated = !!localStorage.getItem('tonti:accessToken');

      if (to.meta.requiresAuth && !isAuthenticated) {
        next({ path: '/login', query: { redirect: to.fullPath } });
      } else if (to.meta.guest && isAuthenticated) {
        next('/mes-darets');
      } else {
        next();
      }
    });
  }
});
