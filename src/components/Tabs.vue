<script setup lang="ts">
const props = defineProps<{
  tabs: Array<{ id: string; label: string }>;
}>();

const model = defineModel<string>({ required: true });
</script>

<template>
  <div role="tablist" class="flex flex-wrap gap-2 rounded-xl bg-white/5 p-1">
    <button
      v-for="tab in props.tabs"
      :key="tab.id"
      :id="`tab-${tab.id}`"
      role="tab"
      type="button"
      :aria-selected="model === tab.id"
      :tabindex="model === tab.id ? 0 : -1"
      @click="model = tab.id"
      class="flex-1 rounded-lg px-4 py-2 text-sm font-semibold transition"
      :class="model === tab.id ? 'bg-primary text-background shadow' : 'text-white/70 hover:text-white'"
    >
      {{ tab.label }}
    </button>
  </div>
</template>
