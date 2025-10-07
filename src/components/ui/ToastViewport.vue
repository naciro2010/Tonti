<script setup lang="ts">
import { computed } from 'vue';
import { useToast } from '../../composables/useToast';

const { messages, remove } = useToast();

const styles = computed(() => ({
  success: 'bg-emerald-500/20 border-emerald-400/60 text-emerald-200',
  info: 'bg-blue-500/20 border-blue-400/60 text-blue-200',
  warning: 'bg-amber-500/20 border-amber-400/60 text-amber-200',
  error: 'bg-red-500/20 border-red-400/60 text-red-200',
}));
</script>

<template>
  <div class="pointer-events-none fixed inset-0 z-50 flex flex-col items-center gap-3 px-4 py-6 sm:items-end sm:justify-end">
    <transition-group name="toast">
      <article
        v-for="toast in messages"
        :key="toast.id"
        class="pointer-events-auto max-w-sm rounded-2xl border px-5 py-4 shadow-xl backdrop-blur"
        :class="styles[toast.level]"
        role="status"
        @mouseenter="remove(toast.id)"
      >
        <h3 class="text-sm font-semibold">{{ toast.title }}</h3>
        <p v-if="toast.description" class="mt-1 text-xs opacity-80">{{ toast.description }}</p>
      </article>
    </transition-group>
  </div>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
}
</style>
