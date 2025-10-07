<script setup lang="ts">
import { Transition } from 'vue';

const props = defineProps<{
  message: string;
  show: boolean;
  type?: 'success' | 'error' | 'info';
}>();
</script>

<template>
  <Transition name="fade" mode="out-in">
    <div
      v-if="props.show"
      class="fixed bottom-6 inset-x-0 mx-auto w-fit rounded-full px-6 py-3 text-sm font-medium shadow-lg"
      :class="[
        props.type === 'success'
          ? 'bg-success/90 text-white'
          : props.type === 'error'
            ? 'bg-danger/90 text-white'
            : 'bg-white/20 text-white',
      ]"
      role="status"
    >
      {{ props.message }}
    </div>
  </Transition>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
