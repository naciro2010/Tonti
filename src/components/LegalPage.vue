<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import { formatDate } from '@/composables/useDates';
import { LEGAL } from '@/config/legal';

defineProps<{ title: string }>();

const { t, locale } = useI18n();
</script>

<template>
  <article class="legal mx-auto max-w-3xl space-y-6">
    <header>
      <h1 class="text-2xl font-bold sm:text-3xl">{{ title }}</h1>
      <p class="mt-1 text-sm text-white/50">
        {{ t('legal.lastUpdated', { date: formatDate(LEGAL.lastUpdated, locale) }) }}
      </p>
      <p v-if="locale !== 'fr'" class="mt-3 rounded-xl bg-white/5 p-3 text-sm text-white/60">
        {{ t('legal.frenchOnly') }}
      </p>
    </header>
    <div class="card space-y-5 text-sm leading-relaxed text-white/80" lang="fr" dir="ltr">
      <slot />
    </div>
  </article>
</template>

<style scoped>
.legal :deep(h2) {
  @apply mt-2 text-lg font-semibold text-white;
}
.legal :deep(ul) {
  @apply list-disc space-y-1 ps-5;
}
</style>
