<script setup lang="ts">
const props = defineProps<{
  id?: string;
  label?: string;
  description?: string;
  error?: string;
  modelValue?: string;
  placeholder?: string;
  rows?: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();
</script>

<template>
  <label class="flex flex-col gap-2 text-sm font-medium text-white/80">
    <span v-if="label" :for="props.id">{{ label }}</span>
    <textarea
      :id="props.id"
      :value="props.modelValue"
      :rows="props.rows ?? 6"
      :placeholder="props.placeholder"
      class="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-base text-white placeholder:text-white/40 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/40"
      @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
    />
    <span v-if="description && !error" class="text-xs text-white/50">{{ description }}</span>
    <span v-if="error" class="text-xs text-red-400">{{ error }}</span>
  </label>
</template>
