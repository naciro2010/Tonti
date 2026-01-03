/**
 * useApiDaret - Composable pour la gestion des Darets avec l'API backend
 */

import { ref, computed, readonly } from 'vue'
import { daretApi, type DaretResponse, type DaretDetailResponse, type CreateDaretRequest, type RoundResponse, ApiError } from '../services/api'

// État global
const darets = ref<DaretResponse[]>([])
const currentDaret = ref<DaretDetailResponse | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

export function useApiDaret() {
  // Computed
  const myDarets = computed(() => darets.value)
  const activeDarets = computed(() => darets.value.filter(d => d.etat === 'ACTIVE'))
  const recruitingDarets = computed(() => darets.value.filter(d => d.etat === 'RECRUTEMENT'))

  // Actions
  async function fetchMyDarets(): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.list()
      if (response.success && response.data) {
        darets.value = response.data
      }
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du chargement des Darets'
      }
    } finally {
      isLoading.value = false
    }
  }

  async function fetchDaret(id: string): Promise<DaretDetailResponse | null> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.getById(id)
      if (response.success && response.data) {
        currentDaret.value = response.data
        return response.data
      }
      return null
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du chargement du Daret'
      }
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function fetchDaretByCode(code: string): Promise<DaretResponse | null> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.getByCode(code)
      if (response.success && response.data) {
        return response.data
      }
      return null
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Daret non trouvé'
      }
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function createDaret(data: CreateDaretRequest): Promise<DaretResponse | null> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.create(data)
      if (response.success && response.data) {
        darets.value = [...darets.value, response.data]
        return response.data
      }
      error.value = response.message || 'Erreur lors de la création'
      return null
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la création du Daret'
      }
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function updateDaret(id: string, data: Partial<CreateDaretRequest>): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.update(id, data)
      if (response.success && response.data) {
        // Mettre à jour la liste
        const index = darets.value.findIndex(d => d.id === id)
        if (index !== -1) {
          darets.value[index] = response.data
        }
        return true
      }
      error.value = response.message || 'Erreur lors de la mise à jour'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la mise à jour du Daret'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function joinDaret(code: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.join(code)
      if (response.success) {
        // Rafraîchir la liste des Darets
        await fetchMyDarets()
        return true
      }
      error.value = response.message || 'Erreur lors de la jonction'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la jonction au Daret'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function leaveDaret(id: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.leave(id)
      if (response.success) {
        darets.value = darets.value.filter(d => d.id !== id)
        return true
      }
      error.value = response.message || 'Erreur lors du départ'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du départ du Daret'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function startDaret(id: string, data?: { dateDebut?: string; roster?: string[] }): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.start(id, data)
      if (response.success && response.data) {
        // Mettre à jour la liste
        const index = darets.value.findIndex(d => d.id === id)
        if (index !== -1) {
          darets.value[index] = response.data
        }
        // Rafraîchir le daret courant
        if (currentDaret.value?.id === id) {
          await fetchDaret(id)
        }
        return true
      }
      error.value = response.message || 'Erreur lors du démarrage'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du démarrage du Daret'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function closeRound(daretId: string, roundId: string): Promise<RoundResponse | null> {
    isLoading.value = true
    error.value = null

    try {
      const response = await daretApi.closeRound(daretId, roundId)
      if (response.success && response.data) {
        // Rafraîchir le daret courant
        if (currentDaret.value?.id === daretId) {
          await fetchDaret(daretId)
        }
        return response.data
      }
      error.value = response.message || 'Erreur lors de la clôture'
      return null
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la clôture du round'
      }
      return null
    } finally {
      isLoading.value = false
    }
  }

  // Helpers
  function getCurrentRound(): RoundResponse | undefined {
    return currentDaret.value?.rounds.find(r => !r.estClos)
  }

  function isCreateur(userId?: string): boolean {
    if (!currentDaret.value || !userId) return false
    return currentDaret.value.createur.userId === userId
  }

  function isMembre(userId?: string): boolean {
    if (!currentDaret.value || !userId) return false
    return currentDaret.value.membres.some(m => m.userId === userId && m.isActive)
  }

  function getMembre(userId?: string) {
    if (!currentDaret.value || !userId) return null
    return currentDaret.value.membres.find(m => m.userId === userId)
  }

  function clearError() {
    error.value = null
  }

  function clearCurrent() {
    currentDaret.value = null
  }

  return {
    // State
    darets: readonly(myDarets),
    activeDarets: readonly(activeDarets),
    recruitingDarets: readonly(recruitingDarets),
    currentDaret: readonly(currentDaret),
    isLoading: readonly(isLoading),
    error: readonly(error),

    // Actions
    fetchMyDarets,
    fetchDaret,
    fetchDaretByCode,
    createDaret,
    updateDaret,
    joinDaret,
    leaveDaret,
    startDaret,
    closeRound,

    // Helpers
    getCurrentRound,
    isCreateur,
    isMembre,
    getMembre,
    clearError,
    clearCurrent
  }
}
