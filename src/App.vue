<script setup lang="ts">
import { computed, watch } from 'vue';
import { useRoute, RouterLink, RouterView } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useStorage } from '@vueuse/core';

import { persistLocale } from '@/i18n';
import { useRtl } from '@/composables/useRtl';

const { t, locale } = useI18n();
const route = useRoute();

const storedLocale = useStorage('tonti:locale', locale.value as string);

watch(
  storedLocale,
  (value) => {
    locale.value = value;
    persistLocale(value);
  },
  { immediate: true },
);

const navigation = computed(() => [
  { name: t('nav.home'), to: '/' },
  { name: t('nav.create'), to: '/daret/creer' },
  { name: t('nav.join'), to: '/daret/rejoindre' },
]);

const { isRtl } = useRtl(locale);

function toggleLocale() {
  storedLocale.value = storedLocale.value === 'fr' ? 'ar' : 'fr';
}
</script>

<template>
  <div :class="['min-h-screen bg-background text-white', isRtl ? 'font-arabic' : 'font-sans']">
    <a
      href="#main"
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-xl focus:bg-primary focus:px-4 focus:py-2 focus:text-background"
    >
      Aller au contenu
    </a>
    <header class="border-b border-white/10 bg-background/80 backdrop-blur supports-backdrop:bg-background/60">
      <div class="container-responsive flex flex-wrap items-center justify-between gap-4 py-4">
        <RouterLink to="/" class="text-xl font-semibold">{{ t('app.name') }}</RouterLink>
        <nav class="flex flex-wrap items-center gap-3">
          <RouterLink
            v-for="item in navigation"
            :key="item.to"
            :to="item.to"
            class="rounded-full px-3 py-1 text-sm font-medium text-white/70 transition hover:text-white"
            :class="route.path === item.to ? 'bg-white/10 text-white' : ''"
          >
            {{ item.name }}
          </RouterLink>
          <button type="button" class="rounded-full border border-white/20 px-3 py-1 text-sm" @click="toggleLocale">
            {{ t('app.language') }}: {{ locale }}
          </button>
        </nav>
      </div>
    </header>
    <main id="main" class="container-responsive py-10">
      <RouterView />
    </main>
    <footer class="border-t border-white/10 bg-background/80">
      <div class="container-responsive py-6 text-sm text-white/60">© {{ new Date().getFullYear() }} · {{ t('app.name') }}</div>
    </footer>
  </div>
</template>
