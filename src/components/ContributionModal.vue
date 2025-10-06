<script setup lang="ts">
import { Dialog, DialogPanel, DialogTitle, TransitionChild, TransitionRoot } from '@headlessui/vue';
import { ref, watch } from 'vue';
import BaseButton from './ui/BaseButton.vue';
import TextInput from './ui/TextInput.vue';
import { useToast } from '../composables/useToast';
import { useI18n } from 'vue-i18n';
import { formatCurrency } from '../utils/currency';

const props = defineProps<{
  open: boolean;
  quickOptions: number[];
  minimum: number;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const amount = ref<number>(props.quickOptions[0] ?? props.minimum);
const anonymous = ref(false);
const isProcessing = ref(false);
const { push } = useToast();
const { t, locale } = useI18n();

watch(
  () => props.open,
  (value) => {
    if (value) {
      amount.value = props.quickOptions[0] ?? props.minimum;
      anonymous.value = false;
    }
  }
);

const pay = async () => {
  if (amount.value < props.minimum) {
    push({
      level: 'warning',
      title: t('cagnotte.amountLabel'),
      description: `Minimum ${formatCurrency(props.minimum, locale.value === 'ar' ? 'ar-MA' : 'fr-MA')}`,
    });
    return;
  }
  isProcessing.value = true;
  try {
    await fetch('/api/payment/simulate/index.json').then((response) => response.json());
    push({
      level: 'success',
      title: 'Contribution simulée',
      description: `Merci pour ${formatCurrency(amount.value, locale.value === 'ar' ? 'ar-MA' : 'fr-MA')} !`,
    });
    emit('close');
  } catch (error) {
    push({ level: 'error', title: 'Échec de la contribution', description: String(error) });
  } finally {
    isProcessing.value = false;
  }
};
</script>

<template>
  <TransitionRoot :show="open" as="template">
    <Dialog as="div" class="relative z-40" @close="emit('close')">
      <TransitionChild
        as="template"
        enter="ease-out duration-200"
        enter-from="opacity-0"
        enter-to="opacity-100"
        leave="ease-in duration-150"
        leave-from="opacity-100"
        leave-to="opacity-0"
      >
        <div class="fixed inset-0 bg-black/60 backdrop-blur" />
      </TransitionChild>

      <div class="fixed inset-0 overflow-y-auto">
        <div class="flex min-h-full items-center justify-center p-4">
          <TransitionChild
            as="template"
            enter="ease-out duration-200"
            enter-from="opacity-0 scale-95"
            enter-to="opacity-100 scale-100"
            leave="ease-in duration-150"
            leave-from="opacity-100 scale-100"
            leave-to="opacity-0 scale-95"
          >
            <DialogPanel class="w-full max-w-md rounded-3xl border border-white/10 bg-background/95 p-8 shadow-2xl">
              <DialogTitle class="text-xl font-semibold">{{ t('cagnotte.contributeTitle') }}</DialogTitle>
              <div class="mt-4 space-y-4">
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="option in props.quickOptions"
                    :key="option"
                    type="button"
                    class="rounded-full border px-4 py-2 text-sm"
                    :class="[
                      amount === option
                        ? 'border-primary bg-primary/20 text-primary'
                        : 'border-white/20 text-white/70 hover:border-primary',
                    ]"
                    @click="amount = option"
                  >
                    {{ formatCurrency(option, locale === 'ar' ? 'ar-MA' : 'fr-MA') }}
                  </button>
                </div>
                <TextInput
                  id="custom-amount"
                  type="number"
                  :label="t('cagnotte.amountLabel')"
                  :description="`Min ${formatCurrency(props.minimum, locale === 'ar' ? 'ar-MA' : 'fr-MA')}`"
                  v-model="amount"
                />
                <label class="flex items-center gap-2 text-sm text-white/70">
                  <input v-model="anonymous" type="checkbox" class="h-4 w-4 rounded border-white/20 bg-white/10" />
                  <span>{{ t('cagnotte.anonymous') }}</span>
                </label>
                <BaseButton :disabled="isProcessing" class="w-full" type="button" @click="pay">
                  {{ isProcessing ? '...' : t('cagnotte.payButton') }}
                </BaseButton>
              </div>
            </DialogPanel>
          </TransitionChild>
        </div>
      </div>
    </Dialog>
  </TransitionRoot>
</template>
