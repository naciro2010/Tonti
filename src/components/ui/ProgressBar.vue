<script setup lang="ts">
const props = defineProps<{
  value: number;
  max: number;
}>();
</script>

<template>
  <div class="w-full overflow-hidden rounded-full bg-white/10">
    <div
      class="h-2 rounded-full bg-primary transition-all duration-500"
      :style="{ width: `${Math.min(100, Math.max(0, Math.round((props.value / props.max) * 100))) || 0}%` }"
    />
  </div>
</template>
