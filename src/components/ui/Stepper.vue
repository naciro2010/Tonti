<script setup lang="ts">
import { computed } from 'vue';

export interface StepItem {
  id: string;
  label: string;
  description?: string;
}

const props = defineProps<{
  steps: StepItem[];
  current: string;
}>();

const emit = defineEmits<{
  (e: 'select', id: string): void;
}>();

const currentIndex = computed(() => props.steps.findIndex((step) => step.id === props.current));
</script>

<template>
  <ol class="flex flex-wrap gap-3 rounded-2xl border border-white/10 bg-white/5 p-4">
    <li
      v-for="(step, index) in props.steps"
      :key="step.id"
      class="flex flex-1 min-w-[140px] items-center gap-3 rounded-xl px-3 py-2"
      :class="[
        index <= currentIndex ? 'bg-primary/10 border border-primary/40 text-white' : 'text-white/60',
      ]"
      role="button"
      tabindex="0"
      @click="emit('select', step.id)"
      @keydown.enter.prevent="emit('select', step.id)"
    >
      <span
        class="flex h-9 w-9 items-center justify-center rounded-full border"
        :class="index <= currentIndex ? 'border-primary bg-primary text-black font-bold' : 'border-white/20'">
        {{ index + 1 }}
      </span>
      <div class="flex flex-col">
        <span class="text-sm font-semibold">{{ step.label }}</span>
        <span v-if="step.description" class="text-xs text-white/50">{{ step.description }}</span>
      </div>
    </li>
  </ol>
</template>
