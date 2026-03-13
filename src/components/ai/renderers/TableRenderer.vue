<script setup lang="ts">
defineProps<{
  data: { headers: string[]; rows: any[][] }
  title: string
}>()
</script>

<template>
  <div>
    <h3 class="mb-3 text-sm font-semibold text-gray-700">{{ title }}</h3>
    <div class="overflow-x-auto rounded-lg border border-gray-200">
      <table class="w-full text-left text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th
              v-for="header in data.headers"
              :key="header"
              class="whitespace-nowrap px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500"
            >
              {{ header }}
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="(row, i) in data.rows" :key="i" class="transition-colors hover:bg-gray-50">
            <td v-for="(cell, j) in row" :key="j" class="whitespace-nowrap px-4 py-3 text-gray-700">
              {{ cell }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p class="mt-2 text-xs text-gray-400">{{ data.rows?.length || 0 }} lignes</p>
  </div>
</template>
