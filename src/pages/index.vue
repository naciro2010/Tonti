<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import BaseButton from '@/components/BaseButton.vue';
import { useAuthStore } from '@/composables/useAuthStore';

const { t } = useI18n();
const auth = useAuthStore();

const features = computed(() =>
  (['organize', 'pay', 'track'] as const).map((key) => ({
    key,
    title: t(`landing.features.${key}.title`),
    text: t(`landing.features.${key}.text`),
  })),
);

const steps = computed(() =>
  (['create', 'invite', 'start', 'pay'] as const).map((key) => t(`landing.steps.${key}`)),
);

const icons: Record<string, string> = {
  organize:
    'M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198v.001c0 .224-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0z',
  pay: 'M2.25 8.25h19.5M2.25 9h19.5m-16.5 5.25h6m-6 2.25h3m-3.75 3h15a2.25 2.25 0 002.25-2.25V6.75A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25v10.5A2.25 2.25 0 004.5 19.5z',
  track:
    'M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z',
};
</script>

<template>
  <div class="space-y-16 sm:space-y-24">
    <section class="mx-auto max-w-3xl pt-6 text-center sm:pt-12">
      <span class="chip mb-6">{{ t('landing.badge') }}</span>
      <h1 class="text-4xl font-bold leading-tight sm:text-5xl">
        {{ t('landing.title') }} <span class="gradient-text">{{ t('landing.titleHighlight') }}</span>
      </h1>
      <p class="mx-auto mt-5 max-w-xl text-base text-white/70 sm:text-lg">{{ t('landing.subtitle') }}</p>
      <div class="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
        <RouterLink :to="auth.isAuthenticated.value ? '/daret/creer' : '/inscription'" class="no-underline">
          <BaseButton size="lg" block>{{ t('landing.ctaPrimary') }}</BaseButton>
        </RouterLink>
        <RouterLink to="/daret/rejoindre" class="no-underline">
          <BaseButton size="lg" variant="secondary" block>{{ t('landing.ctaSecondary') }}</BaseButton>
        </RouterLink>
      </div>
      <p v-if="!auth.isAuthenticated.value" class="mt-4 text-sm text-white/60">
        {{ t('auth.hasAccount') }}
        <RouterLink to="/login" class="font-semibold no-underline">{{ t('auth.login') }}</RouterLink>
      </p>
    </section>

    <section class="grid gap-4 sm:grid-cols-3">
      <article v-for="feature in features" :key="feature.key" class="card">
        <div class="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-primary/15 text-primary">
          <svg
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.6"
            aria-hidden="true"
          >
            <path stroke-linecap="round" stroke-linejoin="round" :d="icons[feature.key]" />
          </svg>
        </div>
        <h2 class="text-lg">{{ feature.title }}</h2>
        <p class="mt-2 text-sm text-white/65">{{ feature.text }}</p>
      </article>
    </section>

    <section class="mx-auto max-w-3xl">
      <h2 class="text-center">{{ t('landing.howTitle') }}</h2>
      <ol class="mt-8 grid gap-3 sm:grid-cols-2">
        <li
          v-for="(step, index) in steps"
          :key="index"
          class="flex items-start gap-4 rounded-2xl bg-white/5 p-5"
        >
          <span
            class="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-primary text-sm font-bold text-background"
          >
            {{ index + 1 }}
          </span>
          <p class="text-white/80">{{ step }}</p>
        </li>
      </ol>
    </section>

    <section class="card mx-auto max-w-3xl text-center">
      <h2 class="text-xl">{{ t('landing.securityTitle') }}</h2>
      <p class="mt-3 text-sm text-white/65">{{ t('landing.securityText') }}</p>
    </section>
  </div>
</template>
