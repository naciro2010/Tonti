import { createRouter, createWebHistory } from 'vue-router';

const HomePage = () => import('../pages/HomePage.vue');
const HowItWorksPage = () => import('../pages/HowItWorksPage.vue');
const FaqPage = () => import('../pages/FaqPage.vue');
const HelpPage = () => import('../pages/HelpPage.vue');
const TermsPage = () => import('../pages/TermsPage.vue');
const PrivacyPage = () => import('../pages/PrivacyPage.vue');
const CreatePage = () => import('../pages/CreateCagnottePage.vue');
const CagnottePage = () => import('../pages/CagnottePage.vue');
const LoginPage = () => import('../pages/LoginPage.vue');

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomePage, meta: { title: 'Accueil' } },
    { path: '/comment-ca-marche', component: HowItWorksPage, meta: { title: 'Comment ça marche ?' } },
    { path: '/faq', component: FaqPage, meta: { title: 'FAQ' } },
    { path: '/aide', component: HelpPage, meta: { title: 'Aide' } },
    { path: '/cgu', component: TermsPage, meta: { title: 'Conditions générales' } },
    { path: '/confidentialite', component: PrivacyPage, meta: { title: 'Politique de confidentialité' } },
    { path: '/creer', component: CreatePage, meta: { title: 'Créer une cagnotte' } },
    { path: '/cagnotte/:slug', component: CagnottePage, meta: { title: 'Cagnotte' } },
    { path: '/auth/login', component: LoginPage, meta: { title: 'Connexion' } },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

router.afterEach((to) => {
  const baseTitle = 'Tonti · La cagnotte digitale des Marocains';
  document.title = to.meta.title ? `${to.meta.title as string} · ${baseTitle}` : baseTitle;
});
