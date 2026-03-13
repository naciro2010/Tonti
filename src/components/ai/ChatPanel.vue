<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import type { ChatMessage, ChatResponse, Artifact } from '@/services/aiApi'
import { sendChatMessage } from '@/services/aiApi'

const props = defineProps<{
  daretId?: string
}>()

const emit = defineEmits<{
  (e: 'artifacts', artifacts: Artifact[]): void
}>()

interface DisplayMessage {
  role: 'user' | 'assistant'
  content: string
  artifacts?: Artifact[]
  loading?: boolean
  timestamp: string
}

const messages = ref<DisplayMessage[]>([])
const input = ref('')
const isLoading = ref(false)
const chatContainer = ref<HTMLElement>()
const inputRef = ref<HTMLTextAreaElement>()

const suggestions = [
  'Donne-moi un état des lieux complet de mes Darets',
  'Analyse les retards de paiement',
  'Génère un rapport PDF récapitulatif',
  'Qui sont les membres les plus fiables ?',
  'Montre-moi un diagramme des paiements',
]

function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

async function send(text?: string) {
  const messageText = text || input.value.trim()
  if (!messageText || isLoading.value) return

  input.value = ''

  // Add user message
  messages.value.push({
    role: 'user',
    content: messageText,
    timestamp: new Date().toISOString(),
  })

  // Add loading placeholder
  messages.value.push({
    role: 'assistant',
    content: '',
    loading: true,
    timestamp: new Date().toISOString(),
  })

  isLoading.value = true
  scrollToBottom()

  try {
    const history: ChatMessage[] = messages.value
      .filter((m) => !m.loading)
      .slice(-10)
      .map((m) => ({
        role: m.role,
        content: m.content,
        timestamp: m.timestamp,
      }))

    const response = await sendChatMessage({
      message: messageText,
      daretId: props.daretId,
      conversationHistory: history.slice(0, -1),
    })

    // Remove loading placeholder
    messages.value = messages.value.filter((m) => !m.loading)

    // Add assistant response
    messages.value.push({
      role: 'assistant',
      content: response.message,
      artifacts: response.artifacts,
      timestamp: response.timestamp,
    })

    // Emit artifacts for the viewer
    if (response.artifacts?.length) {
      emit('artifacts', response.artifacts)
    }
  } catch (error: any) {
    messages.value = messages.value.filter((m) => !m.loading)
    messages.value.push({
      role: 'assistant',
      content: `Erreur: ${error.message || 'Impossible de contacter l\'agent IA. Vérifiez que Ollama est lancé.'}`,
      timestamp: new Date().toISOString(),
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

function autoResize(e: Event) {
  const target = e.target as HTMLTextAreaElement
  target.style.height = 'auto'
  target.style.height = Math.min(target.scrollHeight, 120) + 'px'
}
</script>

<template>
  <div class="flex h-full flex-col">
    <!-- Chat messages -->
    <div ref="chatContainer" class="flex-1 space-y-4 overflow-y-auto p-4">
      <!-- Welcome message when empty -->
      <div v-if="messages.length === 0" class="flex flex-col items-center justify-center py-12">
        <div
          class="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 text-2xl text-white shadow-lg"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
        </div>
        <h3 class="mb-2 text-lg font-semibold text-gray-800">Assistant de Gestion Tonti</h3>
        <p class="mb-6 max-w-md text-center text-sm text-gray-500">
          Je suis votre gestionnaire IA. Posez-moi des questions sur vos Darets, demandez des rapports, des analyses
          ou des visualisations.
        </p>

        <!-- Suggestions -->
        <div class="flex flex-wrap justify-center gap-2">
          <button
            v-for="suggestion in suggestions"
            :key="suggestion"
            class="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm text-gray-600 transition-all hover:border-indigo-300 hover:bg-indigo-50 hover:text-indigo-700"
            @click="send(suggestion)"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>

      <!-- Messages -->
      <div
        v-for="(msg, i) in messages"
        :key="i"
        class="flex gap-3"
        :class="msg.role === 'user' ? 'flex-row-reverse' : ''"
      >
        <!-- Avatar -->
        <div
          class="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full text-xs font-bold text-white"
          :class="msg.role === 'user' ? 'bg-indigo-500' : 'bg-gradient-to-br from-purple-500 to-indigo-600'"
        >
          {{ msg.role === 'user' ? 'V' : 'IA' }}
        </div>

        <!-- Message bubble -->
        <div
          class="max-w-[80%] rounded-2xl px-4 py-3"
          :class="
            msg.role === 'user'
              ? 'bg-indigo-500 text-white'
              : 'border border-gray-100 bg-white text-gray-800 shadow-sm'
          "
        >
          <!-- Loading dots -->
          <div v-if="msg.loading" class="flex items-center gap-1 py-1">
            <div class="h-2 w-2 animate-bounce rounded-full bg-gray-400" style="animation-delay: 0ms"></div>
            <div class="h-2 w-2 animate-bounce rounded-full bg-gray-400" style="animation-delay: 150ms"></div>
            <div class="h-2 w-2 animate-bounce rounded-full bg-gray-400" style="animation-delay: 300ms"></div>
          </div>

          <!-- Content -->
          <div v-else>
            <p class="whitespace-pre-wrap text-sm leading-relaxed">{{ msg.content }}</p>

            <!-- Artifact badges -->
            <div v-if="msg.artifacts?.length" class="mt-3 flex flex-wrap gap-2 border-t border-gray-100 pt-3">
              <span
                v-for="artifact in msg.artifacts"
                :key="artifact.id"
                class="inline-flex items-center gap-1 rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700"
              >
                <svg
                  v-if="artifact.type === 'TABLE'"
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-3.5 w-3.5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"
                  />
                </svg>
                <svg
                  v-else-if="artifact.type === 'KPI'"
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-3.5 w-3.5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"
                  />
                </svg>
                <svg
                  v-else-if="artifact.type === 'MERMAID'"
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-3.5 w-3.5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z"
                  />
                </svg>
                <svg
                  v-else
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-3.5 w-3.5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
                  />
                </svg>
                {{ artifact.title }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Input area -->
    <div class="border-t border-gray-200 bg-white p-4">
      <div class="flex items-end gap-3">
        <textarea
          ref="inputRef"
          v-model="input"
          rows="1"
          placeholder="Posez votre question..."
          class="flex-1 resize-none rounded-xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm text-gray-800 placeholder-gray-400 transition-colors focus:border-indigo-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-100"
          :disabled="isLoading"
          @keydown="handleKeydown"
          @input="autoResize"
        />
        <button
          class="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-xl bg-indigo-500 text-white transition-all hover:bg-indigo-600 disabled:opacity-50"
          :disabled="!input.trim() || isLoading"
          @click="send()"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path
              d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"
            />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>
