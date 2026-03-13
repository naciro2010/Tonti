import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/pages/index.vue'),
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/login.vue'),
    meta: { guest: true },
  },
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
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

export default routes;
