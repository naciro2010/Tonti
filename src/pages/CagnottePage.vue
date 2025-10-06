<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import cagnottes from '../data/cagnottes.json';
import users from '../data/users.json';
import transactions from '../data/transactions.json';
import { formatCurrency, formatNumber } from '../utils/currency';
import { formatDate } from '../utils/date';
import GlassCard from '../components/ui/GlassCard.vue';
import BaseButton from '../components/ui/BaseButton.vue';
import ProgressBar from '../components/ui/ProgressBar.vue';
import ContributionModal from '../components/ContributionModal.vue';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const { locale, t } = useI18n();

const cagnotte = computed(() => cagnottes.find((item) => item.slug === route.params.slug));

const cagnotteTransactions = computed(() =>
  transactions
    .filter((transaction) => transaction.cagnotte === cagnotte.value?.slug)
    .map((transaction) => {
      const profile = users.find((user) => user.id === transaction.user);
      return {
        ...transaction,
        profile,
      };
    })
);

const reached = computed(() => {
  if (!cagnotte.value) return 0;
  return Math.round((cagnotte.value.raised / cagnotte.value.goal) * 100);
});

const isClosed = computed(() => {
  if (!cagnotte.value) return false;
  return new Date(cagnotte.value.deadline).getTime() < Date.now();
});

const showModal = ref(false);

const openModal = () => {
  if (!isClosed.value) {
    showModal.value = true;
  }
};

const closeModal = () => {
  showModal.value = false;
};
</script>

<template>
  <section v-if="cagnotte" class="mx-auto max-w-5xl space-y-8 px-4 py-16">
    <header class="space-y-4">
      <img :src="cagnotte.coverImage" :alt="cagnotte.title" class="h-60 w-full rounded-3xl object-cover" />
      <div class="space-y-2">
        <p class="text-xs uppercase tracking-wide text-white/40">{{ cagnotte.category }}</p>
        <h1 class="text-3xl font-semibold">{{ cagnotte.title }}</h1>
        <p class="text-sm text-white/60">Organisée par {{ cagnotte.organizer }}</p>
        <p class="text-sm text-white/70">{{ cagnotte.description }}</p>
      </div>
    </header>

    <GlassCard class="grid gap-8 md:grid-cols-[2fr,1fr]">
      <div class="space-y-6">
        <div class="space-y-3">
          <div class="flex items-center justify-between text-sm text-white/70">
            <span>{{ formatCurrency(cagnotte.raised, locale === 'ar' ? 'ar-MA' : 'fr-MA') }}</span>
            <span>{{ formatCurrency(cagnotte.goal, locale === 'ar' ? 'ar-MA' : 'fr-MA') }}</span>
          </div>
          <ProgressBar :value="cagnotte.raised" :max="cagnotte.goal" />
          <p class="text-xs text-white/50">{{ reached }}% {{ t('cagnotte.goalReached') }}</p>
        </div>
        <div class="flex flex-wrap items-center gap-4">
          <BaseButton :disabled="isClosed" size="lg" type="button" @click="openModal">
            {{ isClosed ? t('cagnotte.closed') : t('cagnotte.contributeCta') }}
          </BaseButton>
          <span class="text-xs text-white/60">
            Date limite : {{ formatDate(cagnotte.deadline, locale as 'fr' | 'ar') }}
          </span>
        </div>
      </div>
      <div class="space-y-3 rounded-2xl border border-white/10 bg-white/5 p-4">
        <h2 class="text-lg font-semibold">{{ t('cagnotte.summary') }}</h2>
        <p class="text-sm text-white/70">Visibilité : {{ cagnotte.visibility }}</p>
        <p class="text-sm text-white/70">Objectif : {{ formatCurrency(cagnotte.goal, locale === 'ar' ? 'ar-MA' : 'fr-MA') }}</p>
        <p class="text-sm text-white/70">Minimum : {{ formatCurrency(cagnotte.minimum, locale === 'ar' ? 'ar-MA' : 'fr-MA') }}</p>
        <p class="text-sm text-white/70">
          Participants : {{ formatNumber(cagnotteTransactions.length, locale === 'ar' ? 'ar-MA' : 'fr-MA') }}
        </p>
      </div>
    </GlassCard>

    <section class="space-y-4">
      <h2 class="text-xl font-semibold">{{ t('cagnotte.latestContributors') }}</h2>
      <div class="grid gap-4">
        <div
          v-for="transaction in cagnotteTransactions"
          :key="transaction.id"
          class="flex items-center justify-between rounded-2xl border border-white/5 bg-white/5 px-4 py-3"
        >
          <div class="flex items-center gap-3">
            <img
              v-if="transaction.profile && !transaction.anonymous"
              :src="transaction.profile.avatar"
              :alt="transaction.profile.name"
              class="h-10 w-10 rounded-full"
            />
            <div>
              <p class="text-sm font-semibold">
                {{ transaction.anonymous ? 'Anonyme' : transaction.profile?.name ?? 'Membre Tonti' }}
              </p>
              <p class="text-xs text-white/50">{{ formatDate(transaction.createdAt, locale as 'fr' | 'ar') }}</p>
            </div>
          </div>
          <p class="text-sm font-semibold">{{ formatCurrency(transaction.amount, locale === 'ar' ? 'ar-MA' : 'fr-MA') }}</p>
        </div>
      </div>
    </section>

    <ContributionModal
      :open="showModal"
      :quick-options="cagnotte.quickOptions"
      :minimum="cagnotte.minimum"
      @close="closeModal"
    />
  </section>

  <section v-else class="mx-auto max-w-3xl px-4 py-16 text-center">
    <h1 class="text-2xl font-semibold">Cagnotte introuvable</h1>
    <p class="mt-2 text-white/60">Cette page n’existe plus ou a été supprimée.</p>
  </section>
</template>
