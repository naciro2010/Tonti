import { createApp, watch } from 'vue';
import App from './App.vue';
import { router } from './router';
import { i18n } from './i18n';
import './styles/tailwind.css';

const app = createApp(App);

app.use(router);
app.use(i18n);

watch(
  () => i18n.global.locale.value,
  (locale) => {
    document.documentElement.lang = locale;
    document.documentElement.dir = locale === 'ar' ? 'rtl' : 'ltr';
    document.body.classList.toggle('font-arabic', locale === 'ar');
  },
  { immediate: true }
);

app.mount('#app');
