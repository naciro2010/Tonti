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
        { id: 'payments', label: t('dashboard.payments') },
        { id: 'history', label: t('dashboard.history') },
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
          {{ t('actions.downloadIcs') }}
        </BaseButton>
        <BaseButton
          v-if="notifications.supported"
          type="button"
          variant="ghost"
          @click="toggleNotificationPermission"
        >
          {{ t('notifications.enable') }}
        </BaseButton>
      </div>
    </section>

    <section v-if="tabs === 'history'" class="card space-y-4">
      <h3 class="text-lg font-semibold">{{ t('dashboard.history') }}</h3>
      <ul v-if="daret.rounds.length" class="space-y-3">
        <li v-for="round in daret.rounds" :key="round.index" class="flex items-center justify-between gap-4">
          <div>
            <p class="font-semibold">{{ t('dashboard.round', { current: round.index, total: daret.rounds.length }) }}</p>
            <p class="text-xs text-white/60">{{ formatDate(round.dateDebut, locale) }} → {{ formatDate(round.dateFin, locale) }}</p>
          </div>
          <div class="text-right text-sm text-white/70">
            <p>{{ daret.membres.find((membre) => membre.id === round.receveurId)?.nom }}</p>
            <PaymentStatusBadge
              :statut="round.clos ? 'paye' : 'a_payer'"
            />
          </div>
        </li>
      </ul>
      <p v-else class="text-sm text-white/60">{{ t('dashboard.noHistory') }}</p>
    </section>

    <section class="card space-y-4">
      <h3 class="text-lg font-semibold">{{ t('dashboard.roster') }}</h3>
      <ul class="grid gap-2 sm:grid-cols-2">
        <li v-for="(memberId, index) in daret.roster" :key="memberId" class="rounded-lg bg-white/5 px-4 py-3">
          <p class="font-semibold">{{ index + 1 }}. {{ daret.membres.find((membre) => membre.id === memberId)?.nom }}</p>
        </li>
      </ul>
    </section>

    <InviteBanner v-if="inviteLink" :invitation-url="inviteLink" />

    <section class="card space-y-2">
      <h3 class="text-lg font-semibold">Stats</h3>
      <p class="text-sm text-white/70">
        {{ t('dashboard.payments') }} : {{ completionPercentage(paymentsForActive) }}% ·
        {{ t('dashboard.late') }} : {{ countByStatut(paymentsForActive, 'retard') }}
      </p>
    </section>

    <Toast :show="toast.show" :message="toast.message" :type="toast.type" />
  </section>

  <section v-else class="card">
    <p class="text-sm text-white/70">Daret introuvable.</p>
  </section>
</template>
