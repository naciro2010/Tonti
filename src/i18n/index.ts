import { watch } from 'vue';
import { createI18n } from 'vue-i18n';
import fr from './fr';
import ar from './ar';

const defaultLocale = typeof window !== 'undefined' ? localStorage.getItem('tonti_locale') ?? 'fr' : 'fr';

export const i18n = createI18n({
  legacy: false,
  locale: defaultLocale,
  fallbackLocale: 'fr',
  messages: {
    fr,
    ar,
  },
});

if (typeof window !== 'undefined') {
  watch(
    () => i18n.global.locale.value,
    (locale) => {
      localStorage.setItem('tonti_locale', locale);
    },
    { immediate: true }
  );
}
