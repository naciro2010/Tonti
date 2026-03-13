<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  data: { label: string; value: string | number; unit: string; trend: string } | Array<{ label: string; value: string | number; unit: string; trend: string }>
  title: string
}>()

const kpis = computed(() => {
  if (Array.isArray(props.data)) return props.data
  return [props.data]
})

function trendColor(trend: string) {
  switch (trend) {
    case 'up':
      return 'text-green-600 bg-green-50'
    case 'down':
      return 'text-red-600 bg-red-50'
    default:
      return 'text-gray-600 bg-gray-50'
  }
}

function trendIcon(trend: string) {
  switch (trend) {
    case 'up':
      return '\u2191'
    case 'down':
      return '\u2193'
    default:
      return '\u2192'
  }
}
</script>

<template>
  <div>
    <h3 class="mb-3 text-sm font-semibold text-gray-700">{{ title }}</h3>
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="(kpi, i) in kpis"
        :key="i"
        class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
      >
        <p class="mb-1 text-xs font-medium uppercase tracking-wider text-gray-500">{{ kpi.label }}</p>
        <div class="flex items-baseline gap-2">
          <span class="text-3xl font-bold text-gray-900">{{ kpi.value }}</span>
          <span class="text-sm text-gray-500">{{ kpi.unit }}</span>
        </div>
        <span class="mt-2 inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium" :class="trendColor(kpi.trend)">
          {{ trendIcon(kpi.trend) }} {{ kpi.trend === 'up' ? 'En hausse' : kpi.trend === 'down' ? 'En baisse' : 'Stable' }}
        </span>
      </div>
    </div>
  </div>
</template>
