<script setup lang="ts">
import { computed, useAttrs } from 'vue';

defineOptions({ inheritAttrs: false });

const props = defineProps<{
  variant?: 'primary' | 'secondary' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  isFullWidth?: boolean;
}>();

const attrs = useAttrs();

const classes = computed(() => {
  const base = 'inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:opacity-60 disabled:cursor-not-allowed';
  const variants: Record<string, string> = {
    primary: 'bg-primary text-black shadow-glow hover:bg-amber-300',
    secondary: 'bg-secondary text-white hover:bg-slate-800',
    ghost: 'bg-transparent text-white hover:bg-white/10',
    outline: 'border border-white/20 text-white hover:border-primary hover:text-primary',
  };
  const sizes: Record<string, string> = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  };

  return [
    base,
    variants[props.variant ?? 'primary'],
    sizes[props.size ?? 'md'],
    props.isFullWidth ? 'w-full' : '',
  ]
    .filter(Boolean)
    .join(' ');
});
</script>

<template>
  <button v-bind="attrs" :class="classes">
    <slot />
  </button>
</template>
