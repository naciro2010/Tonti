<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import ContributionList from '@/components/ContributionList.vue';
import InviteBanner from '@/components/InviteBanner.vue';
import Modal from '@/components/Modal.vue';
import RoundHeader from '@/components/RoundHeader.vue';
import StatusBadge from '@/components/StatusBadge.vue';
import Tabs from '@/components/Tabs.vue';
import { useAuthStore } from '@/composables/useAuthStore';
import { formatCurrency } from '@/composables/useCurrency';
import { formatDate } from '@/composables/useDates';
import { useToast } from '@/composables/useToast';
import {
  ApiError,
  daretApi,
  paymentApi,
  type Currency,
  type DaretDetailResponse,
  type Locale,
} from '@/services/api';
import { checkoutChannel, isNative, openExternal, publicUrl } from '@/services/platform';
import { errorMessage } from '@/utils/errors';
import { buildIcs } from '@/utils/ics';

const props = defineProps<{ id: string }>();

const { t, locale } = useI18n();
const router = useRouter();
const auth = useAuthStore();
const toast = useToast();

const daret = ref<DaretDetailResponse | null>(null);
const loading = ref(true);
const notFound = ref(false);
const loadError = ref<string | null>(null);
const onlineCurrencies = ref<Currency[]>([]);
const tab = ref('round');

const paying = ref(false);
const starting = ref(false);
const closing = ref(false);
const leaving = ref(false);
const confirmStart = ref(false);
const confirmLeave = ref(false);

async function load() {
  loading.value = daret.value === null;
  loadError.value = null;
  try {
    daret.value = await daretApi.get(props.id);
  } catch (error) {
    if (error instanceof ApiError && (error.status === 404 || error.status === 403)) notFound.value = true;
    else loadError.value = errorMessage(error, t('common.error'));
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await load();
  onlineCurrencies.value = await paymentApi
    .config()
    .then((config) => config.onlinePaymentCurrencies)
    .catch(() => []);
});

watch(() => props.id, load);

const me = computed(() => daret.value?.membres.find((m) => m.userId === auth.user.value?.id));
const isAdmin = computed(() => me.value?.role === 'CREATEUR' || me.value?.role === 'ADMIN');
const isCreator = computed(() => me.value?.role === 'CREATEUR');
const currentRound = computed(() => daret.value?.rounds.find((round) => !round.estClos));
const expectedPayers = computed(() => Math.max((daret.value?.membres.length ?? 1) - 1, 0));
const pot = computed(() => (daret.value ? daret.value.montantMensuel * expectedPayers.value : 0));

const iAmBeneficiary = computed(
  () => !!currentRound.value && currentRound.value.receveur.id === me.value?.id,
);
const iHavePaid = computed(
  () =>
    !!currentRound.value && !!auth.user.value && currentRound.value.paidUserIds.includes(auth.user.value.id),
);
const canPayOnline = computed(() => !!daret.value && onlineCurrencies.value.includes(daret.value.devise));
const canClose = computed(
  () => isAdmin.value && !!currentRound.value && currentRound.value.paidCount >= expectedPayers.value,
);

const inviteLink = computed(() =>
  daret.value ? publicUrl(`/daret/rejoindre?code=${daret.value.codeInvitation}`) : '',
);

const tabs = computed(() => [
  { id: 'round', label: t('dashboard.tabs.round') },
  { id: 'schedule', label: t('dashboard.tabs.schedule'), count: daret.value?.rounds.length },
  { id: 'members', label: t('dashboard.tabs.members'), count: daret.value?.membres.length },
]);

async function pay() {
  if (!daret.value || !currentRound.value) return;
  paying.value = true;
  try {
    const checkout = await paymentApi.checkout({
      daretId: daret.value.id,
      roundId: currentRound.value.id,
      channel: checkoutChannel,
      locale: locale.value as Locale,
    });
    await openExternal(checkout.redirectUrl);
    if (checkoutChannel === 'APP') {
      // Le résultat arrive par deep link ; à défaut (fermeture manuelle), la page de résultat interroge l'API
      await router.push({ name: 'payment-result', query: { paymentId: checkout.paymentId } });
    }
  } catch (error) {
    toast.error(errorMessage(error, t('payment.startError')));
  } finally {
    paying.value = false;
  }
}

async function start() {
  if (!daret.value) return;
  starting.value = true;
  try {
    await daretApi.start(daret.value.id);
    confirmStart.value = false;
    toast.success(t('dashboard.started'));
    await load();
  } catch (error) {
    toast.error(errorMessage(error, t('common.error')));
  } finally {
    starting.value = false;
  }
}

async function closeRound() {
  if (!daret.value || !currentRound.value) return;
  closing.value = true;
  try {
    await daretApi.closeRound(daret.value.id, currentRound.value.id);
    toast.success(t('dashboard.roundClosed'));
    await load();
  } catch (error) {
    toast.error(errorMessage(error, t('common.error')));
  } finally {
    closing.value = false;
  }
}

async function leave() {
  if (!daret.value) return;
  leaving.value = true;
  try {
    await daretApi.leave(daret.value.id);
    toast.success(t('dashboard.left'));
    await router.replace('/mes-darets');
  } catch (error) {
    toast.error(errorMessage(error, t('common.error')));
  } finally {
    leaving.value = false;
    confirmLeave.value = false;
  }
}

function downloadCalendar() {
  if (!daret.value) return;
  const blob = new Blob([buildIcs(daret.value)], { type: 'text/calendar;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${daret.value.nom.replace(/[^\p{L}\p{N}]+/gu, '_')}.ics`;
  link.click();
  URL.revokeObjectURL(url);
}
</script>

<template>
  <div v-if="loading" class="space-y-4" aria-busy="true">
    <div class="skeleton h-10 w-2/3 rounded-xl" />
    <div class="skeleton h-48 rounded-2xl" />
    <div class="skeleton h-64 rounded-2xl" />
  </div>

  <section v-else-if="notFound" class="card flex flex-col items-center py-16 text-center">
    <h2 class="text-xl">{{ t('dashboard.notFoundTitle') }}</h2>
    <p class="mt-2 max-w-sm text-sm text-white/60">{{ t('dashboard.notFoundText') }}</p>
    <RouterLink to="/mes-darets" class="mt-6 no-underline">
      <BaseButton variant="secondary">{{ t('nav.myDarets') }}</BaseButton>
    </RouterLink>
  </section>

  <section v-else-if="loadError" class="card text-center">
    <p class="text-white/70">{{ loadError }}</p>
    <BaseButton class="mt-4" variant="secondary" @click="load">{{ t('common.retry') }}</BaseButton>
  </section>

  <div v-else-if="daret" class="space-y-6">
    <header class="space-y-2">
      <RouterLink to="/mes-darets" class="text-sm text-white/60 no-underline hover:text-white">
        <span class="inline-block rtl:rotate-180" aria-hidden="true">←</span> {{ t('nav.myDarets') }}
      </RouterLink>
      <div class="flex flex-wrap items-center gap-3">
        <h1 class="text-2xl font-bold sm:text-3xl">{{ daret.nom }}</h1>
        <StatusBadge kind="daret" :status="daret.etat" />
      </div>
      <p class="text-sm text-white/60">
        {{ formatCurrency(daret.montantMensuel, daret.devise) }} / {{ t('common.month') }} ·
        {{ t('darets.members', { count: daret.membres.length, size: daret.taille }) }}
      </p>
      <p v-if="daret.description" class="text-sm text-white/70">{{ daret.description }}</p>
    </header>

    <!-- Recrutement : invitation et démarrage -->
    <template v-if="daret.etat === 'RECRUTEMENT'">
      <InviteBanner :code="daret.codeInvitation" :invitation-url="inviteLink" />
      <section class="card space-y-3">
        <h3>{{ t('dashboard.waitingTitle') }}</h3>
        <p class="text-sm text-white/60">
          {{ isAdmin ? t('dashboard.waitingAdmin') : t('dashboard.waitingMember') }}
        </p>
        <div class="flex flex-wrap gap-3">
          <BaseButton v-if="isAdmin" :disabled="daret.membres.length < 2" @click="confirmStart = true">
            {{ t('dashboard.start') }}
          </BaseButton>
          <BaseButton v-if="!isCreator" variant="ghost" @click="confirmLeave = true">
            {{ t('dashboard.leave') }}
          </BaseButton>
        </div>
      </section>
    </template>

    <!-- Daret en cours -->
    <template v-else-if="currentRound">
      <RoundHeader
        :round="currentRound"
        :total-rounds="daret.rounds.length"
        :pot="pot"
        :currency="daret.devise"
        :expected-payers="expectedPayers"
      />

      <section class="card space-y-3">
        <template v-if="iAmBeneficiary">
          <h3>{{ t('dashboard.youReceiveTitle') }}</h3>
          <p class="text-sm text-white/60">
            {{ t('dashboard.youReceiveText', { amount: formatCurrency(pot, daret.devise) }) }}
          </p>
        </template>
        <template v-else-if="iHavePaid">
          <h3 class="text-successSoft">{{ t('dashboard.paidTitle') }}</h3>
          <p class="text-sm text-white/60">{{ t('dashboard.paidText') }}</p>
        </template>
        <template v-else>
          <h3>{{ t('dashboard.payTitle') }}</h3>
          <p class="text-sm text-white/60">
            {{
              t('dashboard.payText', {
                amount: formatCurrency(daret.montantMensuel, daret.devise),
                date: formatDate(currentRound.dateFin, locale),
              })
            }}
          </p>
          <BaseButton v-if="canPayOnline" size="lg" block :loading="paying" @click="pay">
            {{ t('dashboard.payButton', { amount: formatCurrency(daret.montantMensuel, daret.devise) }) }}
          </BaseButton>
          <p v-else class="rounded-xl bg-white/5 p-3 text-sm text-white/60">{{ t('payment.unavailable') }}</p>
          <p v-if="canPayOnline" class="flex items-center justify-center gap-1.5 text-xs text-white/50">
            <svg class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path
                fill-rule="evenodd"
                d="M10 1a4.5 4.5 0 00-4.5 4.5V9H5a2 2 0 00-2 2v6a2 2 0 002 2h10a2 2 0 002-2v-6a2 2 0 00-2-2h-.5V5.5A4.5 4.5 0 0010 1zm3 8V5.5a3 3 0 10-6 0V9h6z"
                clip-rule="evenodd"
              />
            </svg>
            {{ t('payment.securedBy') }}
          </p>
        </template>
      </section>

      <Tabs v-model="tab" :tabs="tabs" />

      <section v-if="tab === 'round'" class="space-y-4">
        <ContributionList
          :membres="daret.membres"
          :round="currentRound"
          :current-user-id="auth.user.value?.id"
        />
        <div v-if="isAdmin" class="flex flex-wrap items-center gap-3">
          <BaseButton :disabled="!canClose" :loading="closing" @click="closeRound">
            {{ t('dashboard.closeRound') }}
          </BaseButton>
          <p v-if="!canClose" class="text-xs text-white/50">{{ t('dashboard.closeRoundHint') }}</p>
        </div>
      </section>
    </template>

    <!-- Daret terminé -->
    <section v-else class="card text-center">
      <h3>{{ t('dashboard.completedTitle') }}</h3>
      <p class="mt-1 text-sm text-white/60">{{ t('dashboard.completedText') }}</p>
    </section>

    <section v-if="daret.etat !== 'RECRUTEMENT' && tab === 'schedule'" class="space-y-2">
      <ol class="space-y-2">
        <li
          v-for="round in daret.rounds"
          :key="round.id"
          class="flex items-center justify-between gap-4 rounded-xl border border-white/5 bg-surface/40 p-4"
        >
          <div class="flex items-center gap-3">
            <span
              class="flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold"
              :class="round.estClos ? 'bg-success/20 text-successSoft' : 'bg-primary/15 text-primary'"
            >
              {{ round.numero }}
            </span>
            <div>
              <p class="font-semibold">{{ round.receveur.firstName }} {{ round.receveur.lastName }}</p>
              <p class="text-xs text-white/55">
                {{ formatDate(round.dateDebut, locale) }} → {{ formatDate(round.dateFin, locale) }}
              </p>
            </div>
          </div>
          <span class="text-sm" :class="round.estClos ? 'text-successSoft' : 'text-white/60'">
            {{
              round.estClos
                ? formatCurrency(round.montantTotal, daret.devise)
                : `${round.paidCount}/${expectedPayers}`
            }}
          </span>
        </li>
      </ol>
      <BaseButton v-if="!isNative" variant="secondary" size="sm" @click="downloadCalendar">{{
        t('dashboard.calendar')
      }}</BaseButton>
    </section>

    <section v-if="daret.etat === 'RECRUTEMENT' || tab === 'members'" class="card space-y-3">
      <h3>{{ t('dashboard.members') }}</h3>
      <ul class="grid gap-2 sm:grid-cols-2">
        <li
          v-for="membre in daret.membres"
          :key="membre.id"
          class="flex items-center gap-3 rounded-xl border border-white/5 bg-white/5 px-4 py-3"
        >
          <span
            class="flex h-8 w-8 items-center justify-center rounded-full bg-primary/15 text-xs font-bold text-primary"
          >
            {{ membre.position ?? '·' }}
          </span>
          <p class="min-w-0 flex-1 truncate font-medium">{{ membre.firstName }} {{ membre.lastName }}</p>
          <span v-if="membre.role !== 'MEMBRE'" class="chip">{{ t(`roles.${membre.role}`) }}</span>
        </li>
      </ul>
    </section>

    <Modal v-model="confirmStart" :title="t('dashboard.startConfirmTitle')">
      <p class="text-sm text-white/70">
        {{ t('dashboard.startConfirmText', { count: daret.membres.length }) }}
      </p>
      <div class="mt-6 flex justify-end gap-3">
        <BaseButton variant="secondary" @click="confirmStart = false">{{ t('common.cancel') }}</BaseButton>
        <BaseButton :loading="starting" @click="start">{{ t('dashboard.start') }}</BaseButton>
      </div>
    </Modal>

    <Modal v-model="confirmLeave" :title="t('dashboard.leaveConfirmTitle')">
      <p class="text-sm text-white/70">{{ t('dashboard.leaveConfirmText') }}</p>
      <div class="mt-6 flex justify-end gap-3">
        <BaseButton variant="secondary" @click="confirmLeave = false">{{ t('common.cancel') }}</BaseButton>
        <BaseButton variant="danger" :loading="leaving" @click="leave">{{ t('dashboard.leave') }}</BaseButton>
      </div>
    </Modal>
  </div>
</template>
