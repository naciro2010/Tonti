<script setup lang="ts">
import { computed, watch, onMounted, ref } from 'vue';
import { useRoute, useRouter, RouterLink, RouterView } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useStorage } from '@vueuse/core';

import { persistLocale } from '@/i18n';
import { useRtl } from '@/composables/useRtl';
import { useAuthStore } from '@/composables/useAuthStore';
import { useNotifications } from '@/composables/useNotifications';
import { useToast } from '@/composables/useToast';

const { t, locale } = useI18n();
const route = useRoute();
const router = useRouter();

const storedLocale = useStorage('tonti:locale', locale.value as string);
const auth = useAuthStore();
const notifications = useNotifications();
const { toasts, dismiss } = useToast();
const showUserMenu = ref(false);
const mobileMenuOpen = ref(false);

watch(
  storedLocale,
  (value) => {
    locale.value = value;
    persistLocale(value);
  },
  { immediate: true },
);

const { isRtl } = useRtl(locale);

function toggleLocale() {
  storedLocale.value = storedLocale.value === 'fr' ? 'ar' : 'fr';
}

onMounted(() => {
  if (auth.isAuthenticated.value) {
    notifications.startPolling();
  }
});

watch(() => auth.isAuthenticated.value, (authenticated) => {
  if (authenticated) {
    notifications.startPolling();
  } else {
    notifications.reset();
  }
});

watch(() => route.path, () => {
  mobileMenuOpen.value = false;
  showUserMenu.value = false;
});

async function handleLogout() {
  showUserMenu.value = false;
  await auth.logout();
  router.push('/');
}

const navigation = computed(() => {
  if (auth.isAuthenticated.value) {
    return [
      { name: t('nav.home'), to: '/' },
      { name: 'Mes Darets', to: '/mes-darets' },
      { name: t('nav.create'), to: '/daret/creer' },
    ];
  }
  return [
    { name: t('nav.home'), to: '/' },
    { name: t('nav.create'), to: '/daret/creer' },
    { name: t('nav.join'), to: '/daret/rejoindre' },
  ];
});
</script>

<template>
  <div :class="['min-h-screen bg-background text-white', isRtl ? 'font-arabic' : 'font-sans']">
    <a
      href="#main"
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-xl focus:bg-primary focus:px-4 focus:py-2 focus:text-background focus:shadow-glow"
    >
      Aller au contenu
    </a>

    <header
      class="sticky top-0 z-40 border-b border-white/10 bg-background/80 backdrop-blur-md supports-backdrop:bg-background/60"
    >
      <div class="container-responsive flex items-center justify-between gap-3 py-3.5">
        <RouterLink
          to="/"
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

        <nav class="hidden items-center gap-1 md:flex" aria-label="Navigation principale">
          <RouterLink
            v-for="item in navigation"
            :key="item.to"
            :to="item.to"
            class="rounded-full px-3 py-1.5 text-sm font-medium text-white/70 transition-colors hover:text-white no-underline"
            :class="route.path === item.to ? 'bg-white/10 text-white' : ''"
          >
            {{ item.name }}
          </RouterLink>
        </nav>

        <div class="flex items-center gap-2">
          <button
            type="button"
            class="hidden items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-semibold uppercase tracking-wide text-white/80 transition-colors hover:border-white/20 hover:bg-white/10 sm:inline-flex"
            @click="toggleLocale"
            :aria-label="`${t('app.language')}: ${locale}`"
          >
            <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3 5h12M9 3v2m1 9.5A18 18 0 016 9m6 9h7M11 21l5-10 5 10" />
            </svg>
            {{ locale }}
          </button>

          <template v-if="auth.isAuthenticated.value">
            <button
              type="button"
              class="relative rounded-full p-2 text-white/70 transition-colors hover:bg-white/10 hover:text-white"
              @click="router.push('/mes-darets')"
              title="Notifications"
              aria-label="Notifications"
            >
              <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
              </svg>
              <span
                v-if="notifications.unreadCount.value > 0"
                class="absolute -right-0.5 -top-0.5 flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold ring-2 ring-background"
              >
                {{ notifications.unreadCount.value > 99 ? '99+' : notifications.unreadCount.value }}
              </span>
            </button>

            <div class="relative">
              <button
                type="button"
                class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary ring-1 ring-inset ring-primary/30 transition-all hover:bg-primary/30 hover:scale-105"
                @click="showUserMenu = !showUserMenu"
                :aria-expanded="showUserMenu"
                aria-label="Menu utilisateur"
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
                  @click="showUserMenu = false"
                >
                  <div class="border-b border-white/10 px-4 py-3">
                    <p class="text-sm font-semibold text-white">{{ auth.fullName.value }}</p>
                    <p class="truncate text-xs text-white/50">{{ auth.user.value?.email }}</p>
                  </div>
                  <div class="py-1">
                    <RouterLink
                      to="/mes-darets"
                      class="block px-4 py-2 text-sm text-white/80 no-underline transition-colors hover:bg-white/5 hover:text-white"
                    >
                      Mes Darets
                    </RouterLink>
                    <button
                      type="button"
                      class="block w-full px-4 py-2 text-left text-sm font-medium text-dangerSoft transition-colors hover:bg-danger/10"
                      @click="handleLogout"
                    >
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
              class="hidden rounded-full bg-primary px-4 py-1.5 text-sm font-semibold text-background shadow-glow transition-all hover:bg-primaryHover hover:shadow-glow-lg sm:inline-flex no-underline"
            >
              {{ t('auth.login') }}
            </RouterLink>
          </template>

          <button
            type="button"
            class="inline-flex items-center justify-center rounded-lg p-2 text-white/80 hover:bg-white/10 md:hidden"
            @click="mobileMenuOpen = !mobileMenuOpen"
            :aria-expanded="mobileMenuOpen"
            aria-label="Ouvrir le menu"
          >
            <svg v-if="!mobileMenuOpen" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
            <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l12 12M6 18L18 6" />
            </svg>
          </button>
        </div>
      </div>

      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0 -translate-y-2"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0 -translate-y-2"
      >
        <div v-if="mobileMenuOpen" class="border-t border-white/10 bg-background/95 backdrop-blur-md md:hidden">
          <nav class="container-responsive flex flex-col gap-1 py-3" aria-label="Navigation mobile">
            <RouterLink
              v-for="item in navigation"
              :key="item.to"
              :to="item.to"
              class="rounded-xl px-4 py-2.5 text-sm font-medium text-white/80 no-underline transition-colors hover:bg-white/10"
              :class="route.path === item.to ? 'bg-white/10 text-white' : ''"
            >
              {{ item.name }}
            </RouterLink>
            <button
              type="button"
              class="flex items-center justify-between rounded-xl px-4 py-2.5 text-sm font-medium text-white/80 transition-colors hover:bg-white/10"
              @click="toggleLocale"
            >
              <span>{{ t('app.language') }}</span>
              <span class="text-xs uppercase tracking-wide text-primary">{{ locale }}</span>
            </button>
            <RouterLink
              v-if="!auth.isAuthenticated.value"
              to="/login"
              class="mt-1 rounded-xl bg-primary px-4 py-2.5 text-center text-sm font-semibold text-background no-underline shadow-glow"
            >
              {{ t('auth.login') }}
            </RouterLink>
          </nav>
        </div>
      </Transition>
    </header>

    <main id="main" class="container-responsive py-10">
      <RouterView v-slot="{ Component }">
        <Transition
          enter-active-class="transition duration-300 ease-out"
          enter-from-class="opacity-0 translate-y-2"
          enter-to-class="opacity-100 translate-y-0"
          mode="out-in"
        >
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>

    <footer class="border-t border-white/10 bg-background/80">
      <div class="container-responsive py-6 text-sm text-white/60">
        &copy; {{ new Date().getFullYear() }} &middot; {{ t('app.name') }}
      </div>
    </footer>

    <Teleport to="body">
      <div
        class="pointer-events-none fixed inset-x-3 bottom-4 z-50 flex flex-col items-center gap-2 sm:inset-x-auto sm:bottom-6 sm:right-6 sm:items-end"
      >
        <TransitionGroup
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="translate-y-2 opacity-0"
          enter-to-class="translate-y-0 opacity-100"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="opacity-100"
          leave-to-class="translate-x-4 opacity-0"
        >
          <div
            v-for="toast in toasts"
            :key="toast.id"
            class="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl border border-white/10 px-4 py-3 text-sm font-medium shadow-2xl backdrop-blur-md"
            :class="[
              toast.type === 'success'
                ? 'bg-success/90 text-white'
                : toast.type === 'error'
                  ? 'bg-danger/90 text-white'
                  : 'bg-surface/95 text-white',
            ]"
            role="status"
          >
            <svg
              v-if="toast.type === 'success'"
              class="mt-0.5 h-4 w-4 flex-shrink-0"
              viewBox="0 0 20 20"
              fill="currentColor"
              aria-hidden="true"
            >
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clip-rule="evenodd"
              />
            </svg>
            <svg
              v-else-if="toast.type === 'error'"
              class="mt-0.5 h-4 w-4 flex-shrink-0"
              viewBox="0 0 20 20"
              fill="currentColor"
              aria-hidden="true"
            >
              <path
                fill-rule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                clip-rule="evenodd"
              />
            </svg>
            <span class="flex-1 leading-relaxed">{{ toast.message }}</span>
            <button
              type="button"
              class="-m-1 rounded-full p-1 opacity-70 transition-opacity hover:opacity-100"
              aria-label="Fermer"
              @click="dismiss(toast.id)"
            >
              <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
              </svg>
            </button>
          </div>
        </TransitionGroup>
      </div>
    </Teleport>

    <div v-if="showUserMenu" class="fixed inset-0 z-40" @click="showUserMenu = false" />
  </div>
</template>
