<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import BaseButton from '@/components/BaseButton.vue';
import { useDaretStore } from '@/composables/useDaretStore';
import { useAuthStore } from '@/composables/useAuthStore';

const { t } = useI18n();
const store = useDaretStore();
const auth = useAuthStore();

const featured = computed(() => store.allDarets.value.slice(0, 3));
const createLink = computed(() => auth.isAuthenticated.value ? '/daret/creer' : '/inscription');

const scrollToDemo = () => {
  document.getElementById('demo')?.scrollIntoView({ behavior: 'smooth' });
};
</script>

<template>
  <div class="space-y-20 pb-12">
    <!-- Hero Section -->
    <section class="relative overflow-hidden">
      <div class="pointer-events-none absolute inset-0 bg-hero-radial" aria-hidden="true" />
      <div
        class="pointer-events-none absolute -top-24 left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full bg-primary/20 blur-3xl"
        aria-hidden="true"
      />
      <div class="relative px-4 py-16 text-center sm:px-6 sm:py-24 lg:py-28">
        <div
          class="mx-auto inline-flex animate-fade-in items-center gap-2 rounded-full border border-primary/30 bg-primary/10 px-3 py-1 text-xs font-semibold uppercase tracking-wider text-primary"
        >
          <span class="relative flex h-2 w-2">
            <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-70" />
            <span class="relative inline-flex h-2 w-2 rounded-full bg-primary" />
          </span>
          {{ t('app.tagline') }}
        </div>
        <h1 class="mx-auto mt-6 max-w-4xl animate-fade-in-up text-balance text-4xl font-bold leading-tight tracking-tight sm:text-5xl lg:text-6xl">
          <span class="bg-gradient-to-br from-white via-white to-white/60 bg-clip-text text-transparent">
            {{ t('landing.hero.title') }}
          </span>
        </h1>
        <p
          class="mx-auto mt-6 max-w-2xl animate-fade-in-up text-pretty text-lg leading-8 text-white/70 sm:text-xl"
          style="animation-delay: 80ms"
        >
          {{ t('landing.hero.subtitle') }}
        </p>
        <div
          class="mt-10 flex animate-fade-in-up flex-col items-stretch justify-center gap-3 sm:flex-row sm:items-center sm:gap-4"
          style="animation-delay: 160ms"
        >
          <RouterLink :to="createLink">
            <BaseButton size="lg" block>
              {{ t('landing.hero.cta_primary') }}
              <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path
                  fill-rule="evenodd"
                  d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z"
                  clip-rule="evenodd"
                />
              </svg>
            </BaseButton>
          </RouterLink>
          <BaseButton size="lg" variant="secondary" block @click="scrollToDemo">
            {{ t('landing.hero.cta_secondary') }}
          </BaseButton>
        </div>

        <!-- Stats Section -->
        <div
          class="mx-auto mt-16 grid max-w-4xl animate-fade-in-up grid-cols-2 gap-4 sm:gap-6 lg:grid-cols-4"
          style="animation-delay: 240ms"
        >
          <div
            v-for="n in 4"
            :key="n"
            class="group rounded-2xl border border-white/10 bg-surface/50 p-5 backdrop-blur-sm transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/40 hover:bg-surface/70 hover:shadow-glow sm:p-6"
          >
            <div class="gradient-text text-3xl font-bold sm:text-4xl">
              {{ t(`landing.stats.stat${n}_value`) }}
            </div>
            <div class="mt-2 text-sm text-white/60">{{ t(`landing.stats.stat${n}_label`) }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="px-4 sm:px-6">
      <div class="mx-auto max-w-7xl">
        <div class="text-center">
          <h2 class="text-3xl font-bold sm:text-4xl">{{ t('landing.features.title') }}</h2>
          <p class="mt-4 text-lg text-white/70">{{ t('landing.features.subtitle') }}</p>
        </div>

        <div class="mt-16 grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          <!-- Feature 1: Secure & Local -->
          <article class="group card-interactive space-y-4">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature1_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature1_desc') }}</p>
          </article>

          <!-- Feature 2: Multilingual -->
          <article class="group card-interactive space-y-4">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature2_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature2_desc') }}</p>
          </article>

          <!-- Feature 3: Simple & Efficient -->
          <article class="group card-interactive space-y-4">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature3_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature3_desc') }}</p>
          </article>

          <!-- Feature 4: Payment Integration -->
          <article class="group card-interactive space-y-4">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature4_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature4_desc') }}</p>
          </article>

          <!-- Feature 5: Access Management -->
          <article class="group card-interactive space-y-4">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature5_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature5_desc') }}</p>
          </article>

          <!-- Feature 6: Open Source -->
          <article class="group card-interactive space-y-4">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature6_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature6_desc') }}</p>
          </article>
        </div>
      </div>
    </section>

    <!-- How It Works Section -->
    <section class="px-4 sm:px-6">
      <div class="mx-auto max-w-7xl">
        <div class="text-center">
          <h2 class="text-3xl font-bold sm:text-4xl">{{ t('landing.howItWorks.title') }}</h2>
          <p class="mt-4 text-lg text-white/70">{{ t('landing.howItWorks.subtitle') }}</p>
        </div>

        <div class="mt-16 grid gap-8 lg:grid-cols-3">
          <!-- Step 1 -->
          <div class="relative">
            <div class="card space-y-4 text-center">
              <div class="mx-auto inline-flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primaryHover text-2xl font-bold text-background">
                1
              </div>
              <h3 class="text-xl font-semibold">{{ t('landing.howItWorks.step1_title') }}</h3>
              <p class="text-sm leading-relaxed text-white/70">{{ t('landing.howItWorks.step1_desc') }}</p>
            </div>
            <!-- Connector Arrow (hidden on mobile) -->
            <div class="absolute right-0 top-1/2 hidden -translate-y-1/2 translate-x-1/2 lg:block">
              <svg class="h-8 w-8 text-primary/30" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </div>
          </div>

          <!-- Step 2 -->
          <div class="relative">
            <div class="card space-y-4 text-center">
              <div class="mx-auto inline-flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primaryHover text-2xl font-bold text-background">
                2
              </div>
              <h3 class="text-xl font-semibold">{{ t('landing.howItWorks.step2_title') }}</h3>
              <p class="text-sm leading-relaxed text-white/70">{{ t('landing.howItWorks.step2_desc') }}</p>
            </div>
            <!-- Connector Arrow (hidden on mobile) -->
            <div class="absolute right-0 top-1/2 hidden -translate-y-1/2 translate-x-1/2 lg:block">
              <svg class="h-8 w-8 text-primary/30" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </div>
          </div>

          <!-- Step 3 -->
          <div class="card space-y-4 text-center">
            <div class="mx-auto inline-flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primaryHover text-2xl font-bold text-background">
              3
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.howItWorks.step3_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.howItWorks.step3_desc') }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Payment Methods Section -->
    <section class="px-4 sm:px-6">
      <div class="mx-auto max-w-7xl">
        <div class="text-center">
          <h2 class="text-3xl font-bold sm:text-4xl">{{ t('landing.payments.title') }}</h2>
          <p class="mt-4 text-lg text-white/70">{{ t('landing.payments.subtitle') }}</p>
        </div>

        <div class="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <!-- Payment Method 1: Cards -->
          <article class="card space-y-3 text-center">
            <div class="text-4xl">💳</div>
            <h3 class="text-lg font-semibold">{{ t('landing.payments.method1_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.payments.method1_desc') }}</p>
          </article>

          <!-- Payment Method 2: PayPal -->
          <article class="card space-y-3 text-center">
            <div class="text-4xl">🅿️</div>
            <h3 class="text-lg font-semibold">{{ t('landing.payments.method2_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.payments.method2_desc') }}</p>
          </article>

          <!-- Payment Method 3: Crypto -->
          <article class="card space-y-3 text-center">
            <div class="text-4xl">₿</div>
            <h3 class="text-lg font-semibold">{{ t('landing.payments.method3_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.payments.method3_desc') }}</p>
          </article>

          <!-- Payment Method 4: Mobile Money -->
          <article class="card space-y-3 text-center">
            <div class="text-4xl">📱</div>
            <h3 class="text-lg font-semibold">{{ t('landing.payments.method4_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.payments.method4_desc') }}</p>
          </article>

          <!-- Payment Method 5: Bank Transfer -->
          <article class="card space-y-3 text-center">
            <div class="text-4xl">🏦</div>
            <h3 class="text-lg font-semibold">{{ t('landing.payments.method5_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.payments.method5_desc') }}</p>
          </article>

          <!-- Payment Method 6: Cash -->
          <article class="card space-y-3 text-center">
            <div class="text-4xl">💵</div>
            <h3 class="text-lg font-semibold">{{ t('landing.payments.method6_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.payments.method6_desc') }}</p>
          </article>
        </div>
      </div>
    </section>

    <!-- Security & Access Control Section -->
    <section class="px-4 sm:px-6">
      <div class="mx-auto max-w-7xl">
        <div class="text-center">
          <h2 class="text-3xl font-bold sm:text-4xl">{{ t('landing.security.title') }}</h2>
          <p class="mt-4 text-lg text-white/70">{{ t('landing.security.subtitle') }}</p>
        </div>

        <div class="mt-16 grid gap-8 lg:grid-cols-3">
          <!-- Role 1: Creator -->
          <article class="card space-y-4">
            <div class="flex items-center gap-3">
              <div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary/20 text-2xl">
                👑
              </div>
              <h3 class="text-xl font-semibold">{{ t('landing.security.role1_title') }}</h3>
            </div>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.security.role1_desc') }}</p>
            <ul class="space-y-2 text-sm text-white/60">
              <li class="flex items-start gap-2">
                <span class="text-success">✓</span>
                <span>Modifier les informations</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-success">✓</span>
                <span>Gérer les membres</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-success">✓</span>
                <span>Clôturer les rounds</span>
              </li>
            </ul>
          </article>

          <!-- Role 2: Member -->
          <article class="card space-y-4">
            <div class="flex items-center gap-3">
              <div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary/20 text-2xl">
                👤
              </div>
              <h3 class="text-xl font-semibold">{{ t('landing.security.role2_title') }}</h3>
            </div>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.security.role2_desc') }}</p>
            <ul class="space-y-2 text-sm text-white/60">
              <li class="flex items-start gap-2">
                <span class="text-success">✓</span>
                <span>Voir la Daret</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-success">✓</span>
                <span>Marquer paiements</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-success">✓</span>
                <span>Recevoir notifications</span>
              </li>
            </ul>
          </article>

          <!-- Role 3: Viewer -->
          <article class="card space-y-4">
            <div class="flex items-center gap-3">
              <div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary/20 text-2xl">
                👁️
              </div>
              <h3 class="text-xl font-semibold">{{ t('landing.security.role3_title') }}</h3>
            </div>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.security.role3_desc') }}</p>
            <ul class="space-y-2 text-sm text-white/60">
              <li class="flex items-start gap-2">
                <span class="text-success">✓</span>
                <span>Accès en lecture seule</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-danger">✗</span>
                <span>Pas de modification</span>
              </li>
              <li class="flex items-start gap-2">
                <span class="text-warning">∼</span>
                <span>Suivre la progression</span>
              </li>
            </ul>
          </article>
        </div>
      </div>
    </section>

    <!-- Demo Darets Section -->
    <section id="demo" class="px-4 sm:px-6">
      <div class="mx-auto max-w-7xl">
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 class="text-3xl font-bold">{{ t('landing.demo.title') }}</h2>
            <p class="mt-2 text-white/60">{{ t('app.tagline') }}</p>
          </div>
          <RouterLink :to="createLink">
            <BaseButton variant="secondary">{{ t('actions.create') }}</BaseButton>
          </RouterLink>
        </div>

        <div v-if="featured.length > 0" class="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <article
            v-for="daret in featured"
            :key="daret.id"
            class="card-interactive group space-y-4"
          >
            <div class="flex items-start justify-between">
              <h3 class="text-xl font-semibold">{{ daret.nom }}</h3>
              <span class="rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
                {{ daret.etat }}
              </span>
            </div>
            <p class="text-sm text-white/70">{{ daret.description }}</p>
            <div class="flex items-center gap-4 text-sm text-white/60">
              <span class="flex items-center gap-1">
                <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
                {{ daret.taille }} {{ t('landing.demo.members') }}
              </span>
              <span class="font-medium text-primary">{{ daret.devise }}</span>
            </div>
            <RouterLink
              :to="`/daret/${daret.id}`"
              class="inline-flex items-center gap-2 text-sm font-medium text-primary transition-all hover:gap-3 hover:underline"
            >
              {{ t('landing.demo.viewDashboard') }}
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </RouterLink>
          </article>
        </div>
      </div>
    </section>

    <!-- Final CTA Section -->
    <section class="px-4 sm:px-6">
      <div class="mx-auto max-w-4xl">
        <div class="relative overflow-hidden rounded-3xl bg-gradient-to-br from-primary via-primaryHover to-primary p-1">
          <div class="rounded-3xl bg-background p-8 text-center sm:p-12">
            <h2 class="text-3xl font-bold sm:text-4xl">{{ t('landing.cta.title') }}</h2>
            <p class="mt-4 text-lg text-white/70">{{ t('landing.cta.subtitle') }}</p>
            <div class="mt-8 flex justify-center">
              <RouterLink :to="createLink">
                <BaseButton size="lg">
                  {{ t('landing.cta.button') }}
                  <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                    <path
                      fill-rule="evenodd"
                      d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </BaseButton>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="border-t border-white/10 px-4 pt-12 sm:px-6">
      <div class="mx-auto max-w-7xl">
        <div class="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
          <!-- Brand -->
          <div class="space-y-4">
            <h3 class="text-xl font-bold text-primary">{{ t('app.name') }}</h3>
            <p class="text-sm text-white/60">{{ t('landing.footer.description') }}</p>
          </div>

          <!-- Links -->
          <div class="space-y-4">
            <h4 class="font-semibold text-white/90">{{ t('landing.footer.links_title') }}</h4>
            <ul class="space-y-2 text-sm">
              <li>
                <RouterLink to="/" class="text-white/60 transition-colors hover:text-primary">
                  {{ t('landing.footer.home') }}
                </RouterLink>
              </li>
              <li>
                <a href="#demo" class="text-white/60 transition-colors hover:text-primary" @click.prevent="scrollToDemo">
                  {{ t('landing.footer.about') }}
                </a>
              </li>
              <li>
                <RouterLink to="/daret/creer" class="text-white/60 transition-colors hover:text-primary">
                  {{ t('nav.create') }}
                </RouterLink>
              </li>
              <li>
                <RouterLink to="/daret/rejoindre" class="text-white/60 transition-colors hover:text-primary">
                  {{ t('nav.join') }}
                </RouterLink>
              </li>
            </ul>
          </div>

          <!-- Resources -->
          <div class="space-y-4">
            <h4 class="font-semibold text-white/90">{{ t('landing.footer.resources_title') }}</h4>
            <ul class="space-y-2 text-sm">
              <li>
                <a
                  href="https://github.com/naciro2010/Tonti"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="text-white/60 transition-colors hover:text-primary"
                >
                  {{ t('landing.footer.github') }}
                </a>
              </li>
              <li>
                <span class="text-white/60">{{ t('landing.footer.documentation') }}</span>
              </li>
            </ul>
          </div>

          <!-- Legal -->
          <div class="space-y-4">
            <h4 class="font-semibold text-white/90">{{ t('landing.footer.legal_title') }}</h4>
            <ul class="space-y-2 text-sm">
              <li>
                <a
                  href="https://opensource.org/licenses/MIT"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="text-white/60 transition-colors hover:text-primary"
                >
                  {{ t('landing.footer.license') }}
                </a>
              </li>
              <li>
                <span class="text-white/60">{{ t('landing.footer.privacy') }}</span>
              </li>
              <li>
                <span class="text-white/60">{{ t('landing.footer.terms') }}</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Copyright -->
        <div class="mt-12 border-t border-white/10 py-8 text-center text-sm text-white/50">
          <p>
            {{ t('landing.footer.madeWith') }} ❤️ {{ t('landing.footer.by') }}
            <a
              href="https://github.com/naciro2010"
              target="_blank"
              rel="noopener noreferrer"
              class="font-medium text-primary transition-colors hover:text-primaryHover"
            >
              naciro2010
            </a>
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>
