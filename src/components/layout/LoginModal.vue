<script setup lang="ts">
import { Dialog, DialogPanel, DialogTitle, TransitionChild, TransitionRoot } from '@headlessui/vue';
import { ref } from 'vue';
import BaseButton from '../ui/BaseButton.vue';
import TextInput from '../ui/TextInput.vue';
import { useLoginModal } from '../../composables/useLoginModal';
import { useI18n } from 'vue-i18n';
import { useToast } from '../../composables/useToast';

const { isOpen, close } = useLoginModal();
const { t } = useI18n();
const { push } = useToast();

const email = ref('');
const password = ref('');

const submit = () => {
  push({
    level: 'info',
    title: t('login.socialComingSoon'),
    description: 'Mock login pour démonstration.',
  });
  close();
  email.value = '';
  password.value = '';
};
</script>

<template>
  <TransitionRoot :show="isOpen" as="template">
    <Dialog as="div" class="relative z-40" @close="close">
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
            <DialogPanel class="w-full max-w-lg rounded-3xl border border-white/10 bg-background/95 p-8 shadow-2xl">
              <DialogTitle class="text-2xl font-semibold text-white">{{ t('login.title') }}</DialogTitle>
              <p class="mt-2 text-sm text-white/70">{{ t('login.subtitle') }}</p>

              <form class="mt-6 space-y-5" @submit.prevent="submit">
                <TextInput
                  id="login-email"
                  :label="t('login.emailLabel')"
                  :placeholder="t('login.emailPlaceholder')"
                  type="email"
                  required
                  v-model="email"
                />
                <TextInput
                  id="login-password"
                  :label="t('login.passwordLabel')"
                  :placeholder="t('login.passwordPlaceholder')"
                  type="password"
                  required
                  v-model="password"
                />
                <label class="flex items-center gap-2 text-sm text-white/70">
                  <input type="checkbox" class="h-4 w-4 rounded border-white/20 bg-white/10" />
                  <span>{{ t('login.remember') }}</span>
                </label>
                <BaseButton type="submit" class="w-full">{{ t('login.submit') }}</BaseButton>
                <BaseButton variant="outline" class="w-full" type="button" disabled>
                  {{ t('login.socialComingSoon') }}
                </BaseButton>
              </form>

              <p class="mt-6 text-center text-sm text-white/70">
                {{ t('login.noAccount') }}
                <span class="font-semibold text-primary">{{ t('login.createOne') }}</span>
              </p>
            </DialogPanel>
          </TransitionChild>
        </div>
      </div>
    </Dialog>
  </TransitionRoot>
</template>
