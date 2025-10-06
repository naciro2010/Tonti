<script setup lang="ts">
import { RouterLink } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { computed, ref } from 'vue';
import BaseButton from '../ui/BaseButton.vue';
import LanguageToggle from './LanguageToggle.vue';
import { useLoginModal } from '../../composables/useLoginModal';

const { t } = useI18n();
const { open } = useLoginModal();
const mobileOpen = ref(false);

const navigation = computed(() => [
  { label: t('navigation.home'), to: '/' },
  { label: t('navigation.howItWorks'), to: '/comment-ca-marche' },
  { label: t('navigation.faq'), to: '/faq' },
  { label: t('navigation.help'), to: '/aide' },
]);
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-white/10 bg-background/80 backdrop-blur">
    <nav class="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
      <RouterLink to="/" class="flex items-center gap-2 text-lg font-semibold text-white">
        <span class="flex h-10 w-10 items-center justify-center rounded-2xl bg-primary text-black">T</span>
        <span>Tonti</span>
      </RouterLink>
      <div class="hidden items-center gap-8 md:flex">
        <RouterLink
          v-for="item in navigation"
          :key="item.to"
          :to="item.to"
          class="text-sm font-medium text-white/70 transition hover:text-white"
          active-class="text-primary"
        >
          {{ item.label }}
        </RouterLink>
        <RouterLink to="/creer" class="text-sm font-medium text-white/70 transition hover:text-white">
          {{ t('navigation.create') }}
        </RouterLink>
      </div>
      <div class="hidden items-center gap-3 md:flex">
        <LanguageToggle />
        <BaseButton variant="outline" size="sm" type="button" @click="open">
          {{ t('navigation.login') }}
        </BaseButton>
      </div>
      <button class="md:hidden" type="button" @click="mobileOpen = !mobileOpen">
        <span class="sr-only">Toggle menu</span>
        <svg class="h-6 w-6 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
    </nav>
    <div v-if="mobileOpen" class="border-t border-white/10 bg-background/95 px-4 py-4 md:hidden">
      <div class="flex flex-col gap-4">
        <RouterLink
          v-for="item in navigation"
          :key="item.to"
          :to="item.to"
          class="text-sm font-medium text-white/80"
          @click="mobileOpen = false"
        >
          {{ item.label }}
        </RouterLink>
        <RouterLink to="/creer" class="text-sm font-medium text-white/80" @click="mobileOpen = false">
          {{ t('navigation.create') }}
        </RouterLink>
        <LanguageToggle />
        <BaseButton variant="outline" size="sm" type="button" @click="open">{{ t('navigation.login') }}</BaseButton>
      </div>
    </div>
  </header>
</template>
