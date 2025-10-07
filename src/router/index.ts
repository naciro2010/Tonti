import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/pages/index.vue'),
  },
  {
    path: '/daret/creer',
    name: 'daret-create',
    component: () => import('@/pages/daret/creer.vue'),
    meta: { ssg: true },
  },
  {
    path: '/daret/rejoindre',
    name: 'daret-join',
    component: () => import('@/pages/daret/rejoindre.vue'),
    meta: { ssg: true },
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
