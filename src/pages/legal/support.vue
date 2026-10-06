<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import { LEGAL } from '@/config/legal';

const { t } = useI18n();
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <header>
      <h1 class="text-2xl font-bold sm:text-3xl">{{ t('legal.support') }}</h1>
      <p class="mt-1 text-sm text-white/60">{{ t('support.subtitle') }}</p>
    </header>

    <section class="card space-y-2">
      <h2 class="text-lg">{{ t('support.contactTitle') }}</h2>
      <p class="text-sm text-white/70">{{ t('support.contactText') }}</p>
      <a :href="`mailto:${LEGAL.supportEmail}`" class="text-lg font-semibold">{{ LEGAL.supportEmail }}</a>
    </section>

    <section class="card space-y-4">
      <h2 class="text-lg">{{ t('support.faqTitle') }}</h2>
      <details
        v-for="key in ['pay', 'security', 'order', 'late', 'delete']"
        :key="key"
        class="group rounded-xl bg-white/5 p-4"
      >
        <summary class="cursor-pointer list-none font-medium">{{ t(`support.faq.${key}.q`) }}</summary>
        <p class="mt-2 text-sm text-white/70">{{ t(`support.faq.${key}.a`) }}</p>
      </details>
    </section>
  </div>
</template>
