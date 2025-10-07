<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import BaseButton from '@/components/BaseButton.vue';
import { useDaretStore } from '@/composables/useDaretStore';

const { t } = useI18n();
const store = useDaretStore();

const featured = computed(() => store.allDarets.value.slice(0, 3));
</script>

<template>
  <section class="space-y-12">
    <header class="grid gap-6 rounded-3xl bg-gradient-to-br from-surface/80 to-background p-8 text-center shadow-xl">
      <h1 class="text-4xl font-bold">{{ t('app.tagline') }}</h1>
      <p class="text-lg text-white/70">
        Organisez, suivez et partagez vos cagnottes rotatives sans serveur : autosave, i18n FR/AR et export calendrier.
      </p>
      <div class="flex flex-wrap justify-center gap-4">
        <RouterLink to="/daret/creer">
          <BaseButton size="lg">{{ t('actions.create') }}</BaseButton>
        </RouterLink>
        <RouterLink to="/daret/rejoindre">
          <BaseButton size="lg" variant="secondary">{{ t('actions.join') }}</BaseButton>
        </RouterLink>
      </div>
    </header>

    <section class="grid gap-6 lg:grid-cols-3">
      <article class="card">
        <h2 class="text-xl font-semibold">100 % statique</h2>
        <p class="mt-2 text-sm text-white/70">
          Build via Vite + vite-plugin-ssg, sans serveur : données locales chiffrées par votre navigateur, pas de fuites.
        </p>
      </article>
      <article class="card">
        <h2 class="text-xl font-semibold">Accessibilité mobile</h2>
        <p class="mt-2 text-sm text-white/70">
          Interface mobile-first conforme WCAG AA, composants Headless UI, focus et navigation clavier complets.
        </p>
      </article>
      <article class="card">
        <h2 class="text-xl font-semibold">Internationalisation</h2>
        <p class="mt-2 text-sm text-white/70">
          Français / العربية avec RTL intégral, traduction des formulaires, badges et toasts.
        </p>
      </article>
    </section>

    <section class="space-y-4">
      <div class="flex items-center justify-between">
        <h2 class="text-2xl font-semibold">Darets de démonstration</h2>
        <RouterLink to="/daret/creer" class="text-sm text-primary hover:underline">{{ t('actions.create') }}</RouterLink>
      </div>
      <div class="grid gap-4 md:grid-cols-2">
        <article v-for="daret in featured" :key="daret.id" class="card space-y-3">
          <h3 class="text-xl font-semibold">{{ daret.nom }}</h3>
          <p class="text-sm text-white/70">{{ daret.description }}</p>
          <p class="text-sm text-white/60">
            {{ daret.taille }} membres · {{ daret.devise }} · {{ daret.etat }}
          </p>
          <RouterLink :to="`/daret/${daret.id}`" class="text-sm text-primary hover:underline">
            Voir le tableau de bord
          </RouterLink>
        </article>
      </div>
    </section>
  </section>
</template>
