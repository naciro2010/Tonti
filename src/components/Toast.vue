<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  message: string;
  show: boolean;
  type?: 'success' | 'error' | 'info';
}>();

const config = computed(() => {
  switch (props.type) {
    case 'success':
      return { classes: 'bg-success/90 text-white ring-success/40' };
    case 'error':
      return { classes: 'bg-danger/90 text-white ring-danger/40' };
    default:
      return { classes: 'bg-surface/95 text-white ring-white/10' };
  }
});
</script>

<template>
  <Transition name="toast" mode="out-in">
    <div
      v-if="props.show"
      class="fixed inset-x-4 bottom-4 z-50 mx-auto w-fit max-w-[calc(100%-2rem)] rounded-full px-5 py-2.5 text-sm font-medium shadow-2xl ring-1 backdrop-blur-md sm:bottom-6"
      :class="config.classes"
      role="status"
    >
      {{ props.message }}
    </div>
  </Transition>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
