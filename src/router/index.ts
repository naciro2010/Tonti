import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

import { useAuthStore } from '@/composables/useAuthStore';

declare module 'vue-router' {
  interface RouteMeta {
    /** Page réservée aux utilisateurs connectés. */
    requiresAuth?: boolean;
    /** Page réservée aux visiteurs (connexion, inscription). */
    guest?: boolean;
  }
}

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: () => import('@/pages/index.vue') },
  { path: '/login', name: 'login', component: () => import('@/pages/login.vue'), meta: { guest: true } },
  {
    path: '/inscription',
    name: 'register',
    component: () => import('@/pages/inscription.vue'),
    meta: { guest: true },
  },
  {
    path: '/mes-darets',
    name: 'my-darets',
    component: () => import('@/pages/mes-darets.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/daret/creer',
    name: 'daret-create',
    component: () => import('@/pages/daret/creer.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/daret/rejoindre',
    name: 'daret-join',
    component: () => import('@/pages/daret/rejoindre.vue'),
  },
  {
    path: '/daret/:id',
    name: 'daret-detail',
    component: () => import('@/pages/daret/[id].vue'),
    props: true,
    meta: { requiresAuth: true },
  },
  {
    path: '/paiement/resultat',
    name: 'payment-result',
    component: () => import('@/pages/paiement/resultat.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('@/pages/notifications.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/compte',
    name: 'account',
    component: () => import('@/pages/compte.vue'),
    meta: { requiresAuth: true },
  },
  { path: '/confidentialite', name: 'privacy', component: () => import('@/pages/legal/confidentialite.vue') },
  { path: '/conditions', name: 'terms', component: () => import('@/pages/legal/conditions.vue') },
  { path: '/support', name: 'support', component: () => import('@/pages/legal/support.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' },
];

export function createAppRouter() {
  const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior: (_to, _from, saved) => saved ?? { top: 0 },
  });

  router.beforeEach((to) => {
    const { isAuthenticated } = useAuthStore();
    if (to.meta.requiresAuth && !isAuthenticated.value) {
      return { name: 'login', query: { redirect: to.fullPath } };
    }
    if (to.meta.guest && isAuthenticated.value) {
      return { name: 'my-darets' };
    }
    return true;
  });

  return router;
}
