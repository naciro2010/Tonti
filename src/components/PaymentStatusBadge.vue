<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { PaiementStatut } from '@/types';

const props = defineProps<{
  statut: PaiementStatut;
}>();

const { t } = useI18n();

const config = computed(() => {
  switch (props.statut) {
    case 'paye':
      return {
        classes: 'bg-success/15 text-successSoft ring-1 ring-inset ring-success/30',
        dot: 'bg-success',
      };
    case 'retard':
      return {
        classes: 'bg-danger/15 text-dangerSoft ring-1 ring-inset ring-danger/30',
        dot: 'bg-danger',
      };
    default:
      return {
        classes: 'bg-warning/15 text-warningSoft ring-1 ring-inset ring-warning/30',
        dot: 'bg-warning',
      };
  }
});
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold"
    :class="config.classes"
  >
    <span class="badge-dot" :class="config.dot" aria-hidden="true" />
    {{ t(`statuses.${props.statut}`) }}
  </span>
</template>
