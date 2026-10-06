<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import { formatCurrency } from '@/composables/useCurrency';
import { daysUntil, formatDate } from '@/composables/useDates';
import type { Currency, RoundResponse } from '@/services/api';

const props = defineProps<{
  round: RoundResponse;
  totalRounds: number;
  /** Montant total que recevra le bénéficiaire (cotisation × payeurs attendus). */
  pot: number;
  currency: Currency;
  expectedPayers: number;
}>();

const { t, locale } = useI18n();

const daysLeft = computed(() => daysUntil(props.round.dateFin));
const collected = computed(() =>
  props.expectedPayers > 0 ? props.round.paidCount / props.expectedPayers : 0,
);
const receveurName = computed(() => `${props.round.receveur.firstName} ${props.round.receveur.lastName}`);
const initials = computed(() =>
  `${props.round.receveur.firstName.charAt(0)}${props.round.receveur.lastName.charAt(0)}`.toUpperCase(),
);
const urgency = computed(() =>
  daysLeft.value <= 0 ? 'text-dangerSoft' : daysLeft.value <= 3 ? 'text-warningSoft' : 'text-successSoft',
);
</script>

<template>
  <section class="card relative overflow-hidden">
    <div
      class="pointer-events-none absolute -right-16 -top-16 h-48 w-48 rounded-full bg-primary/10 blur-3xl"
    />
    <div class="relative space-y-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <p class="section-title">
          {{ t('dashboard.roundOf', { current: round.numero, total: totalRounds }) }}
        </p>
        <p class="text-sm font-medium" :class="urgency">
          {{
            daysLeft === 0 ? t('dashboard.dueToday') : t('dashboard.daysLeft', { count: daysLeft }, daysLeft)
          }}
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-4">
        <span
          class="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-2xl bg-primary/15 text-lg font-bold text-primary ring-1 ring-inset ring-primary/30"
          aria-hidden="true"
        >
          {{ initials }}
        </span>
        <div class="min-w-0 flex-1">
          <p class="text-sm text-white/60">{{ t('dashboard.beneficiary') }}</p>
          <p class="truncate text-xl font-semibold">{{ receveurName }}</p>
          <p class="text-xs text-white/50">
            <span class="whitespace-nowrap">{{ formatDate(round.dateDebut, locale) }}</span>
            →
            <span class="whitespace-nowrap">{{ formatDate(round.dateFin, locale) }}</span>
          </p>
        </div>
        <div
          class="flex w-full items-baseline justify-between gap-3 rounded-xl bg-white/5 px-4 py-3 sm:w-auto sm:flex-col sm:items-end sm:bg-transparent sm:p-0"
        >
          <p class="text-sm text-white/60">{{ t('dashboard.pot') }}</p>
          <p class="text-2xl font-bold text-primary">{{ formatCurrency(pot, currency) }}</p>
        </div>
      </div>

      <div>
        <div class="mb-1.5 flex justify-between text-xs text-white/60">
          <span>{{ t('dashboard.contributionsCollected') }}</span>
          <span>{{ round.paidCount }} / {{ expectedPayers }}</span>
        </div>
        <div
          class="h-2 overflow-hidden rounded-full bg-white/5"
          role="progressbar"
          :aria-valuenow="round.paidCount"
          :aria-valuemax="expectedPayers"
          aria-valuemin="0"
        >
          <div
            class="h-full rounded-full bg-gradient-to-r from-primary to-primarySoft transition-all duration-500"
            :style="{ width: `${Math.round(collected * 100)}%` }"
          />
        </div>
      </div>
    </div>
  </section>
</template>
