<script setup lang="ts">
const props = defineProps<{
  steps: string[];
  current: number;
}>();
</script>

<template>
  <ol class="flex flex-wrap items-center gap-3 text-sm" aria-label="Progression">
    <li v-for="(step, index) in props.steps" :key="step" class="flex items-center gap-2">
      <span
        class="flex h-8 w-8 items-center justify-center rounded-full border"
        :class="index < props.current ? 'border-primary bg-primary text-background' : 'border-white/20 text-white'"
      >
        {{ index + 1 }}
      </span>
      <span class="font-medium" :class="index === props.current ? 'text-primary' : 'text-white/70'">{{ step }}</span>
      <span v-if="index < props.steps.length - 1" class="mx-2 h-px w-10 bg-white/20" aria-hidden="true"></span>
    </li>
  </ol>
</template>
