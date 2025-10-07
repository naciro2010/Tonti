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
</script>

<template>
  <header class="card space-y-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <p class="text-sm uppercase tracking-wide text-white/60">{{ t('dashboard.round', { current: props.roundIndex, total: props.totalRounds }) }}</p>
        <h2 class="text-2xl font-semibold">{{ props.receveur?.nom }}</h2>
      </div>
      <button
        type="button"
        class="text-sm text-primary underline-offset-4 hover:underline"
        @click="toggleDevise"
      >
        {{ t('actions.toggleCurrency') }}
      </button>
    </div>
    <div class="grid gap-4 sm:grid-cols-3">
      <div>
        <p class="text-xs uppercase tracking-wide text-white/50">{{ t('dashboard.receiver') }}</p>
        <p class="text-lg font-semibold">{{ props.receveur?.nom ?? '—' }}</p>
      </div>
      <div>
        <p class="text-xs uppercase tracking-wide text-white/50">{{ t('dashboard.total') }}</p>
        <p class="text-lg font-semibold">{{ formatBoth(props.total, props.devise) }}</p>
      </div>
      <div>
        <p class="text-xs uppercase tracking-wide text-white/50">{{ t('dashboard.daysRemainingLabel') }}</p>
        <p class="text-lg font-semibold">{{ daysLabel }}</p>
      </div>
    </div>
  </header>
</template>
