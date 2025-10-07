<script setup lang="ts">
defineOptions({ inheritAttrs: false });

const model = defineModel<string | number | undefined>({ default: '' });

const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    type?: string;
    placeholder?: string;
    error?: string;
    hint?: string;
    required?: boolean;
    disabled?: boolean;
  }>(),
  {
    type: 'text',
    placeholder: undefined,
    error: undefined,
    hint: undefined,
    required: false,
    disabled: false,
  },
);
</script>

<template>
  <div class="space-y-1">
    <label :for="props.id" class="text-sm font-medium">
      {{ props.label }}
      <span v-if="props.required" aria-hidden="true" class="text-danger">*</span>
    </label>
    <input
      :id="props.id"
      v-model="model"
      :type="props.type"
      :placeholder="props.placeholder"
      :required="props.required"
      :disabled="props.disabled"
      v-bind="$attrs"
      class="rtl:text-right"
    />
    <p v-if="props.hint" class="text-xs text-white/60">{{ props.hint }}</p>
    <p v-if="props.error" class="text-xs text-danger" role="alert">{{ props.error }}</p>
  </div>
</template>
