<script setup lang="ts">
import { RouterLink } from 'vue-router';
import GlassCard from '../components/ui/GlassCard.vue';
import BaseButton from '../components/ui/BaseButton.vue';
import ProgressBar from '../components/ui/ProgressBar.vue';
import { useI18n } from 'vue-i18n';
import cagnottes from '../data/cagnottes.json';
import { formatCurrency, formatNumber } from '../utils/currency';

const categories = [
  { id: 'urgence', label: 'Urgence' },
  { id: 'education', label: 'Éducation' },
  { id: 'sante', label: 'Santé' },
  { id: 'entrepreneuriat', label: 'Entrepreneuriat' },
];

const { t, locale } = useI18n();
</script>

<template>
  <section class="relative overflow-hidden">
    <div class="absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-amber-500/10" />
    <div class="relative mx-auto flex max-w-6xl flex-col gap-12 px-4 py-16 lg:flex-row lg:items-center">
      <div class="max-w-xl space-y-6">
        <span class="inline-flex items-center gap-2 rounded-full border border-primary/40 bg-primary/10 px-4 py-2 text-xs font-semibold text-primary">
          {{ t('home.complianceBadge') }}
        </span>
        <h1 class="text-4xl font-semibold sm:text-5xl">{{ t('hero.title') }}</h1>
        <p class="text-lg text-white/70">{{ t('hero.subtitle') }}</p>
        <div class="flex flex-wrap gap-4">
          <RouterLink to="/creer">
            <BaseButton size="lg">{{ t('hero.createCta') }}</BaseButton>
          </RouterLink>
          <RouterLink to="/comment-ca-marche">
            <BaseButton variant="ghost" size="lg">{{ t('hero.exploreCta') }}</BaseButton>
          </RouterLink>
        </div>
      </div>
      <div class="grid flex-1 gap-4 sm:grid-cols-2">
        <GlassCard class="space-y-2">
          <p class="text-sm text-white/70">{{ t('home.timeline.create') }}</p>
          <h3 class="text-2xl font-semibold">+{{ formatNumber(3200, locale as 'fr' | 'ar') }}</h3>
          <p class="text-xs text-white/50">Cagnottes créées sur la plateforme.</p>
        </GlassCard>
        <GlassCard class="space-y-2">
          <p class="text-sm text-white/70">{{ t('home.timeline.share') }}</p>
          <h3 class="text-2xl font-semibold">{{ formatCurrency(21500000, locale as 'fr' | 'ar') }}</h3>
          <p class="text-xs text-white/50">Montant total collecté depuis 2022.</p>
        </GlassCard>
        <GlassCard class="space-y-2">
          <p class="text-sm text-white/70">{{ t('home.timeline.contribute') }}</p>
          <h3 class="text-2xl font-semibold">+{{ formatNumber(180000, locale as 'fr' | 'ar') }}</h3>
          <p class="text-xs text-white/50">Contributeurs engagés dans la communauté.</p>
        </GlassCard>
        <GlassCard class="space-y-2">
          <p class="text-sm text-white/70">{{ t('home.timeline.follow') }}</p>
          <h3 class="text-2xl font-semibold">98%</h3>
          <p class="text-xs text-white/50">Taux de satisfaction moyen organisateurs.</p>
        </GlassCard>
      </div>
    </div>
  </section>

  <section class="mx-auto max-w-6xl px-4 py-16">
    <h2 class="text-2xl font-semibold">{{ t('home.categoriesTitle') }}</h2>
    <div class="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <GlassCard v-for="category in categories" :key="category.id" class="space-y-3">
        <h3 class="text-lg font-semibold">{{ category.label }}</h3>
        <p class="text-sm text-white/60">
          Des campagnes dédiées pour {{ category.label.toLowerCase() }} avec un suivi réglementaire.
        </p>
      </GlassCard>
    </div>
  </section>

  <section class="mx-auto max-w-6xl px-4 pb-20">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-semibold">{{ t('home.featuredTitle') }}</h2>
      <RouterLink to="/creer" class="text-sm text-primary">{{ t('navigation.create') }}</RouterLink>
    </div>
    <div class="mt-8 grid gap-6 lg:grid-cols-3">
      <GlassCard v-for="item in cagnottes" :key="item.slug" class="space-y-4">
        <img :src="item.coverImage" :alt="item.title" class="h-40 w-full rounded-2xl object-cover" />
        <div class="space-y-2">
          <p class="text-xs uppercase tracking-wide text-white/40">{{ item.category }}</p>
          <RouterLink :to="`/cagnotte/${item.slug}`" class="text-xl font-semibold hover:text-primary">
            {{ item.title }}
          </RouterLink>
          <p class="text-sm text-white/60">{{ item.description }}</p>
        </div>
        <div class="space-y-3">
          <div class="flex items-center justify-between text-sm text-white/70">
            <span>{{ formatCurrency(item.raised, locale as 'fr' | 'ar') }}</span>
            <span>{{ formatCurrency(item.goal, locale as 'fr' | 'ar') }}</span>
          </div>
          <ProgressBar :value="item.raised" :max="item.goal" />
          <div class="text-xs text-white/50">{{ item.organizer }}</div>
        </div>
        <RouterLink :to="`/cagnotte/${item.slug}`">
          <BaseButton variant="ghost" class="w-full">{{ t('cagnotte.contributeCta') }}</BaseButton>
        </RouterLink>
      </GlassCard>
    </div>
  </section>
</template>
