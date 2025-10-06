<script setup lang="ts">
import CreateCagnotteForm from '../components/forms/CreateCagnotteForm.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <section class="mx-auto max-w-5xl space-y-8 px-4 py-16">
    <header class="space-y-3">
      <h1 id="create-title" class="text-3xl font-semibold">{{ t('create.heading') }}</h1>
      <p class="text-white/70">{{ t('create.intro') }}</p>
    </header>
    <CreateCagnotteForm />
  </section>
</template>
