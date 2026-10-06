<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';

import { useAuthStore } from '@/composables/useAuthStore';
import { useNotifications } from '@/composables/useNotifications';
import { useRtl } from '@/composables/useRtl';
import { useToast } from '@/composables/useToast';
import { persistLocale } from '@/i18n';
import { isNative } from '@/services/platform';

const { t, locale } = useI18n();
const route = useRoute();
const router = useRouter();

const auth = useAuthStore();
const notifications = useNotifications();
const { toasts, dismiss } = useToast();
const { isRtl } = useRtl(locale);

const showUserMenu = ref(false);
const mobileMenuOpen = ref(false);

watch(
  locale,
  (value) => {
    persistLocale(value);
    document.documentElement.lang = value;
  },
  { immediate: true },
);

function toggleLocale() {
  locale.value = locale.value === 'fr' ? 'ar' : 'fr';
}

onMounted(() => {
  if (auth.isAuthenticated.value) {
    notifications.startPolling();
    void auth.refreshProfile().catch(() => undefined);
  }
});

watch(
  () => auth.isAuthenticated.value,
  (authenticated) => {
    if (authenticated) notifications.startPolling();
    else notifications.reset();
  },
);

watch(
  () => route.fullPath,
  () => {
    mobileMenuOpen.value = false;
    showUserMenu.value = false;
  },
);

async function handleLogout() {
  showUserMenu.value = false;
  await auth.logout();
  await router.push('/');
}

const navigation = computed(() =>
  auth.isAuthenticated.value
    ? [
        { name: t('nav.myDarets'), to: '/mes-darets' },
        { name: t('nav.create'), to: '/daret/creer' },
        { name: t('nav.join'), to: '/daret/rejoindre' },
      ]
    : [
        { name: t('nav.home'), to: '/' },
        { name: t('nav.join'), to: '/daret/rejoindre' },
      ],
);

/** Barre d'onglets mobile (utilisateur connecté), au plus près des conventions iOS. */
const tabs = computed(() => [
  {
    name: t('nav.myDarets'),
    to: '/mes-darets',
    icon: 'M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z',
  },
  { name: t('nav.create'), to: '/daret/creer', icon: 'M12 4.5v15m7.5-7.5h-15' },
  {
    name: t('nav.notifications'),
    to: '/notifications',
    icon: 'M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0',
    badge: true,
  },
  {
    name: t('nav.account'),
    to: '/compte',
    icon: 'M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z',
  },
]);

const showTabBar = computed(() => auth.isAuthenticated.value);
</script>

<template>
  <div
    :class="[
      'min-h-screen bg-background text-white',
      isRtl ? 'font-arabic' : 'font-sans',
      { 'is-native': isNative },
    ]"
  >
    <a
      href="#main"
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-xl focus:bg-primary focus:px-4 focus:py-2 focus:text-background"
    >
      {{ t('nav.skipToContent') }}
    </a>

    <header class="safe-top sticky top-0 z-40 border-b border-white/10 bg-background/80 backdrop-blur-md">
      <div class="container-responsive flex items-center justify-between gap-3 py-3.5">
        <RouterLink
          :to="auth.isAuthenticated.value ? '/mes-darets' : '/'"
          class="group inline-flex items-center gap-2 text-xl font-bold tracking-tight no-underline"
        >
          <span
            class="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/15 text-primary ring-1 ring-inset ring-primary/25 transition-all group-hover:bg-primary/25"
          >
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M6 6h12v3H6zM8 11h8v3H8zM10 16h4v3h-4z" />
            </svg>
          </span>
          <span class="text-white">{{ t('app.name') }}</span>
        </RouterLink>

        <nav class="hidden items-center gap-1 md:flex" :aria-label="t('nav.mainNav')">
          <RouterLink
            v-for="item in navigation"
            :key="item.to"
            :to="item.to"
            class="rounded-full px-3 py-1.5 text-sm font-medium text-white/70 no-underline transition-colors hover:text-white"
            :class="route.path === item.to ? 'bg-white/10 text-white' : ''"
          >
            {{ item.name }}
          </RouterLink>
        </nav>

        <div class="flex items-center gap-2">
          <button
            type="button"
            class="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-semibold uppercase tracking-wide text-white/80 transition-colors hover:border-white/20 hover:bg-white/10"
            :aria-label="`${t('app.language')} : ${locale === 'fr' ? 'العربية' : 'Français'}`"
            @click="toggleLocale"
          >
            {{ locale === 'fr' ? 'ع' : 'FR' }}
          </button>

          <template v-if="auth.isAuthenticated.value">
            <RouterLink
              to="/notifications"
              class="relative hidden rounded-full p-2 text-white/70 transition-colors hover:bg-white/10 hover:text-white md:inline-flex"
              :aria-label="t('nav.notifications')"
            >
              <svg
                class="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="1.5"
                aria-hidden="true"
              >
                <path stroke-linecap="round" stroke-linejoin="round" :d="tabs[2].icon" />
              </svg>
              <span
                v-if="notifications.unreadCount.value > 0"
                class="absolute -right-0.5 -top-0.5 flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold ring-2 ring-background"
              >
                {{ notifications.unreadCount.value > 99 ? '99+' : notifications.unreadCount.value }}
              </span>
            </RouterLink>

            <div class="relative hidden md:block">
              <button
                type="button"
                class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary ring-1 ring-inset ring-primary/30 transition-all hover:bg-primary/30"
                :aria-expanded="showUserMenu"
                :aria-label="t('nav.account')"
                @click="showUserMenu = !showUserMenu"
              >
                {{ auth.initials.value }}
              </button>

              <Transition
                enter-active-class="transition duration-150 ease-out"
                enter-from-class="scale-95 opacity-0"
                enter-to-class="scale-100 opacity-100"
                leave-active-class="transition duration-100 ease-in"
                leave-from-class="scale-100 opacity-100"
                leave-to-class="scale-95 opacity-0"
              >
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 z-50 mt-2 w-60 overflow-hidden rounded-2xl border border-white/10 bg-surface/95 shadow-2xl backdrop-blur-md rtl:left-0 rtl:right-auto"
                >
                  <div class="border-b border-white/10 px-4 py-3">
                    <p class="text-sm font-semibold text-white">{{ auth.fullName.value }}</p>
                    <p class="truncate text-xs text-white/50">{{ auth.user.value?.email }}</p>
                  </div>
                  <div class="py-1">
                    <RouterLink to="/compte" class="menu-item">{{ t('nav.account') }}</RouterLink>
                    <button type="button" class="menu-item text-dangerSoft" @click="handleLogout">
                      {{ t('auth.logout') }}
                    </button>
                  </div>
                </div>
              </Transition>
            </div>
          </template>

          <template v-else>
            <RouterLink
              to="/login"
              class="hidden rounded-full bg-primary px-4 py-1.5 text-sm font-semibold text-background no-underline shadow-glow transition-all hover:bg-primaryHover sm:inline-flex"
            >
              {{ t('auth.login') }}
            </RouterLink>
            <button
              type="button"
              class="inline-flex items-center justify-center rounded-lg p-2 text-white/80 hover:bg-white/10 md:hidden"
              :aria-expanded="mobileMenuOpen"
              :aria-label="mobileMenuOpen ? t('nav.closeMenu') : t('nav.openMenu')"
              @click="mobileMenuOpen = !mobileMenuOpen"
            >
              <svg
                class="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
                aria-hidden="true"
              >
                <path
                  v-if="!mobileMenuOpen"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M4 6h16M4 12h16M4 18h16"
                />
                <path v-else stroke-linecap="round" stroke-linejoin="round" d="M6 6l12 12M6 18L18 6" />
              </svg>
            </button>
          </template>
        </div>
      </div>

      <div v-if="mobileMenuOpen" class="border-t border-white/10 bg-background/95 backdrop-blur-md md:hidden">
        <nav class="container-responsive flex flex-col gap-1 py-3" :aria-label="t('nav.mainNav')">
          <RouterLink v-for="item in navigation" :key="item.to" :to="item.to" class="menu-item rounded-xl">
            {{ item.name }}
          </RouterLink>
          <RouterLink
            to="/login"
            class="mt-1 rounded-xl bg-primary px-4 py-2.5 text-center text-sm font-semibold text-background no-underline"
          >
            {{ t('auth.login') }}
          </RouterLink>
        </nav>
      </div>
    </header>

    <main id="main" class="container-responsive py-8" :class="showTabBar ? 'pb-28 md:pb-10' : ''">
      <RouterView v-slot="{ Component }">
        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="opacity-0 translate-y-1"
          enter-to-class="opacity-100 translate-y-0"
          mode="out-in"
        >
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>

    <footer
      v-if="!isNative"
      class="border-t border-white/10 bg-background/80"
      :class="showTabBar ? 'hidden md:block' : ''"
    >
      <div
        class="container-responsive flex flex-wrap items-center justify-between gap-3 py-6 text-sm text-white/60"
      >
        <span>&copy; {{ new Date().getFullYear() }} · {{ t('app.name') }}</span>
        <nav class="flex flex-wrap gap-4" :aria-label="t('legal.title')">
          <RouterLink to="/conditions" class="text-white/60 no-underline hover:text-white">{{
            t('legal.terms')
          }}</RouterLink>
          <RouterLink to="/confidentialite" class="text-white/60 no-underline hover:text-white">{{
            t('legal.privacy')
          }}</RouterLink>
          <RouterLink to="/support" class="text-white/60 no-underline hover:text-white">{{
            t('legal.support')
          }}</RouterLink>
        </nav>
      </div>
    </footer>

    <nav
      v-if="showTabBar"
      class="safe-bottom fixed inset-x-0 bottom-0 z-40 border-t border-white/10 bg-background/90 backdrop-blur-md md:hidden"
      :aria-label="t('nav.mainNav')"
    >
      <ul class="grid grid-cols-4">
        <li v-for="tab in tabs" :key="tab.to">
          <RouterLink
            :to="tab.to"
            class="relative flex flex-col items-center gap-1 py-2.5 text-[11px] font-medium no-underline transition-colors"
            :class="route.path.startsWith(tab.to) ? 'text-primary' : 'text-white/60'"
            :aria-current="route.path.startsWith(tab.to) ? 'page' : undefined"
          >
            <svg
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="1.6"
              aria-hidden="true"
            >
              <path stroke-linecap="round" stroke-linejoin="round" :d="tab.icon" />
            </svg>
            {{ tab.name }}
            <span
              v-if="tab.badge && notifications.unreadCount.value > 0"
              class="absolute top-1.5 flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold text-white ltr:left-1/2 ltr:ml-1.5 rtl:right-1/2 rtl:mr-1.5"
            >
              {{ notifications.unreadCount.value > 9 ? '9+' : notifications.unreadCount.value }}
            </span>
          </RouterLink>
        </li>
      </ul>
    </nav>

    <Teleport to="body">
      <div
        class="pointer-events-none fixed inset-x-3 z-50 flex flex-col items-center gap-2 sm:inset-x-auto sm:right-6 sm:items-end"
        :class="showTabBar ? 'bottom-24 md:bottom-6' : 'bottom-4 sm:bottom-6'"
        aria-live="polite"
      >
        <TransitionGroup
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="translate-y-2 opacity-0"
          enter-to-class="translate-y-0 opacity-100"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="opacity-100"
          leave-to-class="opacity-0"
        >
          <div
            v-for="toast in toasts"
            :key="toast.id"
            class="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl border border-white/10 px-4 py-3 text-sm font-medium shadow-2xl backdrop-blur-md"
            :class="
              toast.type === 'success'
                ? 'bg-success/90'
                : toast.type === 'error'
                  ? 'bg-danger/90'
                  : 'bg-surface/95'
            "
            :role="toast.type === 'error' ? 'alert' : 'status'"
          >
            <span class="flex-1 leading-relaxed">{{ toast.message }}</span>
            <button
              type="button"
              class="-m-1 rounded-full p-1 opacity-70 transition-opacity hover:opacity-100"
              :aria-label="t('common.close')"
              @click="dismiss(toast.id)"
            >
              <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path
                  d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z"
                />
              </svg>
            </button>
          </div>
        </TransitionGroup>
      </div>
    </Teleport>

    <div v-if="showUserMenu" class="fixed inset-0 z-30" aria-hidden="true" @click="showUserMenu = false" />
  </div>
</template>
