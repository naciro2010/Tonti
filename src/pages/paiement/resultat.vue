<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import { formatCurrency } from '@/composables/useCurrency';
import { useNotifications } from '@/composables/useNotifications';
import { paymentApi, type PaymentResponse } from '@/services/api';

/**
 * Résultat d'un paiement. Le retour du PSP ne fait pas foi : on interroge l'API jusqu'à ce
 * que le webhook ait mis à jour le statut (quelques secondes en général).
 */
const { t } = useI18n();
const route = useRoute();
const notifications = useNotifications();

const payment = ref<PaymentResponse | null>(null);
const failed = ref(false);
const timedOut = ref(false);

const POLL_INTERVAL_MS = 2000;
const MAX_ATTEMPTS = 30;
let attempts = 0;
let timer: ReturnType<typeof setTimeout> | null = null;

const paymentId = computed(() => (typeof route.query.paymentId === 'string' ? route.query.paymentId : ''));

const state = computed<'success' | 'pending' | 'failed' | 'cancelled'>(() => {
  switch (payment.value?.status) {
    case 'SUCCEEDED':
      return 'success';
    case 'FAILED':
      return 'failed';
    case 'CANCELLED':
    case 'REFUNDED':
    case 'PARTIALLY_REFUNDED':
      return 'cancelled';
    default:
      return 'pending';
  }
});

async function poll() {
  if (!paymentId.value) {
    failed.value = true;
    return;
  }
  try {
    payment.value = await paymentApi.get(paymentId.value);
  } catch {
    // Erreur transitoire (réseau) : on réessaie
  }
  attempts += 1;
  if (state.value !== 'pending') {
    void notifications.refresh();
    return;
  }
  if (attempts >= MAX_ATTEMPTS) {
    timedOut.value = true;
    return;
  }
  timer = setTimeout(poll, POLL_INTERVAL_MS);
}

function restart() {
  if (timer) clearTimeout(timer);
  attempts = 0;
  timedOut.value = false;
  failed.value = false;
  void poll();
}

onMounted(poll);
watch(paymentId, restart);
onBeforeUnmount(() => {
  if (timer) clearTimeout(timer);
});
</script>

<template>
  <div class="mx-auto max-w-md py-8">
    <section class="card flex flex-col items-center text-center" aria-live="polite">
      <template v-if="failed">
        <h1 class="text-2xl">{{ t('payment.result.unknownTitle') }}</h1>
        <RouterLink to="/mes-darets" class="mt-6 no-underline">
          <BaseButton variant="secondary">{{ t('nav.myDarets') }}</BaseButton>
        </RouterLink>
      </template>

      <template v-else>
        <div
          class="mb-5 flex h-16 w-16 items-center justify-center rounded-full"
          :class="{
            'bg-success/15 text-successSoft': state === 'success',
            'bg-primary/15 text-primary': state === 'pending',
            'bg-danger/15 text-dangerSoft': state === 'failed',
            'bg-white/10 text-white/70': state === 'cancelled',
          }"
          aria-hidden="true"
        >
          <svg
            v-if="state === 'pending' && !timedOut"
            class="h-8 w-8 animate-spin"
            viewBox="0 0 24 24"
            fill="none"
          >
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity="0.25" stroke-width="3" />
            <path
              d="M22 12a10 10 0 0 0-10-10"
              stroke="currentColor"
              stroke-width="3"
              stroke-linecap="round"
            />
          </svg>
          <svg
            v-else-if="state === 'success'"
            class="h-8 w-8"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
          </svg>
          <svg
            v-else
            class="h-8 w-8"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>

        <h1 class="text-2xl">
          {{ timedOut ? t('payment.result.slowTitle') : t(`payment.result.${state}Title`) }}
        </h1>
        <p class="mt-2 text-sm text-white/60">
          {{ timedOut ? t('payment.result.slowText') : t(`payment.result.${state}Text`) }}
        </p>
        <p v-if="payment" class="mt-4 text-3xl font-bold text-primary">
          {{ formatCurrency(payment.amount, payment.currency) }}
        </p>
        <p v-if="payment?.failureReason && state !== 'success'" class="mt-2 text-xs text-white/50">
          {{ payment.failureReason }}
        </p>

        <div class="mt-8 flex w-full flex-col gap-3">
          <RouterLink v-if="payment" :to="`/daret/${payment.daretId}`" class="no-underline">
            <BaseButton block>{{ t('payment.result.backToDaret') }}</BaseButton>
          </RouterLink>
          <BaseButton v-if="timedOut" variant="secondary" block @click="restart">{{
            t('common.retry')
          }}</BaseButton>
        </div>
      </template>
    </section>
  </div>
</template>
