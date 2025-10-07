<script setup lang="ts">
import GlassCard from '../components/ui/GlassCard.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <section class="mx-auto max-w-4xl space-y-8 px-4 py-16">
    <header class="space-y-3">
      <h1 class="text-3xl font-semibold">{{ t('privacy.title') }}</h1>
      <p class="text-white/70">{{ t('privacy.intro') }}</p>
    </header>
    <GlassCard class="space-y-3 text-sm text-white/70">
      <p>
        Nous stockons uniquement des données non sensibles dans le navigateur (localStorage) pour l’auto-sauvegarde des
        brouillons. Aucun paiement réel n’est traité dans cette démo et aucune donnée bancaire n’est collectée.
      </p>
      <p>
        Pour l’analytics, nous utilisons Plausible dans un mode respectueux de la vie privée, sans cookies ni collecte
        personnelle.
      </p>
    </GlassCard>
  </section>
</template>
