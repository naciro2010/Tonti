<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import mermaid from 'mermaid'

const props = defineProps<{
  data: { diagram: string }
  title: string
}>()

const container = ref<HTMLElement>()
const error = ref('')

mermaid.initialize({
  startOnLoad: false,
  theme: 'default',
  securityLevel: 'strict',
  fontFamily: 'Inter, sans-serif',
})

async function renderDiagram() {
  if (!container.value || !props.data?.diagram) return

  error.value = ''
  try {
    const id = `mermaid-${Date.now()}`
    const { svg } = await mermaid.render(id, props.data.diagram)
    container.value.innerHTML = svg
  } catch (e: any) {
    error.value = e.message || 'Erreur de rendu du diagramme'
    container.value.innerHTML = ''
  }
}

onMounted(renderDiagram)
watch(() => props.data?.diagram, renderDiagram)
</script>

<template>
  <div>
    <h3 class="mb-3 text-sm font-semibold text-gray-700">{{ title }}</h3>
    <div class="rounded-xl border border-gray-200 bg-white p-6">
      <div v-if="error" class="rounded-lg bg-red-50 p-4 text-sm text-red-600">{{ error }}</div>
      <div ref="container" class="flex items-center justify-center overflow-auto"></div>
    </div>
    <!-- Raw code toggle -->
    <details class="mt-2">
      <summary class="cursor-pointer text-xs text-gray-400 hover:text-gray-600">Voir le code Mermaid</summary>
      <pre class="mt-1 overflow-auto rounded-lg bg-gray-900 p-3 text-xs text-green-400">{{ data.diagram }}</pre>
    </details>
  </div>
</template>
