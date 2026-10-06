<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import BaseInput from '@/components/BaseInput.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import { useAuthStore } from '@/composables/useAuthStore';
import { formatCurrency } from '@/composables/useCurrency';
import { useToast } from '@/composables/useToast';
import { ApiError, daretApi, type DaretResponse } from '@/services/api';
import { errorMessage } from '@/utils/errors';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const code = ref(typeof route.query.code === 'string' ? route.query.code.toUpperCase() : '');
const preview = ref<DaretResponse | null>(null);
const error = ref('');
const searching = ref(false);
const joining = ref(false);

async function search() {
  const value = code.value.trim().toUpperCase();
  if (!/^[A-Z0-9]{6}$/.test(value)) {
    error.value = t('join.invalidCode');
    return;
  }
  error.value = '';
  searching.value = true;
  try {
    preview.value = await daretApi.preview(value);
  } catch (e) {
    preview.value = null;
    error.value =
      e instanceof ApiError && e.status === 404 ? t('join.notFound') : errorMessage(e, t('common.error'));
  } finally {
    searching.value = false;
  }
}

async function join() {
  if (!preview.value) return;
  if (!auth.isAuthenticated.value) {
    await router.push({
      name: 'login',
      query: { redirect: `/daret/rejoindre?code=${preview.value.codeInvitation}` },
    });
    return;
  }
  joining.value = true;
  try {
    await daretApi.join(preview.value.codeInvitation);
    toast.success(t('join.success', { name: preview.value.nom }));
    await router.replace(`/daret/${preview.value.id}`);
  } catch (e) {
    toast.error(errorMessage(e, t('common.error')));
  } finally {
    joining.value = false;
  }
}

onMounted(() => {
  if (code.value) void search();
});
</script>

<template>
  <div class="mx-auto max-w-md space-y-6">
    <header class="text-center">
      <h1 class="text-2xl font-bold sm:text-3xl">{{ t('join.title') }}</h1>
      <p class="mt-1 text-sm text-white/60">{{ t('join.subtitle') }}</p>
    </header>

    <form class="card space-y-4" novalidate @submit.prevent="search">
      <BaseInput
        id="code"
        v-model="code"
        :label="t('join.code')"
        :hint="t('join.codeHint')"
        :error="error"
        maxlength="6"
        autocapitalize="characters"
        autocomplete="off"
        class="text-center font-mono text-xl uppercase tracking-[0.3em]"
      />
      <BaseButton type="submit" block variant="secondary" :loading="searching">{{
        t('join.search')
      }}</BaseButton>
    </form>

    <section v-if="preview" class="card space-y-4">
      <div class="flex items-start justify-between gap-3">
        <h2 class="text-xl">{{ preview.nom }}</h2>
        <StatusBadge kind="daret" :status="preview.etat" />
      </div>
      <p v-if="preview.description" class="text-sm text-white/70">{{ preview.description }}</p>
      <dl class="grid grid-cols-2 gap-3 text-sm">
        <div class="rounded-xl bg-white/5 p-3">
          <dt class="text-white/50">{{ t('create.fields.amount') }}</dt>
          <dd class="font-semibold">{{ formatCurrency(preview.montantMensuel, preview.devise) }}</dd>
        </div>
        <div class="rounded-xl bg-white/5 p-3">
          <dt class="text-white/50">{{ t('dashboard.members') }}</dt>
          <dd class="font-semibold">{{ preview.membresCount }} / {{ preview.taille }}</dd>
        </div>
      </dl>
      <BaseButton v-if="preview.etat === 'RECRUTEMENT'" block :loading="joining" @click="join">
        {{ auth.isAuthenticated.value ? t('join.join') : t('join.loginToJoin') }}
      </BaseButton>
      <p v-else class="text-center text-sm text-white/60">{{ t('join.closed') }}</p>
    </section>
  </div>
</template>
