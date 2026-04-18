<script setup lang="ts">
const props = defineProps<{
  tabs: Array<{ id: string; label: string; count?: number }>;
}>();

const model = defineModel<string>({ required: true });
</script>

<template>
  <div
    role="tablist"
    class="inline-flex w-full flex-wrap gap-1 rounded-2xl border border-white/10 bg-surface/60 p-1 backdrop-blur-sm"
  >
    <button
      v-for="tab in props.tabs"
      :key="tab.id"
      :id="`tab-${tab.id}`"
      role="tab"
      type="button"
      :aria-selected="model === tab.id"
      :tabindex="model === tab.id ? 0 : -1"
      @click="model = tab.id"
      class="relative flex flex-1 items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition-all duration-200"
      :class="
        model === tab.id
          ? 'bg-primary text-background shadow-glow'
          : 'text-white/70 hover:bg-white/5 hover:text-white'
      "
    >
      <span>{{ tab.label }}</span>
      <span
        v-if="typeof tab.count === 'number'"
        class="rounded-full px-1.5 py-0.5 text-[10px] font-bold"
        :class="model === tab.id ? 'bg-background/20 text-background' : 'bg-white/10 text-white/70'"
      >
        {{ tab.count }}
      </span>
    </button>
  </div>
</template>
