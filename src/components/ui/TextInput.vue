<script setup lang="ts">
const props = defineProps<{
  id?: string;
  label?: string;
  description?: string;
  error?: string;
  type?: string;
  modelValue?: string | number;
  placeholder?: string;
  required?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void;
}>();

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const value = props.type === 'number' ? Number(target.value) : target.value;
  emit('update:modelValue', value);
};
</script>

<template>
  <label class="flex flex-col gap-2 text-sm font-medium text-white/80">
    <span v-if="label" :for="props.id">{{ label }}</span>
    <input
      :id="props.id"
      :value="props.modelValue"
      :type="props.type ?? 'text'"
      :placeholder="props.placeholder"
      :required="props.required"
      class="w-full rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-base text-white placeholder:text-white/40 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/40"
      @input="handleInput"
    />
    <span v-if="description && !error" class="text-xs text-white/50">{{ description }}</span>
    <span v-if="error" class="text-xs text-red-400">{{ error }}</span>
  </label>
</template>
