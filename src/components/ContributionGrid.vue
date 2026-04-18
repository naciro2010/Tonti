<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { Membre, Paiement, PaiementStatut } from '@/types';

import BaseButton from './BaseButton.vue';
import PaymentStatusBadge from './PaymentStatusBadge.vue';

const props = defineProps<{
  membres: Membre[];
  paiements: Paiement[];
  roundIndex: number;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (event: 'update', payload: { membreId: string; statut: PaiementStatut }): void;
}>();

const { t } = useI18n();

const paymentMap = computed(() => {
  const map = new Map<string, Paiement>();
  props.paiements
    .filter((paiement) => paiement.round === props.roundIndex)
    .forEach((paiement) => {
      map.set(paiement.membreId, paiement);
    });
  return map;
});

function statutOf(membreId: string): PaiementStatut {
  return paymentMap.get(membreId)?.statut ?? 'a_payer';
}

function initials(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('');
}

function mark(membreId: string, statut: PaiementStatut) {
  emit('update', { membreId, statut });
}
</script>

<template>
  <div class="space-y-3">
    <div
      v-for="membre in props.membres"
      :key="membre.id"
      class="group grid gap-3 rounded-2xl border border-white/10 bg-surface/40 p-4 transition-colors hover:border-white/20 hover:bg-surface/60 sm:grid-cols-[auto_1fr_auto] sm:items-center"
    >
      <div
        class="hidden h-10 w-10 flex-shrink-0 items-center justify-center rounded-full bg-primary/15 text-sm font-bold text-primary ring-1 ring-inset ring-primary/25 sm:flex"
        aria-hidden="true"
      >
        {{ initials(membre.nom) }}
      </div>
      <div class="space-y-1.5">
        <p class="font-semibold leading-tight text-white">{{ membre.nom }}</p>
        <p v-if="membre.contact" class="text-xs text-white/55">{{ membre.contact }}</p>
        <PaymentStatusBadge :statut="statutOf(membre.id)" />
      </div>
      <div class="flex flex-wrap items-center gap-2 justify-start sm:justify-end">
        <BaseButton
          type="button"
          size="sm"
          :variant="statutOf(membre.id) === 'paye' ? 'primary' : 'secondary'"
          :disabled="props.disabled"
          @click="mark(membre.id, 'paye')"
        >
          {{ t('actions.markPaid') }}
        </BaseButton>
        <BaseButton
          type="button"
          size="sm"
          :variant="statutOf(membre.id) === 'retard' ? 'danger' : 'ghost'"
          :disabled="props.disabled"
          @click="mark(membre.id, 'retard')"
        >
          {{ t('actions.markLate') }}
        </BaseButton>
        <BaseButton
          type="button"
          size="sm"
          variant="ghost"
          :disabled="props.disabled"
          @click="mark(membre.id, 'a_payer')"
        >
          {{ t('actions.markPending') }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>
