<script setup lang="ts">
defineOptions({ inheritAttrs: false });

const model = defineModel<string>({ default: '' });

const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    options: Array<{ label: string; value: string }>;
    placeholder?: string;
    error?: string;
    required?: boolean;
  }>(),
  {
    placeholder: undefined,
    error: undefined,
    required: false,
  },
);
</script>

<template>
  <div class="space-y-1">
    <label :for="props.id">{{ props.label }}</label>
    <select :id="props.id" v-model="model" :required="props.required" v-bind="$attrs">
      <option v-if="props.placeholder" value="">{{ props.placeholder }}</option>
      <option v-for="option in props.options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
    <p v-if="props.error" class="text-xs text-danger" role="alert">{{ props.error }}</p>
  </div>
</template>
