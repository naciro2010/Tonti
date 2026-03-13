<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  data: { elements: any[] }
  title: string
}>()

const svgContent = computed(() => {
  if (!props.data?.elements?.length) return null

  // Render excalidraw elements as simple SVG shapes
  const elements = props.data.elements
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity

  for (const el of elements) {
    const x = el.x || 0
    const y = el.y || 0
    const w = el.width || 100
    const h = el.height || 50
    minX = Math.min(minX, x)
    minY = Math.min(minY, y)
    maxX = Math.max(maxX, x + w)
    maxY = Math.max(maxY, y + h)
  }

  const padding = 20
  const width = maxX - minX + padding * 2
  const height = maxY - minY + padding * 2

  const shapes = elements.map((el) => {
    const x = (el.x || 0) - minX + padding
    const y = (el.y || 0) - minY + padding
    const w = el.width || 100
    const h = el.height || 50
    const fill = el.backgroundColor || '#e0e7ff'
    const stroke = el.strokeColor || '#4f46e5'
    const text = el.text || ''

    if (el.type === 'ellipse') {
      return `<ellipse cx="${x + w / 2}" cy="${y + h / 2}" rx="${w / 2}" ry="${h / 2}" fill="${fill}" stroke="${stroke}" stroke-width="2"/>
        <text x="${x + w / 2}" y="${y + h / 2}" text-anchor="middle" dominant-baseline="middle" font-size="12" fill="#1e293b">${text}</text>`
    }
    if (el.type === 'arrow' || el.type === 'line') {
      const x2 = x + w
      const y2 = y + h
      return `<line x1="${x}" y1="${y}" x2="${x2}" y2="${y2}" stroke="${stroke}" stroke-width="2" marker-end="url(#arrowhead)"/>`
    }
    // Default: rectangle
    return `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="8" fill="${fill}" stroke="${stroke}" stroke-width="2"/>
      <text x="${x + w / 2}" y="${y + h / 2}" text-anchor="middle" dominant-baseline="middle" font-size="12" fill="#1e293b">${text}</text>`
  })

  return `<svg viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
    <defs><marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto"><polygon points="0 0, 10 3.5, 0 7" fill="#4f46e5"/></marker></defs>
    ${shapes.join('\n')}
  </svg>`
})
</script>

<template>
  <div>
    <h3 class="mb-3 text-sm font-semibold text-gray-700">{{ title }}</h3>
    <div class="rounded-xl border border-gray-200 bg-white p-6">
      <div v-if="svgContent" v-html="svgContent" class="flex items-center justify-center"></div>
      <div v-else class="py-8 text-center text-sm text-gray-400">Aucun element a afficher</div>
    </div>
  </div>
</template>
