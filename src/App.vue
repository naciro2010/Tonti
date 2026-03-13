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
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-xl focus:bg-primary focus:px-4 focus:py-2 focus:text-background"
    >
      Aller au contenu
    </a>
    <header class="border-b border-white/10 bg-background/80 backdrop-blur supports-backdrop:bg-background/60">
      <div class="container-responsive flex flex-wrap items-center justify-between gap-4 py-4">
        <RouterLink to="/" class="text-xl font-semibold">{{ t('app.name') }}</RouterLink>
        <nav class="flex flex-wrap items-center gap-3">
          <RouterLink
            v-for="item in navigation"
            :key="item.to"
            :to="item.to"
            class="rounded-full px-3 py-1 text-sm font-medium text-white/70 transition hover:text-white"
            :class="route.path === item.to ? 'bg-white/10 text-white' : ''"
          >
            {{ item.name }}
          </RouterLink>

          <!-- Language toggle -->
          <button type="button" class="rounded-full border border-white/20 px-3 py-1 text-sm" @click="toggleLocale">
            {{ t('app.language') }}: {{ locale }}
          </button>

          <!-- Authenticated user controls -->
          <template v-if="auth.isAuthenticated.value">
            <!-- Notification bell -->
            <button
              type="button"
              class="relative rounded-full p-2 text-white/70 transition hover:bg-white/10 hover:text-white"
              @click="router.push('/mes-darets')"
              title="Notifications"
            >
              <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
              </svg>
              <span
                v-if="notifications.unreadCount.value > 0"
                class="absolute -right-0.5 -top-0.5 flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold"
              >
                {{ notifications.unreadCount.value > 99 ? '99+' : notifications.unreadCount.value }}
              </span>
            </button>

            <!-- User avatar menu -->
            <div class="relative">
              <button
                type="button"
                class="flex h-8 w-8 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary transition hover:bg-primary/30"
                @click="showUserMenu = !showUserMenu"
              >
                {{ auth.initials.value }}
              </button>

              <!-- Dropdown -->
              <Transition
                enter-active-class="transition duration-100 ease-out"
                enter-from-class="scale-95 opacity-0"
                enter-to-class="scale-100 opacity-100"
                leave-active-class="transition duration-75 ease-in"
                leave-from-class="scale-100 opacity-100"
                leave-to-class="scale-95 opacity-0"
              >
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 z-50 mt-2 w-56 overflow-hidden rounded-xl border border-white/10 bg-surface shadow-xl"
                  @click="showUserMenu = false"
                >
                  <div class="border-b border-white/10 px-4 py-3">
                    <p class="text-sm font-medium">{{ auth.fullName.value }}</p>
                    <p class="text-xs text-white/50">{{ auth.user.value?.email }}</p>
                  </div>
                  <div class="py-1">
                    <RouterLink to="/mes-darets" class="block px-4 py-2 text-sm text-white/70 hover:bg-white/5 hover:text-white">
                      Mes Darets
                    </RouterLink>
                    <button
                      type="button"
                      class="block w-full px-4 py-2 text-left text-sm text-danger hover:bg-white/5"
                      @click="handleLogout"
                    >
                      {{ t('auth.logout') }}
                    </button>
                  </div>
                </div>
              </Transition>
            </div>
          </template>

          <!-- Login button for unauthenticated users -->
          <template v-else>
            <RouterLink
              to="/login"
              class="rounded-full bg-primary px-4 py-1.5 text-sm font-medium text-background transition hover:bg-primaryHover"
            >
              {{ t('auth.login') }}
            </RouterLink>
          </template>
        </nav>
      </div>
    </header>

    <main id="main" class="container-responsive py-10">
      <RouterView />
    </main>

    <footer class="border-t border-white/10 bg-background/80">
      <div class="container-responsive py-6 text-sm text-white/60">&copy; {{ new Date().getFullYear() }} &middot; {{ t('app.name') }}</div>
    </footer>

    <!-- Global Toast Container -->
    <Teleport to="body">
      <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-2">
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
            class="flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium shadow-lg backdrop-blur"
            :class="[
              toast.type === 'success' ? 'bg-success/90 text-white' :
              toast.type === 'error' ? 'bg-danger/90 text-white' :
              'bg-white/20 text-white'
            ]"
            role="status"
          >
            <span class="flex-1">{{ toast.message }}</span>
            <button
              type="button"
              class="rounded-full p-0.5 opacity-70 transition hover:opacity-100"
              @click="dismiss(toast.id)"
            >
              <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path d="M6.28 5.22a.75.75 0 00-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 101.06 1.06L10 11.06l3.72 3.72a.75.75 0 101.06-1.06L11.06 10l3.72-3.72a.75.75 0 00-1.06-1.06L10 8.94 6.28 5.22z" />
              </svg>
            </button>
          </div>
        </TransitionGroup>
      </div>
    </Teleport>

    <!-- Click outside handler for user menu -->
    <div v-if="showUserMenu" class="fixed inset-0 z-40" @click="showUserMenu = false" />
  </div>
</template>
