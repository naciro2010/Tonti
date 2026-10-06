<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import BaseInput from '@/components/BaseInput.vue';
import { useAuthStore } from '@/composables/useAuthStore';
import { useToast } from '@/composables/useToast';
import { EMAIL_PATTERN, errorMessage } from '@/utils/errors';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const form = reactive({ email: '', password: '' });
const errors = ref<Record<string, string>>({});
const submitting = ref(false);

function validate() {
  errors.value = {};
  if (!form.email.trim()) errors.value.email = t('validation.required');
  else if (!EMAIL_PATTERN.test(form.email.trim())) errors.value.email = t('validation.email');
  if (!form.password) errors.value.password = t('validation.required');
  return Object.keys(errors.value).length === 0;
}

/** N'accepte que des chemins internes pour éviter les redirections ouvertes. */
function redirectTarget() {
  const redirect = route.query.redirect;
  return typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')
    ? redirect
    : '/mes-darets';
}

async function handleSubmit() {
  if (!validate()) return;
  submitting.value = true;
  try {
    await auth.login(form.email.trim(), form.password);
    toast.success(t('auth.loginSuccess'));
    await router.replace(redirectTarget());
  } catch (error) {
    toast.error(errorMessage(error, t('auth.invalidCredentials')));
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="mx-auto max-w-md py-6 sm:py-12">
    <div class="card p-6 sm:p-8">
      <div class="mb-8 text-center">
        <h1 class="text-2xl font-bold">{{ t('auth.loginTitle') }}</h1>
        <p class="mt-2 text-sm text-white/60">{{ t('auth.loginSubtitle') }}</p>
      </div>

      <form class="space-y-5" novalidate @submit.prevent="handleSubmit">
        <BaseInput
          id="email"
          v-model="form.email"
          :label="t('auth.email')"
          type="email"
          :error="errors.email"
          required
          autocomplete="email"
          inputmode="email"
          autocapitalize="off"
        />
        <BaseInput
          id="password"
          v-model="form.password"
          :label="t('auth.password')"
          type="password"
          :error="errors.password"
          required
          autocomplete="current-password"
        />
        <BaseButton type="submit" block :loading="submitting">{{ t('auth.login') }}</BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-white/60">
        {{ t('auth.noAccount') }}
        <RouterLink
          :to="{ name: 'register', query: route.query }"
          class="font-semibold text-primary no-underline hover:text-primaryHover"
        >
          {{ t('auth.createAccount') }}
        </RouterLink>
      </p>
    </div>
  </div>
</template>
