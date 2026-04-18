<script setup lang="ts">
const props = defineProps<{
  steps: string[];
  current: number;
}>();
</script>

<template>
  <ol
    class="flex flex-wrap items-center gap-x-2 gap-y-3 text-sm"
    aria-label="Progression"
    role="list"
  >
    <li
      v-for="(step, index) in props.steps"
      :key="step"
      class="flex items-center gap-2"
      :aria-current="index === props.current ? 'step' : undefined"
    >
      <span
        class="relative flex h-8 w-8 items-center justify-center rounded-full border text-sm font-semibold transition-all duration-200"
        :class="[
          index < props.current
            ? 'border-primary bg-primary text-background shadow-glow'
            : index === props.current
              ? 'border-primary text-primary animate-pulse-ring'
              : 'border-white/15 text-white/50',
        ]"
      >
        <svg
          v-if="index < props.current"
          class="h-4 w-4"
          viewBox="0 0 20 20"
          fill="currentColor"
          aria-hidden="true"
        >
          <path
            fill-rule="evenodd"
            d="M16.704 5.29a1 1 0 010 1.42l-8 8a1 1 0 01-1.415 0l-4-4a1 1 0 111.415-1.415L8 12.585l7.29-7.295a1 1 0 011.414 0z"
            clip-rule="evenodd"
          />
        </svg>
        <span v-else>{{ index + 1 }}</span>
      </span>
      <span
        class="font-medium transition-colors"
        :class="
          index === props.current
            ? 'text-white'
            : index < props.current
              ? 'text-white/70'
              : 'text-white/50'
        "
      >
        {{ step }}
      </span>
      <span
        v-if="index < props.steps.length - 1"
        aria-hidden="true"
        class="mx-2 h-px w-8 transition-colors sm:w-12"
        :class="index < props.current ? 'bg-primary/70' : 'bg-white/15'"
      />
    </li>
  </ol>
</template>
