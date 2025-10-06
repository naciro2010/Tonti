<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  tabs: { id: string; label: string }[];
  initial?: string;
}>();

const active = ref(props.initial ?? props.tabs[0]?.id ?? '');

const emit = defineEmits<{
  (e: 'update:active', value: string): void;
}>();

const setActive = (id: string) => {
  active.value = id;
  emit('update:active', id);
};
</script>

<template>
  <div>
    <div class="flex gap-2 rounded-full bg-white/5 p-1">
      <button
        v-for="tab in props.tabs"
        :key="tab.id"
        type="button"
        class="flex-1 rounded-full px-4 py-2 text-sm font-semibold transition"
        :class="tab.id === active ? 'bg-primary text-black shadow-glow' : 'text-white/70 hover:text-white'"
        @click="setActive(tab.id)"
      >
        {{ tab.label }}
      </button>
    </div>
    <div class="mt-4">
      <slot :active="active" />
    </div>
  </div>
</template>
