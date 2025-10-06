<script setup lang="ts">
import GlassCard from '../components/ui/GlassCard.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <section class="mx-auto max-w-4xl space-y-8 px-4 py-16">
    <header class="space-y-3">
      <h1 class="text-3xl font-semibold">{{ t('terms.title') }}</h1>
      <p class="text-white/70">{{ t('terms.intro') }}</p>
    </header>
    <GlassCard class="space-y-3 text-sm text-white/70">
      <p>
        L’utilisation de Tonti implique l’acceptation de nos règles de conformité, de notre politique KYC et du respect des
        plafonds réglementaires fixés par Bank Al-Maghrib. Chaque organisateur s’engage à fournir des informations exactes et
        à utiliser les fonds pour l’objectif déclaré.
      </p>
      <p>
        La plateforme conserve des journaux d’audit statiques et prépare l’intégration future d’une API de paiement certifiée.
      </p>
    </GlassCard>
  </section>
</template>
