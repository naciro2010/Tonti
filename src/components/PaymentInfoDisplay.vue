<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import QrcodeVue from 'qrcode.vue';

import type { PaymentInfo } from '@/types';
import { usePaymentIntegration } from '@/composables/usePaymentIntegration';
import BaseButton from './BaseButton.vue';
import Toast from './Toast.vue';

const props = defineProps<{
  paymentInfo: PaymentInfo;
  amount?: number;
  currency?: string;
}>();

const { t } = useI18n();
const {
  formatPaymentInfo,
  getPaymentMethodIcon,
  copyPaymentInfo,
  generateCryptoQRData,
} = usePaymentIntegration();

const showToast = ref(false);
const toastMessage = ref('');

async function handleCopy() {
  const success = await copyPaymentInfo(props.paymentInfo);
  if (success) {
    toastMessage.value = t('payment.copied');
    showToast.value = true;
  }
}

function getQRCodeData(): string {
  if (props.paymentInfo.method === 'crypto' && props.paymentInfo.details.cryptoAddress) {
    const { network, address } = props.paymentInfo.details.cryptoAddress;
    return generateCryptoQRData(network, address, props.amount);
  }
  if (props.paymentInfo.method === 'mobile_money' && props.paymentInfo.details.phoneNumber) {
    return props.paymentInfo.details.phoneNumber;
  }
  if (props.paymentInfo.method === 'paypal' && props.paymentInfo.details.paypalEmail) {
    return props.paymentInfo.details.paypalEmail;
  }
  return '';
}

function getPaymentLink(): string | null {
  if (props.paymentInfo.method === 'stripe' && props.paymentInfo.details.stripeLink) {
    return props.paymentInfo.details.stripeLink;
  }
  if (props.paymentInfo.method === 'paypal' && props.paymentInfo.details.paypalEmail) {
    return `https://paypal.me/${props.paymentInfo.details.paypalEmail.split('@')[0]}`;
  }
  return null;
}

const methodLabels: Record<string, string> = {
  stripe: t('payment.methods.stripe'),
  paypal: t('payment.methods.paypal'),
  crypto: t('payment.methods.crypto'),
  mobile_money: t('payment.methods.mobile_money'),
  bank_transfer: t('payment.methods.bank_transfer'),
  cash: t('payment.methods.cash'),
};
</script>

<template>
  <div class="card space-y-4">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <span class="text-2xl">{{ getPaymentMethodIcon(paymentInfo.method) }}</span>
        <div>
          <h3 class="font-semibold">{{ methodLabels[paymentInfo.method] }}</h3>
          <p class="text-sm text-white/60">{{ formatPaymentInfo(paymentInfo) }}</p>
        </div>
      </div>
    </div>

    <!-- Montant -->
    <div v-if="amount && currency" class="rounded-lg border border-primary/20 bg-primary/10 p-3">
      <p class="text-sm text-white/70">{{ t('payment.amountToPay') }}</p>
      <p class="text-2xl font-bold text-primary">
        {{ amount }} {{ currency }}
      </p>
    </div>

    <!-- QR Code pour crypto et mobile money -->
    <div v-if="getQRCodeData()" class="flex justify-center rounded-lg bg-white p-4">
      <QrcodeVue :value="getQRCodeData()" :size="200" level="H" />
    </div>

    <!-- Détails selon la méthode -->
    <div class="space-y-2 rounded-lg border border-white/10 bg-white/5 p-4 text-sm">
      <!-- Stripe -->
      <div v-if="paymentInfo.method === 'stripe' && paymentInfo.details.stripeLink">
        <p class="text-white/60">{{ t('payment.stripeLink') }}</p>
        <p class="break-all font-mono text-xs text-white/90">{{ paymentInfo.details.stripeLink }}</p>
      </div>

      <!-- PayPal -->
      <div v-if="paymentInfo.method === 'paypal' && paymentInfo.details.paypalEmail">
        <p class="text-white/60">{{ t('payment.paypalEmail') }}</p>
        <p class="font-mono text-white/90">{{ paymentInfo.details.paypalEmail }}</p>
      </div>

      <!-- Crypto -->
      <div v-if="paymentInfo.method === 'crypto' && paymentInfo.details.cryptoAddress">
        <p class="text-white/60">{{ t('payment.cryptoNetwork') }}</p>
        <p class="font-semibold text-primary">{{ paymentInfo.details.cryptoAddress.network }}</p>
        <p class="mt-2 text-white/60">{{ t('payment.cryptoAddress') }}</p>
        <p class="break-all font-mono text-xs text-white/90">
          {{ paymentInfo.details.cryptoAddress.address }}
        </p>
      </div>

      <!-- Mobile Money -->
      <div v-if="paymentInfo.method === 'mobile_money' && paymentInfo.details.phoneNumber">
        <p class="text-white/60">{{ t('payment.phoneNumber') }}</p>
        <p class="font-mono text-lg text-white/90">{{ paymentInfo.details.phoneNumber }}</p>
      </div>

      <!-- Bank Transfer -->
      <div v-if="paymentInfo.method === 'bank_transfer'">
        <div v-if="paymentInfo.details.accountName">
          <p class="text-white/60">{{ t('payment.accountName') }}</p>
          <p class="font-semibold text-white/90">{{ paymentInfo.details.accountName }}</p>
        </div>
        <div v-if="paymentInfo.details.iban" class="mt-2">
          <p class="text-white/60">{{ t('payment.iban') }}</p>
          <p class="font-mono text-white/90">{{ paymentInfo.details.iban }}</p>
        </div>
      </div>

      <!-- Cash -->
      <div v-if="paymentInfo.method === 'cash'">
        <p class="text-white/70">{{ t('payment.cashInfo') }}</p>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex flex-wrap gap-2">
      <BaseButton
        v-if="paymentInfo.method !== 'cash'"
        variant="secondary"
        class="flex-1"
        @click="handleCopy"
      >
        {{ t('payment.copyInfo') }}
      </BaseButton>
      <BaseButton
        v-if="getPaymentLink()"
        class="flex-1"
        @click="() => window.open(getPaymentLink()!, '_blank')"
      >
        {{ t('payment.openLink') }}
      </BaseButton>
    </div>

    <Toast v-model="showToast" :message="toastMessage" />
  </div>
</template>
