<script setup lang="ts">
import GlassCard from '../components/ui/GlassCard.vue';
import BaseButton from '../components/ui/BaseButton.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <section class="mx-auto max-w-3xl space-y-8 px-4 py-16">
    <header class="space-y-4 text-center">
      <h1 class="text-3xl font-semibold">{{ t('help.title') }}</h1>
      <p class="text-white/70">{{ t('help.intro') }}</p>
    </header>

    <GlassCard class="space-y-4 text-sm text-white/70">
      <p><strong>Email :</strong> {{ t('help.contactEmail') }}</p>
      <p><strong>Téléphone :</strong> {{ t('help.contactPhone') }}</p>
      <p>
        Notre équipe conformité vérifie chaque campagne avant publication et vous accompagne pour préparer les pièces
        administratives.
      </p>
      <div class="flex flex-wrap gap-3">
        <BaseButton variant="outline" type="button">Prendre rendez-vous</BaseButton>
        <BaseButton variant="ghost" type="button">Consulter le centre d’aide</BaseButton>
      </div>
    </GlassCard>
  </section>
</template>
