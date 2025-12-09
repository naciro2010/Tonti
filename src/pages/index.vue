<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import BaseButton from '@/components/BaseButton.vue';
import { useDaretStore } from '@/composables/useDaretStore';

const { t } = useI18n();
const store = useDaretStore();

const featured = computed(() => store.allDarets.value.slice(0, 3));

const scrollToDemo = () => {
  document.getElementById('demo')?.scrollIntoView({ behavior: 'smooth' });
};
</script>

<template>
  <div class="space-y-20 pb-12">
    <!-- Hero Section -->
    <section class="relative overflow-hidden">
      <div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-primary/5" />
      <div class="relative px-4 py-16 text-center sm:px-6 sm:py-24 lg:py-32">
        <h1 class="mx-auto max-w-4xl text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl">
          <span class="bg-gradient-to-r from-white via-white to-white/80 bg-clip-text text-transparent">
            {{ t('landing.hero.title') }}
          </span>
        </h1>
        <p class="mx-auto mt-6 max-w-2xl text-lg leading-8 text-white/70 sm:text-xl">
          {{ t('landing.hero.subtitle') }}
        </p>
        <div class="mt-10 flex flex-wrap items-center justify-center gap-4">
          <RouterLink to="/daret/creer">
            <BaseButton size="lg" class="shadow-lg shadow-primary/20 transition-all hover:shadow-xl hover:shadow-primary/30">
              {{ t('landing.hero.cta_primary') }}
            </BaseButton>
          </RouterLink>
          <BaseButton size="lg" variant="secondary" @click="scrollToDemo">
            {{ t('landing.hero.cta_secondary') }}
          </BaseButton>
        </div>

        <!-- Stats Section -->
        <div class="mx-auto mt-16 grid max-w-4xl grid-cols-2 gap-6 sm:gap-8 lg:grid-cols-4">
          <div class="rounded-2xl border border-white/10 bg-surface/50 p-6 backdrop-blur-sm">
            <div class="text-3xl font-bold text-primary sm:text-4xl">{{ t('landing.stats.stat1_value') }}</div>
            <div class="mt-2 text-sm text-white/60">{{ t('landing.stats.stat1_label') }}</div>
          </div>
          <div class="rounded-2xl border border-white/10 bg-surface/50 p-6 backdrop-blur-sm">
            <div class="text-3xl font-bold text-primary sm:text-4xl">{{ t('landing.stats.stat2_value') }}</div>
            <div class="mt-2 text-sm text-white/60">{{ t('landing.stats.stat2_label') }}</div>
          </div>
          <div class="rounded-2xl border border-white/10 bg-surface/50 p-6 backdrop-blur-sm">
            <div class="text-3xl font-bold text-primary sm:text-4xl">{{ t('landing.stats.stat3_value') }}</div>
            <div class="mt-2 text-sm text-white/60">{{ t('landing.stats.stat3_label') }}</div>
          </div>
          <div class="rounded-2xl border border-white/10 bg-surface/50 p-6 backdrop-blur-sm">
            <div class="text-3xl font-bold text-primary sm:text-4xl">{{ t('landing.stats.stat4_value') }}</div>
            <div class="mt-2 text-sm text-white/60">{{ t('landing.stats.stat4_label') }}</div>
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
          <article class="group card space-y-4 transition-all hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10">
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
          <article class="group card space-y-4 transition-all hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10">
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
          <article class="group card space-y-4 transition-all hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10">
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

          <!-- Feature 4: Easy Sharing -->
          <article class="group card space-y-4 transition-all hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature4_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature4_desc') }}</p>
          </article>

          <!-- Feature 5: Mobile-First -->
          <article class="group card space-y-4 transition-all hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10">
            <div class="inline-flex rounded-xl bg-primary/10 p-3">
              <svg class="h-8 w-8 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold">{{ t('landing.features.feature5_title') }}</h3>
            <p class="text-sm leading-relaxed text-white/70">{{ t('landing.features.feature5_desc') }}</p>
          </article>

          <!-- Feature 6: Open Source -->
          <article class="group card space-y-4 transition-all hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10">
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

    <!-- Demo Darets Section -->
    <section id="demo" class="px-4 sm:px-6">
      <div class="mx-auto max-w-7xl">
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 class="text-3xl font-bold">{{ t('landing.demo.title') }}</h2>
            <p class="mt-2 text-white/60">{{ t('app.tagline') }}</p>
          </div>
          <RouterLink to="/daret/creer">
            <BaseButton variant="secondary">{{ t('actions.create') }}</BaseButton>
          </RouterLink>
        </div>

        <div v-if="featured.length > 0" class="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <article v-for="daret in featured" :key="daret.id" class="card group space-y-4 transition-all hover:border-primary/50 hover:shadow-lg hover:shadow-primary/10">
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
            <div class="mt-8">
              <RouterLink to="/daret/creer">
                <BaseButton size="lg" class="shadow-lg shadow-primary/20 transition-all hover:shadow-xl hover:shadow-primary/30">
                  {{ t('landing.cta.button') }}
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
