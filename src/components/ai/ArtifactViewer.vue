<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Artifact } from '@/services/aiApi'
import MermaidRenderer from './renderers/MermaidRenderer.vue'
import TableRenderer from './renderers/TableRenderer.vue'
import KpiRenderer from './renderers/KpiRenderer.vue'
import ExcalidrawRenderer from './renderers/ExcalidrawRenderer.vue'
import DownloadableRenderer from './renderers/DownloadableRenderer.vue'

const props = defineProps<{
  artifacts: Artifact[]
}>()

const selectedIndex = ref(0)

const currentArtifact = computed(() => props.artifacts[selectedIndex.value])

function selectArtifact(index: number) {
  selectedIndex.value = index
}

function getTypeIcon(type: string): string {
  switch (type) {
    case 'TABLE':
      return 'table'
    case 'KPI':
      return 'kpi'
    case 'MERMAID':
      return 'diagram'
    case 'PDF':
      return 'pdf'
    case 'XLSX':
      return 'excel'
    case 'EXCALIDRAW':
      return 'draw'
    default:
      return 'file'
  }
}

function getTypeColor(type: string): string {
  switch (type) {
    case 'TABLE':
      return 'bg-blue-100 text-blue-700'
    case 'KPI':
      return 'bg-green-100 text-green-700'
    case 'MERMAID':
      return 'bg-purple-100 text-purple-700'
    case 'PDF':
      return 'bg-red-100 text-red-700'
    case 'XLSX':
      return 'bg-emerald-100 text-emerald-700'
    case 'EXCALIDRAW':
      return 'bg-orange-100 text-orange-700'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}
</script>

<template>
  <div class="flex h-full flex-col bg-white">
    <!-- Empty state -->
    <div v-if="artifacts.length === 0" class="flex flex-1 flex-col items-center justify-center p-8 text-center">
      <div class="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gray-100">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-8 w-8 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z"
          />
        </svg>
      </div>
      <h3 class="mb-1 text-sm font-medium text-gray-600">Aucun artefact</h3>
      <p class="text-xs text-gray-400">Les visualisations apparaitront ici</p>
    </div>

    <!-- Artifacts tabs + content -->
    <template v-else>
      <!-- Tabs bar -->
      <div class="flex items-center gap-1 overflow-x-auto border-b border-gray-200 px-4 py-2">
        <button
          v-for="(artifact, i) in artifacts"
          :key="artifact.id"
          class="flex flex-shrink-0 items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors"
          :class="
            selectedIndex === i
              ? 'bg-indigo-50 text-indigo-700'
              : 'text-gray-500 hover:bg-gray-50 hover:text-gray-700'
          "
          @click="selectArtifact(i)"
        >
          <span class="rounded px-1.5 py-0.5 text-[10px] font-bold uppercase" :class="getTypeColor(artifact.type)">
            {{ getTypeIcon(artifact.type) }}
          </span>
          <span class="max-w-[120px] truncate">{{ artifact.title }}</span>
        </button>
      </div>

      <!-- Artifact content -->
      <div class="flex-1 overflow-auto p-4">
        <div v-if="currentArtifact" class="h-full">
          <!-- TABLE -->
          <TableRenderer v-if="currentArtifact.type === 'TABLE'" :data="currentArtifact.data" :title="currentArtifact.title" />

          <!-- KPI -->
          <KpiRenderer v-else-if="currentArtifact.type === 'KPI'" :data="currentArtifact.data" :title="currentArtifact.title" />

          <!-- MERMAID -->
          <MermaidRenderer v-else-if="currentArtifact.type === 'MERMAID'" :data="currentArtifact.data" :title="currentArtifact.title" />

          <!-- EXCALIDRAW -->
          <ExcalidrawRenderer v-else-if="currentArtifact.type === 'EXCALIDRAW'" :data="currentArtifact.data" :title="currentArtifact.title" />

          <!-- PDF / XLSX (downloadable) -->
          <DownloadableRenderer
            v-else-if="currentArtifact.type === 'PDF' || currentArtifact.type === 'XLSX'"
            :data="currentArtifact.data"
            :title="currentArtifact.title"
            :type="currentArtifact.type"
          />
        </div>
      </div>
    </template>
  </div>
</template>
