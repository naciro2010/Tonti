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
  <section class="mx-auto max-w-xl space-y-8 animate-fade-in-up">
    <header class="space-y-3 text-center">
      <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/15 text-primary ring-1 ring-inset ring-primary/30">
        <svg class="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
        </svg>
      </div>
      <h1 class="text-3xl font-bold tracking-tight sm:text-4xl">{{ t('join.title') }}</h1>
      <p class="text-sm text-white/70">{{ t('join.subtitle') }}</p>
    </header>
    <form class="card space-y-5" @submit.prevent="join">
      <BaseInput
        id="code"
        v-model="form.code"
        :label="t('form.invitationCode')"
        hint="Le code d'invitation vous a été partagé par l'organisateur."
        placeholder="ABC-123"
        required
        autocapitalize="characters"
      />
      <BaseInput
        id="nom"
        v-model="form.nom"
        :label="t('form.memberName')"
        placeholder="Votre nom complet"
        required
      />
      <BaseInput
        id="contact"
        v-model="form.contact"
        :label="t('form.memberContact')"
        placeholder="Email ou téléphone (optionnel)"
        hint="Facilite les rappels si l'organisateur les active."
      />
      <BaseButton type="submit" block size="lg">
        {{ t('actions.join') }}
        <svg class="h-4 w-4 rtl:rotate-180" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
          <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
        </svg>
      </BaseButton>
    </form>
    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </section>
</template>
