<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import BaseButton from '@/components/BaseButton.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import { useAuthStore } from '@/composables/useAuthStore';
import { formatCurrency } from '@/composables/useCurrency';
import { daretApi, type DaretResponse } from '@/services/api';
import { errorMessage } from '@/utils/errors';

const { t } = useI18n();
const auth = useAuthStore();

const darets = ref<DaretResponse[]>([]);
const loading = ref(true);
const loadError = ref<string | null>(null);

async function load() {
  loading.value = true;
  loadError.value = null;
  try {
    darets.value = await daretApi.list();
  } catch (error) {
    loadError.value = errorMessage(error, t('darets.loadError'));
  } finally {
    loading.value = false;
  }
}

onMounted(load);

const sections = computed(() =>
  [
    {
      key: 'active',
      title: t('darets.sections.active'),
      items: darets.value.filter((d) => d.etat === 'ACTIVE' || d.etat === 'VERROUILLEE'),
    },
    {
      key: 'recruiting',
      title: t('darets.sections.recruiting'),
      items: darets.value.filter((d) => d.etat === 'RECRUTEMENT'),
    },
    {
      key: 'done',
      title: t('darets.sections.done'),
      items: darets.value.filter((d) => d.etat === 'TERMINEE' || d.etat === 'ANNULEE'),
    },
  ].filter((section) => section.items.length > 0),
);
</script>

<template>
  <div class="space-y-8">
    <header class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <p class="text-sm text-white/60">
          {{ t('darets.greeting', { name: auth.user.value?.firstName ?? '' }) }}
        </p>
        <h1 class="text-2xl font-bold sm:text-3xl">{{ t('darets.title') }}</h1>
      </div>
      <div class="flex gap-2">
        <RouterLink to="/daret/rejoindre" class="no-underline">
          <BaseButton variant="secondary">{{ t('nav.join') }}</BaseButton>
        </RouterLink>
        <RouterLink to="/daret/creer" class="hidden no-underline sm:block">
          <BaseButton>{{ t('nav.create') }}</BaseButton>
        </RouterLink>
      </div>
    </header>

    <div v-if="loading" class="grid gap-4 sm:grid-cols-2" aria-busy="true">
      <div v-for="i in 4" :key="i" class="skeleton h-36 rounded-2xl" />
    </div>

    <div v-else-if="loadError" class="card text-center">
      <p class="text-white/70">{{ loadError }}</p>
      <BaseButton class="mt-4" variant="secondary" @click="load">{{ t('common.retry') }}</BaseButton>
    </div>

    <div v-else-if="darets.length === 0" class="card flex flex-col items-center py-14 text-center">
      <div class="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/15 text-primary">
        <svg class="h-8 w-8" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
          <path d="M6 6h12v3H6zM8 11h8v3H8zM10 16h4v3h-4z" />
        </svg>
      </div>
      <h2 class="text-xl">{{ t('darets.emptyTitle') }}</h2>
      <p class="mt-2 max-w-sm text-sm text-white/60">{{ t('darets.emptyText') }}</p>
      <div class="mt-6 flex flex-wrap justify-center gap-3">
        <RouterLink to="/daret/creer" class="no-underline"
          ><BaseButton>{{ t('nav.create') }}</BaseButton></RouterLink
        >
        <RouterLink to="/daret/rejoindre" class="no-underline">
          <BaseButton variant="secondary">{{ t('nav.join') }}</BaseButton>
        </RouterLink>
      </div>
    </div>

    <section v-for="section in sections" v-else :key="section.key" class="space-y-3">
      <h2 class="section-title">{{ section.title }} · {{ section.items.length }}</h2>
      <div class="grid gap-4 sm:grid-cols-2">
        <RouterLink
          v-for="daret in section.items"
          :key="daret.id"
          :to="`/daret/${daret.id}`"
          class="card-interactive block text-white no-underline"
        >
          <div class="flex items-start justify-between gap-3">
            <h3 class="truncate">{{ daret.nom }}</h3>
            <StatusBadge kind="daret" :status="daret.etat" />
          </div>
          <p class="mt-3 text-2xl font-bold text-primary">
            {{ formatCurrency(daret.montantMensuel, daret.devise) }}
            <span class="text-sm font-normal text-white/50">/ {{ t('common.month') }}</span>
          </p>
          <div class="mt-3 flex flex-wrap gap-2 text-xs">
            <span class="chip">{{
              t('darets.members', { count: daret.membresCount, size: daret.taille })
            }}</span>
            <span v-if="daret.currentRound" class="chip">
              {{ t('dashboard.roundOf', { current: daret.currentRound, total: daret.taille }) }}
            </span>
            <span v-if="daret.createurId === auth.user.value?.id" class="chip">{{
              t('darets.organizer')
            }}</span>
          </div>
        </RouterLink>
      </div>
    </section>
  </div>
</template>
