<script setup lang="ts">
import { computed } from 'vue';

defineOptions({ inheritAttrs: false });

const model = defineModel<string>({ default: '' });

const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    options: Array<{ label: string; value: string }>;
    placeholder?: string;
    error?: string;
    hint?: string;
    required?: boolean;
  }>(),
  {
    placeholder: undefined,
    error: undefined,
    hint: undefined,
    required: false,
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
    <div class="relative">
      <select
        :id="props.id"
        v-model="model"
        :required="props.required"
        :aria-invalid="props.error ? 'true' : undefined"
        :aria-describedby="describedBy"
        v-bind="$attrs"
        class="appearance-none pr-10 rtl:pl-10 rtl:pr-4"
      >
        <option v-if="props.placeholder" value="">{{ props.placeholder }}</option>
        <option v-for="option in props.options" :key="option.value" :value="option.value">
          {{ option.label }}
        </option>
      </select>
      <svg
        class="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-white/50 rtl:left-3 rtl:right-auto"
        viewBox="0 0 20 20"
        fill="currentColor"
        aria-hidden="true"
      >
        <path
          fill-rule="evenodd"
          d="M5.23 7.21a.75.75 0 011.06.02L10 11.06l3.71-3.83a.75.75 0 111.08 1.04l-4.25 4.39a.75.75 0 01-1.08 0L5.21 8.27a.75.75 0 01.02-1.06z"
          clip-rule="evenodd"
        />
      </svg>
    </div>
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
      class="text-xs font-medium text-dangerSoft"
      role="alert"
    >
      {{ props.error }}
    </p>
  </div>
</template>
