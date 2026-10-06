<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import BaseInput from '@/components/BaseInput.vue';
import Modal from '@/components/Modal.vue';
import { useAuthStore } from '@/composables/useAuthStore';
import { useToast } from '@/composables/useToast';
import { errorMessage, fieldErrors, PHONE_PATTERN } from '@/utils/errors';

const { t } = useI18n();
const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const appVersion = __APP_VERSION__;

const profile = reactive({
  firstName: auth.user.value?.firstName ?? '',
  lastName: auth.user.value?.lastName ?? '',
  phone: auth.user.value?.phone ?? '',
});
const profileErrors = ref<Record<string, string>>({});
const savingProfile = ref(false);

const passwords = reactive({ old: '', new: '', confirm: '' });
const passwordErrors = ref<Record<string, string>>({});
const savingPassword = ref(false);

const deleteOpen = ref(false);
const deletePassword = ref('');
const deleteError = ref('');
const deleting = ref(false);

async function saveProfile() {
  const e: Record<string, string> = {};
  if (!profile.firstName.trim()) e.firstName = t('validation.required');
  if (!profile.lastName.trim()) e.lastName = t('validation.required');
  if (profile.phone.trim() && !PHONE_PATTERN.test(profile.phone.trim())) e.phone = t('validation.phone');
  profileErrors.value = e;
  if (Object.keys(e).length) return;

  savingProfile.value = true;
  try {
    await auth.updateProfile({
      firstName: profile.firstName.trim(),
      lastName: profile.lastName.trim(),
      phone: profile.phone.trim(),
    });
    toast.success(t('account.profileSaved'));
  } catch (error) {
    profileErrors.value = fieldErrors(error);
    toast.error(errorMessage(error, t('common.error')));
  } finally {
    savingProfile.value = false;
  }
}

async function changePassword() {
  const e: Record<string, string> = {};
  if (!passwords.old) e.old = t('validation.required');
  if (passwords.new.length < 8) e.new = t('validation.passwordLength');
  if (passwords.new !== passwords.confirm) e.confirm = t('validation.passwordMismatch');
  passwordErrors.value = e;
  if (Object.keys(e).length) return;

  savingPassword.value = true;
  try {
    await auth.changePassword(passwords.old, passwords.new);
    toast.success(t('account.passwordChanged'));
    await router.replace({ name: 'login' });
  } catch (error) {
    toast.error(errorMessage(error, t('common.error')));
  } finally {
    savingPassword.value = false;
  }
}

async function deleteAccount() {
  if (!deletePassword.value) {
    deleteError.value = t('validation.required');
    return;
  }
  deleting.value = true;
  deleteError.value = '';
  try {
    await auth.deleteAccount(deletePassword.value);
    deleteOpen.value = false;
    toast.success(t('account.deleted'));
    await router.replace('/');
  } catch (error) {
    deleteError.value = errorMessage(error, t('common.error'));
  } finally {
    deleting.value = false;
  }
}

async function logout() {
  await auth.logout();
  await router.replace('/');
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <header>
      <h1 class="text-2xl font-bold sm:text-3xl">{{ t('account.title') }}</h1>
      <p class="mt-1 text-sm text-white/60">{{ auth.user.value?.email }}</p>
    </header>

    <form class="card space-y-4" novalidate @submit.prevent="saveProfile">
      <h2 class="text-lg">{{ t('account.profile') }}</h2>
      <div class="grid gap-4 sm:grid-cols-2">
        <BaseInput
          id="firstName"
          v-model="profile.firstName"
          :label="t('auth.firstName')"
          :error="profileErrors.firstName"
          autocomplete="given-name"
        />
        <BaseInput
          id="lastName"
          v-model="profile.lastName"
          :label="t('auth.lastName')"
          :error="profileErrors.lastName"
          autocomplete="family-name"
        />
      </div>
      <BaseInput
        id="phone"
        v-model="profile.phone"
        :label="`${t('auth.phone')} (${t('common.optional')})`"
        type="tel"
        inputmode="tel"
        :error="profileErrors.phone"
        autocomplete="tel"
      />
      <BaseButton type="submit" :loading="savingProfile">{{ t('common.save') }}</BaseButton>
    </form>

    <form class="card space-y-4" novalidate @submit.prevent="changePassword">
      <h2 class="text-lg">{{ t('account.security') }}</h2>
      <BaseInput
        id="oldPassword"
        v-model="passwords.old"
        :label="t('account.currentPassword')"
        type="password"
        :error="passwordErrors.old"
        autocomplete="current-password"
      />
      <div class="grid gap-4 sm:grid-cols-2">
        <BaseInput
          id="newPassword"
          v-model="passwords.new"
          :label="t('account.newPassword')"
          type="password"
          :error="passwordErrors.new"
          autocomplete="new-password"
        />
        <BaseInput
          id="confirmPassword"
          v-model="passwords.confirm"
          :label="t('auth.confirmPassword')"
          type="password"
          :error="passwordErrors.confirm"
          autocomplete="new-password"
        />
      </div>
      <p class="text-xs text-white/50">{{ t('account.passwordNotice') }}</p>
      <BaseButton type="submit" variant="secondary" :loading="savingPassword">{{
        t('account.changePassword')
      }}</BaseButton>
    </form>

    <nav class="card divide-y divide-white/5 p-0" :aria-label="t('legal.title')">
      <RouterLink to="/conditions" class="menu-item px-6 py-4">{{ t('legal.terms') }}</RouterLink>
      <RouterLink to="/confidentialite" class="menu-item px-6 py-4">{{ t('legal.privacy') }}</RouterLink>
      <RouterLink to="/support" class="menu-item px-6 py-4">{{ t('legal.support') }}</RouterLink>
      <button type="button" class="menu-item px-6 py-4" @click="logout">{{ t('auth.logout') }}</button>
    </nav>

    <section class="card space-y-3 border-danger/30">
      <h2 class="text-lg text-dangerSoft">{{ t('account.deleteTitle') }}</h2>
      <p class="text-sm text-white/60">{{ t('account.deleteText') }}</p>
      <BaseButton variant="danger" @click="deleteOpen = true">{{ t('account.deleteButton') }}</BaseButton>
    </section>

    <p class="text-center text-xs text-white/40">{{ t('app.name') }} · v{{ appVersion }}</p>

    <Modal v-model="deleteOpen" :title="t('account.deleteTitle')">
      <form class="space-y-4" novalidate @submit.prevent="deleteAccount">
        <p class="text-sm text-white/70">{{ t('account.deleteConfirm') }}</p>
        <BaseInput
          id="deletePassword"
          v-model="deletePassword"
          :label="t('auth.password')"
          type="password"
          :error="deleteError"
          autocomplete="current-password"
        />
        <div class="flex justify-end gap-3">
          <BaseButton variant="secondary" @click="deleteOpen = false">{{ t('common.cancel') }}</BaseButton>
          <BaseButton type="submit" variant="danger" :loading="deleting">{{
            t('account.deleteButton')
          }}</BaseButton>
        </div>
      </form>
    </Modal>
  </div>
</template>
