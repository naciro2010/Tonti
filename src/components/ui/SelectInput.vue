<script setup lang="ts">
const props = defineProps<{
  id?: string;
  label?: string;
  options: { value: string; label: string }[];
  modelValue?: string;
  error?: string;
  placeholder?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();

const handleChange = (event: Event) => {
  emit('update:modelValue', (event.target as HTMLSelectElement).value);
};
</script>

<template>
  <label class="flex flex-col gap-2 text-sm font-medium text-white/80">
    <span v-if="label" :for="props.id">{{ label }}</span>
    <select
      :id="props.id"
      :value="props.modelValue"
      class="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-base text-white focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/40"
      @change="handleChange"
    >
      <option value="" disabled>{{ props.placeholder ?? 'Sélectionner' }}</option>
      <option v-for="option in props.options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
    <span v-if="error" class="text-xs text-red-400">{{ error }}</span>
  </label>
</template>
