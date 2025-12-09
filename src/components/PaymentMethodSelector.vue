<script setup lang="ts">
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

import type { PaymentInfo, PaymentMethod } from '@/types';
import BaseButton from './BaseButton.vue';
import BaseInput from './BaseInput.vue';
import BaseSelect from './BaseSelect.vue';

const props = defineProps<{
  modelValue?: PaymentInfo;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (event: 'update:modelValue', value: PaymentInfo): void;
}>();

const { t } = useI18n();

const selectedMethod = ref<PaymentMethod>(props.modelValue?.method || 'cash');
const stripeLink = ref(props.modelValue?.details.stripeLink || '');
const paypalEmail = ref(props.modelValue?.details.paypalEmail || '');
const cryptoNetwork = ref<'BTC' | 'ETH' | 'USDT'>(
  props.modelValue?.details.cryptoAddress?.network || 'BTC',
);
const cryptoAddress = ref(props.modelValue?.details.cryptoAddress?.address || '');
const phoneNumber = ref(props.modelValue?.details.phoneNumber || '');
const iban = ref(props.modelValue?.details.iban || '');
const accountName = ref(props.modelValue?.details.accountName || '');

const paymentMethods: { value: PaymentMethod; label: string; icon: string }[] = [
  { value: 'cash', label: t('payment.methods.cash'), icon: '💵' },
  { value: 'mobile_money', label: t('payment.methods.mobile_money'), icon: '📱' },
  { value: 'bank_transfer', label: t('payment.methods.bank_transfer'), icon: '🏦' },
  { value: 'paypal', label: t('payment.methods.paypal'), icon: '🅿️' },
  { value: 'stripe', label: t('payment.methods.stripe'), icon: '💳' },
  { value: 'crypto', label: t('payment.methods.crypto'), icon: '₿' },
];

const cryptoNetworks = [
  { value: 'BTC', label: 'Bitcoin (BTC)' },
  { value: 'ETH', label: 'Ethereum (ETH)' },
  { value: 'USDT', label: 'Tether (USDT)' },
];

function savePaymentInfo() {
  const paymentInfo: PaymentInfo = {
    method: selectedMethod.value,
    details: {},
  };

  switch (selectedMethod.value) {
    case 'stripe':
      paymentInfo.details.stripeLink = stripeLink.value;
      break;
    case 'paypal':
      paymentInfo.details.paypalEmail = paypalEmail.value;
      break;
    case 'crypto':
      paymentInfo.details.cryptoAddress = {
        network: cryptoNetwork.value,
        address: cryptoAddress.value,
      };
      break;
    case 'mobile_money':
      paymentInfo.details.phoneNumber = phoneNumber.value;
      break;
    case 'bank_transfer':
      paymentInfo.details.iban = iban.value;
      paymentInfo.details.accountName = accountName.value;
      break;
  }

  emit('update:modelValue', paymentInfo);
}

const isValid = computed(() => {
  switch (selectedMethod.value) {
    case 'stripe':
      return stripeLink.value.trim().length > 0;
    case 'paypal':
      return paypalEmail.value.trim().length > 0 && paypalEmail.value.includes('@');
    case 'crypto':
      return cryptoAddress.value.trim().length > 0;
    case 'mobile_money':
      return phoneNumber.value.trim().length > 0;
    case 'bank_transfer':
      return iban.value.trim().length > 0;
    case 'cash':
      return true;
    default:
      return false;
  }
});
</script>

<template>
  <div class="space-y-4">
    <div>
      <label class="mb-2 block text-sm font-medium">{{ t('payment.selectMethod') }}</label>
      <BaseSelect v-model="selectedMethod" :disabled="props.disabled">
        <option v-for="method in paymentMethods" :key="method.value" :value="method.value">
          {{ method.icon }} {{ method.label }}
        </option>
      </BaseSelect>
    </div>

    <!-- Stripe -->
    <div v-if="selectedMethod === 'stripe'" class="space-y-3">
      <BaseInput
        v-model="stripeLink"
        type="url"
        :placeholder="t('payment.stripeLinkPlaceholder')"
        :disabled="props.disabled"
      />
      <p class="text-xs text-white/60">{{ t('payment.stripeHint') }}</p>
    </div>

    <!-- PayPal -->
    <div v-if="selectedMethod === 'paypal'" class="space-y-3">
      <BaseInput
        v-model="paypalEmail"
        type="email"
        :placeholder="t('payment.paypalEmailPlaceholder')"
        :disabled="props.disabled"
      />
      <p class="text-xs text-white/60">{{ t('payment.paypalHint') }}</p>
    </div>

    <!-- Crypto -->
    <div v-if="selectedMethod === 'crypto'" class="space-y-3">
      <BaseSelect v-model="cryptoNetwork" :disabled="props.disabled">
        <option v-for="network in cryptoNetworks" :key="network.value" :value="network.value">
          {{ network.label }}
        </option>
      </BaseSelect>
      <BaseInput
        v-model="cryptoAddress"
        :placeholder="t('payment.cryptoAddressPlaceholder')"
        :disabled="props.disabled"
      />
      <p class="text-xs text-white/60">{{ t('payment.cryptoHint') }}</p>
    </div>

    <!-- Mobile Money -->
    <div v-if="selectedMethod === 'mobile_money'" class="space-y-3">
      <BaseInput
        v-model="phoneNumber"
        type="tel"
        :placeholder="t('payment.phoneNumberPlaceholder')"
        :disabled="props.disabled"
      />
      <p class="text-xs text-white/60">{{ t('payment.mobileMoneyHint') }}</p>
    </div>

    <!-- Bank Transfer -->
    <div v-if="selectedMethod === 'bank_transfer'" class="space-y-3">
      <BaseInput
        v-model="accountName"
        :placeholder="t('payment.accountNamePlaceholder')"
        :disabled="props.disabled"
      />
      <BaseInput
        v-model="iban"
        :placeholder="t('payment.ibanPlaceholder')"
        :disabled="props.disabled"
      />
      <p class="text-xs text-white/60">{{ t('payment.bankTransferHint') }}</p>
    </div>

    <!-- Cash -->
    <div v-if="selectedMethod === 'cash'" class="rounded-lg border border-white/10 bg-white/5 p-4">
      <p class="text-sm text-white/70">{{ t('payment.cashHint') }}</p>
    </div>

    <BaseButton
      :disabled="!isValid || props.disabled"
      class="w-full"
      @click="savePaymentInfo"
    >
      {{ t('payment.saveMethod') }}
    </BaseButton>
  </div>
</template>
