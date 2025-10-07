import { computed, watchEffect } from 'vue';

export function useRtl(localeRef: { value: string }) {
  const isRtl = computed(() => localeRef.value === 'ar');

  watchEffect(() => {
    if (typeof document === 'undefined') {
      return;
    }

    const direction = isRtl.value ? 'rtl' : 'ltr';
    document.documentElement.setAttribute('dir', direction);
    document.documentElement.classList.toggle('rtl', isRtl.value);
    document.documentElement.classList.toggle('ltr', !isRtl.value);
  });

  return {
    isRtl,
  };
}
