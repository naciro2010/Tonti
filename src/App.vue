<script setup lang="ts">
import { RouterView } from 'vue-router';
import AppHeader from './components/layout/AppHeader.vue';
import AppFooter from './components/layout/AppFooter.vue';
import LoginModal from './components/layout/LoginModal.vue';
import ToastViewport from './components/ui/ToastViewport.vue';
</script>

<template>
  <div class="flex min-h-screen flex-col bg-background text-white">
    <a href="#main" class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-xl focus:bg-primary focus:px-4 focus:py-2 focus:text-black">
      Aller au contenu
    </a>
    <AppHeader />
    <main id="main" class="flex-1">
      <RouterView />
    </main>
    <AppFooter />
    <LoginModal />
    <ToastViewport />
  </div>
</template>
