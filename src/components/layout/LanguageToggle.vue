<script setup lang="ts">
import BaseButton from '../ui/BaseButton.vue';
import { i18n } from '../../i18n';
import { computed } from 'vue';

const locales = [
  { id: 'fr', label: 'FR' },
  { id: 'ar', label: 'العربية' },
];

const current = computed(() => i18n.global.locale.value);

const toggle = (locale: string) => {
  i18n.global.locale.value = locale;
};
</script>

<template>
  <div class="flex items-center gap-2">
    <BaseButton
      v-for="locale in locales"
      :key="locale.id"
      :variant="current === locale.id ? 'primary' : 'ghost'"
      size="sm"
      type="button"
      @click="toggle(locale.id)"
    >
      {{ locale.label }}
    </BaseButton>
  </div>
</template>
