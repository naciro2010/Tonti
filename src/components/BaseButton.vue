<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'ghost';
    size?: 'sm' | 'md' | 'lg';
    block?: boolean;
    type?: 'button' | 'submit' | 'reset';
    disabled?: boolean;
  }>(),
  {
    variant: 'primary',
    size: 'md',
    block: false,
    type: 'button',
    disabled: false,
  },
);

const classes = computed(() => {
  const base =
    'inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-60';

  const size = {
    sm: 'px-3 py-2 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  }[props.size];

  const variant = {
    primary: 'bg-primary text-background hover:bg-primaryHover',
    secondary: 'bg-white/10 text-white hover:bg-white/20',
    ghost: 'bg-transparent text-primary hover:bg-white/10',
  }[props.variant];

  return [base, size, variant, props.block ? 'w-full' : ''].join(' ');
});
</script>

<template>
  <button :type="props.type" :disabled="props.disabled" :class="classes">
    <slot />
  </button>
</template>
