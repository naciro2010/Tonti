<script setup lang="ts">
import GlassCard from '../components/ui/GlassCard.vue';
import { useI18n } from 'vue-i18n';

const { t, tm } = useI18n();
</script>

<template>
  <section class="mx-auto max-w-4xl space-y-12 px-4 py-16">
    <header class="space-y-4 text-center">
      <h1 class="text-3xl font-semibold">{{ t('how.title') }}</h1>
      <p class="text-white/70">Tonti accompagne les organisateurs de bout en bout avec une expérience fluide.</p>
    </header>

    <div class="grid gap-6 md:grid-cols-2">
      <GlassCard v-for="step in tm('how.steps')" :key="step.title" class="space-y-3">
        <h2 class="text-xl font-semibold">{{ step.title }}</h2>
        <p class="text-sm text-white/60">{{ step.description }}</p>
      </GlassCard>
    </div>

    <GlassCard class="space-y-3">
      <h2 class="text-xl font-semibold">Déploiement GitHub Pages</h2>
      <p class="text-sm text-white/60">
        Ce front-end statique est prêt pour un déploiement automatique via GitHub Actions et compatible avec GitHub Pages sans
        backend.
      </p>
    </GlassCard>
  </section>
</template>
