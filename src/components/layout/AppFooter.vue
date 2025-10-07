<script setup lang="ts">
import { RouterLink } from 'vue-router';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <footer class="border-t border-white/10 bg-background/80 py-10">
    <div class="mx-auto grid max-w-6xl gap-8 px-4 md:grid-cols-2">
      <div class="space-y-4">
        <div class="flex items-center gap-3 text-xl font-semibold">
          <span class="flex h-12 w-12 items-center justify-center rounded-3xl bg-primary text-black">T</span>
          <span>Tonti</span>
        </div>
        <p class="text-sm text-white/70">{{ t('footer.description') }}</p>
        <p class="text-xs text-white/50">{{ t('footer.legalReminder') }}</p>
      </div>
      <div class="grid gap-3 text-sm text-white/70">
        <RouterLink to="/cgu" class="hover:text-white">{{ t('navigation.legal') }}</RouterLink>
        <RouterLink to="/confidentialite" class="hover:text-white">{{ t('navigation.privacy') }}</RouterLink>
        <RouterLink to="/aide" class="hover:text-white">Support</RouterLink>
      </div>
    </div>
    <p class="mt-10 text-center text-xs text-white/40">
      {{ t('footer.copyright', { year: new Date().getFullYear() }) }}
    </p>
  </footer>
</template>
