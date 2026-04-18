<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { Devise, Membre } from '@/types';
import { useCurrency } from '@/composables/useCurrency';

const props = defineProps<{
  roundIndex: number;
  totalRounds: number;
  receveur?: Membre;
  total: number;
  devise: Devise;
  daysRemaining: number;
}>();

const { t } = useI18n();
const { formatBoth, toggleDevise } = useCurrency(props.devise);

const daysLabel = computed(() =>
  props.daysRemaining === 0
    ? t('dashboard.today')
    : t('dashboard.daysRemaining', { count: props.daysRemaining })
);

const progress = computed(() =>
  props.totalRounds > 0 ? Math.round((props.roundIndex / props.totalRounds) * 100) : 0,
);

const urgency = computed(() => {
  if (props.daysRemaining <= 0) return 'text-dangerSoft';
  if (props.daysRemaining <= 3) return 'text-warningSoft';
  return 'text-successSoft';
});

const initials = computed(() => {
  if (!props.receveur?.nom) return '?';
  return props.receveur.nom
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('');
});
</script>

<template>
  <header class="card relative overflow-hidden">
    <div
      class="pointer-events-none absolute -top-20 right-0 h-48 w-48 rounded-full bg-primary/20 blur-3xl"
      aria-hidden="true"
    />
    <div class="relative space-y-6">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <div
            class="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/15 text-xl font-bold text-primary ring-1 ring-inset ring-primary/30"
            aria-hidden="true"
          >
            {{ initials }}
          </div>
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-white/60">
              {{ t('dashboard.round', { current: props.roundIndex, total: props.totalRounds }) }}
            </p>
            <h2 class="mt-0.5 text-2xl font-bold tracking-tight text-white sm:text-3xl">
              {{ props.receveur?.nom ?? '—' }}
            </h2>
          </div>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-semibold text-white/80 transition-colors hover:border-primary/40 hover:bg-primary/10 hover:text-primary"
          @click="toggleDevise"
        >
          <svg class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path d="M4 6a2 2 0 012-2h8a2 2 0 012 2v1H4V6z" />
            <path fill-rule="evenodd" d="M16 9H4v5a2 2 0 002 2h8a2 2 0 002-2V9zM6 12a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd" />
          </svg>
          {{ t('actions.toggleCurrency') }}
        </button>
      </div>

      <div class="grid gap-4 sm:grid-cols-3">
        <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
          <p class="text-[11px] font-semibold uppercase tracking-wider text-white/50">
            {{ t('dashboard.receiver') }}
          </p>
          <p class="mt-1 text-lg font-bold text-white">{{ props.receveur?.nom ?? '—' }}</p>
        </div>
        <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
          <p class="text-[11px] font-semibold uppercase tracking-wider text-white/50">
            {{ t('dashboard.total') }}
          </p>
          <p class="mt-1 text-lg font-bold text-primary">{{ formatBoth(props.total, props.devise) }}</p>
        </div>
        <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
          <p class="text-[11px] font-semibold uppercase tracking-wider text-white/50">
            {{ t('dashboard.daysRemainingLabel') }}
          </p>
          <p class="mt-1 flex items-center gap-2 text-lg font-bold" :class="urgency">
            <span class="relative flex h-2 w-2">
              <span
                v-if="props.daysRemaining <= 3"
                class="absolute inline-flex h-full w-full animate-ping rounded-full opacity-70"
                :class="props.daysRemaining <= 0 ? 'bg-danger' : 'bg-warning'"
              />
              <span class="relative inline-flex h-2 w-2 rounded-full bg-current" />
            </span>
            {{ daysLabel }}
          </p>
        </div>
      </div>

      <div>
        <div class="flex items-center justify-between text-xs font-semibold text-white/60">
          <span>{{ t('dashboard.round', { current: props.roundIndex, total: props.totalRounds }) }}</span>
          <span>{{ progress }}%</span>
        </div>
        <div class="mt-2 h-2 w-full overflow-hidden rounded-full bg-white/5">
          <div
            class="h-full rounded-full bg-gradient-to-r from-primary via-primarySoft to-primary transition-all duration-500"
            :style="{ width: `${progress}%` }"
          />
        </div>
      </div>
    </div>
  </header>
</template>
