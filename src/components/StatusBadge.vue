<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { DaretStatus, PaymentStatus } from '@/services/api';

const props = defineProps<
  { kind: 'daret'; status: DaretStatus } | { kind: 'payment'; status: PaymentStatus | 'DUE' }
>();

const { t } = useI18n();

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'neutral';

const TONES: Record<string, Tone> = {
  RECRUTEMENT: 'info',
  VERROUILLEE: 'warning',
  ACTIVE: 'success',
  TERMINEE: 'neutral',
  ANNULEE: 'danger',
  SUCCEEDED: 'success',
  PENDING: 'warning',
  PROCESSING: 'warning',
  REQUIRES_ACTION: 'warning',
  DUE: 'warning',
  FAILED: 'danger',
  CANCELLED: 'neutral',
  REFUNDED: 'neutral',
  PARTIALLY_REFUNDED: 'neutral',
};

const CLASSES: Record<Tone, { badge: string; dot: string }> = {
  success: { badge: 'bg-success/15 text-successSoft ring-success/30', dot: 'bg-success' },
  warning: { badge: 'bg-warning/15 text-warningSoft ring-warning/30', dot: 'bg-warning' },
  danger: { badge: 'bg-danger/15 text-dangerSoft ring-danger/30', dot: 'bg-danger' },
  info: { badge: 'bg-blue-500/15 text-blue-300 ring-blue-500/30', dot: 'bg-blue-400' },
  neutral: { badge: 'bg-white/10 text-white/60 ring-white/10', dot: 'bg-white/40' },
};

const style = computed(() => CLASSES[TONES[props.status] ?? 'neutral']);
const label = computed(() =>
  props.kind === 'daret' ? t(`daretStatus.${props.status}`) : t(`paymentStatus.${props.status}`),
);
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 whitespace-nowrap rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset"
    :class="style.badge"
  >
    <span class="badge-dot" :class="style.dot" aria-hidden="true" />
    {{ label }}
  </span>
</template>
