<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import ChatPanel from '@/components/ai/ChatPanel.vue'
import ArtifactViewer from '@/components/ai/ArtifactViewer.vue'
import type { Artifact } from '@/services/aiApi'

const router = useRouter()

// All artifacts accumulated from conversation
const allArtifacts = ref<Artifact[]>([])

// Track selected daret for context
const selectedDaretId = ref<string>()

// Mobile: toggle between chat and artifacts
const showArtifacts = ref(false)

function onNewArtifacts(artifacts: Artifact[]) {
  allArtifacts.value = [...allArtifacts.value, ...artifacts]
  // Auto-show artifact panel on mobile
  if (window.innerWidth < 768) {
    showArtifacts.value = true
  }
}

function clearArtifacts() {
  allArtifacts.value = []
}
</script>

<template>
  <div class="flex h-screen flex-col bg-gray-50">
    <!-- Header -->
    <header class="flex items-center justify-between border-b border-gray-200 bg-white px-4 py-3 shadow-sm">
      <div class="flex items-center gap-3">
        <button
          class="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
          @click="router.push('/')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
        <div>
          <h1 class="text-lg font-bold text-gray-800">Assistant IA</h1>
          <p class="text-xs text-gray-500">Gestionnaire intelligent de vos Darets</p>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <!-- Mobile toggle -->
        <button
          class="rounded-lg border border-gray-200 p-2 text-gray-500 hover:bg-gray-50 md:hidden"
          @click="showArtifacts = !showArtifacts"
        >
          <svg v-if="!showArtifacts" xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6z" />
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </button>

        <!-- Artifact count badge -->
        <span
          v-if="allArtifacts.length > 0"
          class="inline-flex items-center gap-1 rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-600"
        >
          {{ allArtifacts.length }} artefact{{ allArtifacts.length > 1 ? 's' : '' }}
          <button class="ml-1 text-indigo-400 hover:text-indigo-600" @click="clearArtifacts">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </span>
      </div>
    </header>

    <!-- Main content: split view -->
    <div class="flex flex-1 overflow-hidden">
      <!-- Chat Panel (left) -->
      <div
        class="flex-1 border-r border-gray-200 bg-gray-50"
        :class="{ 'hidden md:flex md:flex-col': showArtifacts }"
      >
        <ChatPanel :daret-id="selectedDaretId" @artifacts="onNewArtifacts" />
      </div>

      <!-- Artifact Viewer (right) -->
      <div
        class="w-full md:w-1/2 lg:w-3/5"
        :class="{ 'hidden md:block': !showArtifacts }"
      >
        <ArtifactViewer :artifacts="allArtifacts" />
      </div>
    </div>
  </div>
</template>
