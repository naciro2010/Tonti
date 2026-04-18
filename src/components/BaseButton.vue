<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
    size?: 'sm' | 'md' | 'lg';
    block?: boolean;
    type?: 'button' | 'submit' | 'reset';
    disabled?: boolean;
    loading?: boolean;
  }>(),
  {
    variant: 'primary',
    size: 'md',
    block: false,
    type: 'button',
    disabled: false,
    loading: false,
  },
);

const classes = computed(() => {
  const base =
    'group relative inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition-all duration-150 ease-out ' +
    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-background ' +
    'disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:transform-none ' +
    'active:scale-[0.98] select-none';

  const size = {
    sm: 'px-3.5 py-1.5 text-sm',
    md: 'px-4 py-2.5 text-sm sm:text-base',
    lg: 'px-6 py-3 text-base sm:text-lg',
  }[props.size];

  const variant = {
    primary:
      'bg-primary text-background shadow-glow hover:bg-primaryHover hover:shadow-glow-lg hover:-translate-y-0.5 focus-visible:ring-primary',
    secondary:
      'bg-white/10 text-white border border-white/10 hover:bg-white/15 hover:border-white/20 focus-visible:ring-white/50',
    ghost:
      'bg-transparent text-primary hover:bg-primary/10 focus-visible:ring-primary/60',
    danger:
      'bg-danger text-white hover:bg-danger/90 hover:-translate-y-0.5 focus-visible:ring-danger',
  }[props.variant];

  return [base, size, variant, props.block ? 'w-full' : ''].join(' ');
});
</script>

<template>
  <button
    :type="props.type"
    :disabled="props.disabled || props.loading"
    :aria-busy="props.loading"
    :class="classes"
  >
    <svg
      v-if="props.loading"
      class="h-4 w-4 animate-spin"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-opacity="0.25" stroke-width="3" />
      <path d="M22 12a10 10 0 0 0-10-10" stroke="currentColor" stroke-width="3" stroke-linecap="round" />
    </svg>
    <span :class="props.loading ? 'opacity-90' : ''">
      <slot />
    </span>
  </button>
</template>
