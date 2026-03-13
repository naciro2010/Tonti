<script setup lang="ts">
import { ref } from 'vue'
import { saveAs } from 'file-saver'
import { downloadPdf, downloadXlsx } from '@/services/aiApi'

const props = defineProps<{
  data: any
  title: string
  type: 'PDF' | 'XLSX'
}>()

const isDownloading = ref(false)
const downloadError = ref('')

async function handleDownload() {
  isDownloading.value = true
  downloadError.value = ''

  try {
    const payload = { ...props.data, title: props.title }
    let blob: Blob

    if (props.type === 'PDF') {
      blob = await downloadPdf(payload)
    } else {
      blob = await downloadXlsx(payload)
    }

    const ext = props.type === 'PDF' ? 'pdf' : 'xlsx'
    const filename = `${props.title.replace(/\s+/g, '_').toLowerCase()}.${ext}`
    saveAs(blob, filename)
  } catch (e: any) {
    downloadError.value = e.message || 'Erreur lors du telechargement'
  } finally {
    isDownloading.value = false
  }
}

// Preview table data if available in the artifact
const hasPreview = props.data?.headers && props.data?.rows
</script>

<template>
  <div>
    <h3 class="mb-3 text-sm font-semibold text-gray-700">{{ title }}</h3>

    <div class="rounded-xl border border-gray-200 bg-white p-8 text-center">
      <!-- Icon -->
      <div class="mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-2xl" :class="type === 'PDF' ? 'bg-red-50' : 'bg-emerald-50'">
        <svg v-if="type === 'PDF'" xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-emerald-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      </div>

      <p class="mb-1 text-sm font-medium text-gray-700">{{ title }}.{{ type === 'PDF' ? 'pdf' : 'xlsx' }}</p>
      <p class="mb-4 text-xs text-gray-400">
        {{ type === 'PDF' ? 'Document PDF' : 'Fichier Excel' }} pret a etre telecharge
      </p>

      <button
        class="inline-flex items-center gap-2 rounded-lg px-6 py-2.5 text-sm font-medium text-white transition-all"
        :class="type === 'PDF' ? 'bg-red-500 hover:bg-red-600' : 'bg-emerald-500 hover:bg-emerald-600'"
        :disabled="isDownloading"
        @click="handleDownload"
      >
        <svg v-if="!isDownloading" xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
        </svg>
        <svg v-else class="h-4 w-4 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        {{ isDownloading ? 'Generation...' : 'Telecharger' }}
      </button>

      <p v-if="downloadError" class="mt-3 text-xs text-red-500">{{ downloadError }}</p>
    </div>

    <!-- Data preview if available -->
    <div v-if="hasPreview" class="mt-4">
      <details>
        <summary class="cursor-pointer text-xs text-gray-400 hover:text-gray-600">Apercu des donnees</summary>
        <div class="mt-2 overflow-x-auto rounded-lg border border-gray-200">
          <table class="w-full text-left text-xs">
            <thead class="bg-gray-50">
              <tr>
                <th v-for="h in data.headers" :key="h" class="px-3 py-2 font-semibold text-gray-500">{{ h }}</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="(row, i) in data.rows.slice(0, 5)" :key="i">
                <td v-for="(cell, j) in row" :key="j" class="px-3 py-2 text-gray-600">{{ cell }}</td>
              </tr>
            </tbody>
          </table>
          <p v-if="data.rows.length > 5" class="border-t border-gray-100 px-3 py-2 text-xs text-gray-400">
            ... et {{ data.rows.length - 5 }} lignes de plus
          </p>
        </div>
      </details>
    </div>
  </div>
</template>
