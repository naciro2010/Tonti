<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { PaiementStatut } from '@/types';

const props = defineProps<{
  statut: PaiementStatut;
}>();

const { t } = useI18n();

const variant = computed(() => {
  switch (props.statut) {
    case 'paye':
      return 'bg-success/20 text-success';
    case 'retard':
      return 'bg-danger/20 text-danger';
    default:
      return 'bg-warning/20 text-warning';
  }
});
</script>

<template>
  <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold" :class="variant">
    {{ t(`statuses.${props.statut}`) }}
  </span>
</template>
