<script setup lang="ts">
import { computed } from 'vue';

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

const describedBy = computed(() => {
  const ids: string[] = [];
  if (props.hint) ids.push(`${props.id}-hint`);
  if (props.error) ids.push(`${props.id}-error`);
  return ids.length ? ids.join(' ') : undefined;
});
</script>

<template>
  <div class="space-y-1.5">
    <label :for="props.id" class="flex items-center gap-1 text-sm font-medium text-white/90">
      {{ props.label }}
      <span v-if="props.required" aria-hidden="true" class="text-primary">*</span>
    </label>
    <input
      :id="props.id"
      v-model="model"
      :type="props.type"
      :placeholder="props.placeholder"
      :required="props.required"
      :disabled="props.disabled"
      :aria-invalid="props.error ? 'true' : undefined"
      :aria-describedby="describedBy"
      v-bind="$attrs"
      class="rtl:text-right"
    />
    <p
      v-if="props.hint && !props.error"
      :id="`${props.id}-hint`"
      class="text-xs leading-relaxed text-white/60"
    >
      {{ props.hint }}
    </p>
    <p
      v-if="props.error"
      :id="`${props.id}-error`"
      class="flex items-center gap-1.5 text-xs font-medium text-dangerSoft"
      role="alert"
    >
      <svg class="h-3.5 w-3.5 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
        <path
          fill-rule="evenodd"
          d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
          clip-rule="evenodd"
        />
      </svg>
      {{ props.error }}
    </p>
  </div>
</template>
