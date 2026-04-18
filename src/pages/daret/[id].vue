<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

import BaseButton from '@/components/BaseButton.vue';
import ContributionGrid from '@/components/ContributionGrid.vue';
import InviteBanner from '@/components/InviteBanner.vue';
import PaymentStatusBadge from '@/components/PaymentStatusBadge.vue';
import RoundHeader from '@/components/RoundHeader.vue';
import Tabs from '@/components/Tabs.vue';
import Toast from '@/components/Toast.vue';
import { useDaretStore } from '@/composables/useDaretStore';
import { daysRemaining, currentRound as pickCurrentRound, formatDate } from '@/composables/useDates';
import { buildIcs } from '@/utils/ics';
import { calculateTotalPot, completionPercentage, countByStatut, isRoundClosable } from '@/utils/math';

const store = useDaretStore();
const route = useRoute();
const { t, locale } = useI18n();

const daret = computed(() => store.getDaret(route.params.id as string));

const activeRound = computed(() => (daret.value ? pickCurrentRound(daret.value.rounds) : undefined));

const paymentsForActive = computed(() => {
  if (!daret.value || !activeRound.value) {
    return [];
  }
  return daret.value.paiements.filter((paiement) => paiement.round === activeRound.value?.index);
});

const tabs = ref('payments');

const toast = reactive({ show: false, message: '', type: 'success' as 'success' | 'error' | 'info' });

const inviteLink = computed(() => {
  if (!daret.value || typeof window === 'undefined') {
    return '';
  }
  const base = window.location.origin + import.meta.env.BASE_URL;
  return `${base}daret/rejoindre?code=${daret.value.codeInvitation}`;
});

const notifications = reactive({
  supported: typeof window !== 'undefined' && 'Notification' in window,
  permission: typeof window !== 'undefined' && 'Notification' in window ? Notification.permission : 'default',
});

function notify(message: string) {
  toast.show = true;
  toast.message = message;
  toast.type = 'success';
  setTimeout(() => (toast.show = false), 2500);
}

async function toggleNotificationPermission() {
  if (!notifications.supported) {
    return;
  }
  const permission = await Notification.requestPermission();
  notifications.permission = permission;
  if (permission === 'granted') {
    notify(t('notifications.enabled'));
  } else if (permission === 'denied') {
    toast.show = true;
    toast.type = 'error';
    toast.message = t('notifications.denied');
    setTimeout(() => (toast.show = false), 2500);
  }
}

function markPaiement(payload: { membreId: string; statut: 'a_payer' | 'paye' | 'retard' }) {
  if (!daret.value || !activeRound.value) {
    return;
  }
  store.markPaiement(daret.value.id, payload.membreId, activeRound.value.index, payload.statut);
  notify(t('dashboard.statusUpdated'));
}

function closeRound() {
  if (!daret.value || !activeRound.value) {
    return;
  }
  store.closeRound(daret.value.id, activeRound.value.index);
  notify(t('dashboard.roundClosed'));
}

function downloadCalendar() {
  if (!daret.value) {
    return;
  }
  const ics = buildIcs(daret.value);
  const blob = new Blob([ics], { type: 'text/calendar;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${daret.value.nom.replace(/\s+/g, '_')}.ics`;
  link.click();
  URL.revokeObjectURL(url);
}
</script>

<template>
  <section v-if="daret" class="space-y-8">
    <RoundHeader
      v-if="activeRound"
      :round-index="activeRound.index"
      :total-rounds="daret.rounds.length"
      :receveur="daret.membres.find((member) => member.id === activeRound.receveurId)"
      :total="calculateTotalPot(daret)"
      :devise="daret.devise"
      :days-remaining="daysRemaining(activeRound)"
    />

    <Tabs
      v-model="tabs"
      :tabs="[
        { id: 'payments', label: t('dashboard.payments'), count: paymentsForActive.length },
        { id: 'history', label: t('dashboard.history'), count: daret.rounds.length },
      ]"
    />

    <section v-if="tabs === 'payments'" class="space-y-6">
      <ContributionGrid
        v-if="activeRound"
        :membres="daret.membres"
        :paiements="daret.paiements"
        :round-index="activeRound.index"
        @update="markPaiement"
      />
      <div class="flex flex-wrap gap-3">
        <BaseButton
          type="button"
          :disabled="!activeRound || !paymentsForActive.length || !isRoundClosable(paymentsForActive)"
          @click="closeRound"
        >
          {{ t('actions.closeRound') }}
        </BaseButton>
        <BaseButton type="button" variant="secondary" @click="downloadCalendar">
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M6 2a1 1 0 011 1v1h6V3a1 1 0 112 0v1h1a2 2 0 012 2v10a2 2 0 01-2 2H4a2 2 0 01-2-2V6a2 2 0 012-2h1V3a1 1 0 011-1zm10 6H4v8h12V8zM9 11a1 1 0 100 2 1 1 0 000-2z" clip-rule="evenodd" />
          </svg>
          {{ t('actions.downloadIcs') }}
        </BaseButton>
        <BaseButton
          v-if="notifications.supported"
          type="button"
          variant="ghost"
          @click="toggleNotificationPermission"
        >
          <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
          </svg>
          {{ t('notifications.enable') }}
        </BaseButton>
      </div>
    </section>

    <section v-if="tabs === 'history'" class="card space-y-4">
      <h3 class="text-lg font-semibold">{{ t('dashboard.history') }}</h3>
      <ul v-if="daret.rounds.length" class="space-y-2">
        <li
          v-for="round in daret.rounds"
          :key="round.index"
          class="flex items-center justify-between gap-4 rounded-xl border border-white/5 bg-surface/40 p-4 transition-colors hover:border-white/15"
        >
          <div class="flex items-center gap-3">
            <span
              class="flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold"
              :class="round.clos ? 'bg-success/20 text-successSoft' : 'bg-primary/15 text-primary'"
            >
              {{ round.index }}
            </span>
            <div>
              <p class="font-semibold">{{ t('dashboard.round', { current: round.index, total: daret.rounds.length }) }}</p>
              <p class="text-xs text-white/55">{{ formatDate(round.dateDebut, locale) }} → {{ formatDate(round.dateFin, locale) }}</p>
            </div>
          </div>
          <div class="text-right text-sm text-white/70">
            <p class="font-medium text-white">{{ daret.membres.find((membre) => membre.id === round.receveurId)?.nom }}</p>
            <PaymentStatusBadge :statut="round.clos ? 'paye' : 'a_payer'" />
          </div>
        </li>
      </ul>
      <p v-else class="text-sm text-white/60">{{ t('dashboard.noHistory') }}</p>
    </section>

    <section class="card space-y-4">
      <h3 class="flex items-center gap-2 text-lg font-semibold">
        <svg class="h-5 w-5 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
        </svg>
        {{ t('dashboard.roster') }}
      </h3>
      <ul class="grid gap-2 sm:grid-cols-2">
        <li
          v-for="(memberId, index) in daret.roster"
          :key="memberId"
          class="flex items-center gap-3 rounded-xl border border-white/5 bg-white/5 px-4 py-3 transition-colors hover:border-primary/30"
        >
          <span class="flex h-8 w-8 items-center justify-center rounded-full bg-primary/15 text-xs font-bold text-primary ring-1 ring-inset ring-primary/25">
            {{ index + 1 }}
          </span>
          <p class="truncate font-semibold">{{ daret.membres.find((membre) => membre.id === memberId)?.nom }}</p>
        </li>
      </ul>
    </section>

    <InviteBanner v-if="inviteLink" :invitation-url="inviteLink" />

    <section class="card">
      <h3 class="flex items-center gap-2 text-lg font-semibold">
        <svg class="h-5 w-5 text-primary" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />
        </svg>
        Statistiques
      </h3>
      <div class="mt-4 grid gap-3 sm:grid-cols-2">
        <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
          <p class="text-xs font-semibold uppercase tracking-wider text-white/50">{{ t('dashboard.payments') }}</p>
          <div class="mt-2 flex items-baseline gap-2">
            <p class="text-3xl font-bold text-primary">{{ completionPercentage(paymentsForActive) }}%</p>
            <p class="text-xs text-white/60">complétion</p>
          </div>
          <div class="mt-3 h-2 overflow-hidden rounded-full bg-white/5">
            <div
              class="h-full rounded-full bg-gradient-to-r from-primary to-primarySoft transition-all duration-500"
              :style="{ width: `${completionPercentage(paymentsForActive)}%` }"
            />
          </div>
        </div>
        <div class="rounded-xl border border-white/10 bg-surface/40 p-4">
          <p class="text-xs font-semibold uppercase tracking-wider text-white/50">{{ t('dashboard.late') }}</p>
          <div class="mt-2 flex items-baseline gap-2">
            <p class="text-3xl font-bold" :class="countByStatut(paymentsForActive, 'retard') > 0 ? 'text-dangerSoft' : 'text-successSoft'">
              {{ countByStatut(paymentsForActive, 'retard') }}
            </p>
            <p class="text-xs text-white/60">membre(s)</p>
          </div>
        </div>
      </div>
    </section>

    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </section>

  <section v-else class="card flex flex-col items-center py-16 text-center">
    <div class="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-danger/15 text-dangerSoft ring-1 ring-inset ring-danger/30">
      <svg class="h-8 w-8" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
      </svg>
    </div>
    <h2 class="mb-2 text-xl font-semibold">Daret introuvable</h2>
    <p class="mb-6 max-w-sm text-sm text-white/60">
      Cette Daret n'existe pas ou a été supprimée. Consultez la liste de vos Darets ou rejoignez-en une avec un code.
    </p>
    <div class="flex flex-wrap justify-center gap-3">
      <RouterLink to="/mes-darets">
        <BaseButton variant="secondary">Mes Darets</BaseButton>
      </RouterLink>
      <RouterLink to="/daret/rejoindre">
        <BaseButton variant="primary">Rejoindre</BaseButton>
      </RouterLink>
    </div>
  </section>
</template>
