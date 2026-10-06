<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import BaseInput from '@/components/BaseInput.vue';
import { useAuthStore } from '@/composables/useAuthStore';
import { useToast } from '@/composables/useToast';
import { EMAIL_PATTERN, PHONE_PATTERN, errorMessage, fieldErrors } from '@/utils/errors';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false,
});
const errors = ref<Record<string, string>>({});
const submitting = ref(false);

function validate() {
  const e: Record<string, string> = {};
  if (!form.firstName.trim()) e.firstName = t('validation.required');
  if (!form.lastName.trim()) e.lastName = t('validation.required');
  if (!form.email.trim()) e.email = t('validation.required');
  else if (!EMAIL_PATTERN.test(form.email.trim())) e.email = t('validation.email');
  if (form.phone.trim() && !PHONE_PATTERN.test(form.phone.trim())) e.phone = t('validation.phone');
  if (form.password.length < 8) e.password = t('validation.passwordLength');
  if (form.password !== form.confirmPassword) e.confirmPassword = t('validation.passwordMismatch');
  if (!form.acceptTerms) e.acceptTerms = t('auth.termsRequired');
  errors.value = e;
  return Object.keys(e).length === 0;
}

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
    await auth.register({
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      email: form.email.trim(),
      password: form.password,
      phone: form.phone.trim() || undefined,
    });
    toast.success(t('auth.registerSuccess'));
    await router.replace(redirectTarget());
  } catch (error) {
    errors.value = fieldErrors(error);
    toast.error(errorMessage(error, t('common.error')));
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="mx-auto max-w-lg py-6 sm:py-12">
    <div class="card p-6 sm:p-8">
      <div class="mb-8 text-center">
        <h1 class="text-2xl font-bold">{{ t('auth.registerTitle') }}</h1>
        <p class="mt-2 text-sm text-white/60">{{ t('auth.registerSubtitle') }}</p>
      </div>

      <form class="space-y-5" novalidate @submit.prevent="handleSubmit">
        <div class="grid gap-4 sm:grid-cols-2">
          <BaseInput
            id="firstName"
            v-model="form.firstName"
            :label="t('auth.firstName')"
            :error="errors.firstName"
            required
            autocomplete="given-name"
          />
          <BaseInput
            id="lastName"
            v-model="form.lastName"
            :label="t('auth.lastName')"
            :error="errors.lastName"
            required
            autocomplete="family-name"
          />
        </div>
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
          id="phone"
          v-model="form.phone"
          :label="`${t('auth.phone')} (${t('common.optional')})`"
          type="tel"
          :error="errors.phone"
          :hint="t('auth.phoneHint')"
          autocomplete="tel"
          inputmode="tel"
        />
        <BaseInput
          id="password"
          v-model="form.password"
          :label="t('auth.password')"
          type="password"
          :error="errors.password"
          :hint="t('auth.passwordHint')"
          required
          autocomplete="new-password"
        />
        <BaseInput
          id="confirmPassword"
          v-model="form.confirmPassword"
          :label="t('auth.confirmPassword')"
          type="password"
          :error="errors.confirmPassword"
          required
          autocomplete="new-password"
        />

        <div>
          <label class="flex items-start gap-3 text-sm font-normal text-white/80">
            <input
              v-model="form.acceptTerms"
              type="checkbox"
              class="mt-0.5 h-5 w-5 flex-shrink-0 rounded border-white/20 p-0 accent-primary"
              :aria-invalid="errors.acceptTerms ? 'true' : undefined"
            />
            <i18n-t keypath="auth.acceptTerms" tag="span">
              <template #terms>
                <RouterLink to="/conditions">{{ t('legal.terms') }}</RouterLink>
              </template>
              <template #privacy>
                <RouterLink to="/confidentialite">{{ t('legal.privacy') }}</RouterLink>
              </template>
            </i18n-t>
          </label>
          <p v-if="errors.acceptTerms" class="mt-1.5 text-xs font-medium text-dangerSoft" role="alert">
            {{ errors.acceptTerms }}
          </p>
        </div>

        <BaseButton type="submit" block :loading="submitting">{{ t('auth.register') }}</BaseButton>
      </form>

      <p class="mt-6 text-center text-sm text-white/60">
        {{ t('auth.hasAccount') }}
        <RouterLink
          :to="{ name: 'login', query: route.query }"
          class="font-semibold text-primary no-underline hover:text-primaryHover"
        >
          {{ t('auth.login') }}
        </RouterLink>
      </p>
    </div>
  </div>
</template>
