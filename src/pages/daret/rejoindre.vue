<script setup lang="ts">
import { reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { nanoid } from 'nanoid';

import BaseButton from '@/components/BaseButton.vue';
import BaseInput from '@/components/BaseInput.vue';
import Toast from '@/components/Toast.vue';
import { useDaretStore } from '@/composables/useDaretStore';

const { t } = useI18n();
const store = useDaretStore();
const route = useRoute();
const router = useRouter();

const form = reactive({
  code: (route.query.code as string) || '',
  nom: '',
  contact: '',
});

const toast = reactive({ show: false, message: '', type: 'success' as 'success' | 'error' | 'info' });

function showToast(message: string, type: 'success' | 'error') {
  toast.show = true;
  toast.message = message;
  toast.type = type;
  setTimeout(() => (toast.show = false), 2500);
}

function join() {
  if (!form.code.trim() || !form.nom.trim()) {
    showToast(t('wizard.validation'), 'error');
    return;
  }
  const membre = {
    id: nanoid(6),
    nom: form.nom.trim(),
    contact: form.contact.trim() || undefined,
  };
  const daret = store.joinDaretByCode(form.code.trim(), membre);
  if (daret) {
    showToast(t('join.success'), 'success');
    router.push(`/daret/${daret.id}`);
  } else {
    showToast(t('join.error'), 'error');
  }
}
</script>

<template>
  <section class="mx-auto max-w-xl space-y-6">
    <header class="space-y-2 text-center">
      <h1 class="text-3xl font-semibold">{{ t('join.title') }}</h1>
      <p class="text-sm text-white/70">{{ t('join.subtitle') }}</p>
    </header>
    <form class="card space-y-4" @submit.prevent="join">
      <BaseInput id="code" v-model="form.code" :label="t('form.invitationCode')" required />
      <BaseInput id="nom" v-model="form.nom" :label="t('form.memberName')" required />
      <BaseInput id="contact" v-model="form.contact" :label="t('form.memberContact')" />
      <BaseButton type="submit">{{ t('actions.join') }}</BaseButton>
    </form>
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </section>
</template>
