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

function mark(membreId: string, statut: PaiementStatut) {
  emit('update', { membreId, statut });
}
</script>

<template>
  <div class="space-y-3">
    <div
      v-for="membre in props.membres"
      :key="membre.id"
      class="grid gap-2 rounded-xl border border-white/10 bg-white/5 p-4 sm:grid-cols-[1fr_auto] sm:items-center"
    >
      <div>
        <p class="font-semibold">{{ membre.nom }}</p>
        <p v-if="membre.contact" class="text-xs text-white/60">{{ membre.contact }}</p>
        <PaymentStatusBadge
          class="mt-2"
          :statut="paymentMap.get(membre.id)?.statut ?? 'a_payer'"
        />
      </div>
      <div class="flex flex-wrap items-center gap-2 justify-start sm:justify-end">
        <BaseButton
          type="button"
          size="sm"
          variant="secondary"
          :disabled="props.disabled"
          @click="mark(membre.id, 'paye')"
        >
          {{ t('actions.markPaid') }}
        </BaseButton>
        <BaseButton
          type="button"
          size="sm"
          variant="ghost"
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
