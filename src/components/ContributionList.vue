<script setup lang="ts">
import { useI18n } from 'vue-i18n';

import type { MembreResponse, RoundResponse } from '@/services/api';

import StatusBadge from './StatusBadge.vue';

defineProps<{
  membres: MembreResponse[];
  round: RoundResponse;
  currentUserId?: string;
}>();

const { t } = useI18n();
</script>

<template>
  <ul class="divide-y divide-white/5 overflow-hidden rounded-2xl border border-white/10 bg-surface/40">
    <li v-for="membre in membres" :key="membre.id" class="flex items-center gap-3 px-4 py-3">
      <span
        class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-white/10 text-xs font-bold"
        aria-hidden="true"
      >
        {{ `${membre.firstName.charAt(0)}${membre.lastName.charAt(0)}`.toUpperCase() }}
      </span>
      <div class="min-w-0 flex-1">
        <p class="truncate font-medium">
          {{ membre.firstName }} {{ membre.lastName }}
          <span v-if="membre.userId === currentUserId" class="text-white/50">({{ t('common.you') }})</span>
        </p>
        <p v-if="membre.id === round.receveur.id" class="text-xs text-primary">
          {{ t('dashboard.beneficiary') }}
        </p>
      </div>
      <StatusBadge
        v-if="membre.id !== round.receveur.id"
        kind="payment"
        :status="round.paidUserIds.includes(membre.userId) ? 'SUCCEEDED' : 'DUE'"
      />
    </li>
  </ul>
</template>
