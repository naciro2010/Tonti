/**
 * useApiAuth - Composable pour l'authentification avec l'API backend
 * Remplace l'ancien système basé sur localStorage
 */

import { ref, computed, readonly } from 'vue'
import { authApi, type UserResponse, type LoginRequest, type RegisterRequest, ApiError } from '../services/api'

// État global réactif
const currentUser = ref<UserResponse | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)
const isInitialized = ref(false)

// Initialiser l'état depuis localStorage
function initFromStorage() {
  if (isInitialized.value) return

  const storedUser = authApi.getUser()
  if (storedUser && authApi.isAuthenticated()) {
    currentUser.value = storedUser
  }
  isInitialized.value = true
}

export function useApiAuth() {
  // Initialiser au premier appel
  initFromStorage()

  // Computed
  const isAuthenticated = computed(() => !!currentUser.value && authApi.isAuthenticated())
  const user = computed(() => currentUser.value)
  const fullName = computed(() =>
    currentUser.value ? `${currentUser.value.firstName} ${currentUser.value.lastName}` : ''
  )

  // Actions
  async function register(data: RegisterRequest): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.register(data)
      if (response.success && response.data) {
        currentUser.value = response.data.user
        return true
      }
      error.value = response.message || 'Erreur lors de l\'inscription'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur de connexion au serveur'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function login(data: LoginRequest): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.login(data)
      if (response.success && response.data) {
        currentUser.value = response.data.user
        return true
      }
      error.value = response.message || 'Identifiants invalides'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur de connexion au serveur'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } finally {
      currentUser.value = null
    }
  }

  async function refreshUser(): Promise<void> {
    if (!authApi.isAuthenticated()) return

    try {
      const response = await authApi.getMe()
      if (response.success && response.data) {
        currentUser.value = response.data
      }
    } catch (e) {
      // Token invalide, déconnecter
      currentUser.value = null
      authApi.clearAuth()
    }
  }

  async function updateProfile(data: { firstName?: string; lastName?: string; phone?: string }): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.updateProfile(data)
      if (response.success && response.data) {
        currentUser.value = response.data
        return true
      }
      error.value = response.message || 'Erreur lors de la mise à jour'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur de connexion au serveur'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function changePassword(oldPassword: string, newPassword: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.changePassword(oldPassword, newPassword)
      if (response.success) {
        return true
      }
      error.value = response.message || 'Erreur lors du changement de mot de passe'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur de connexion au serveur'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    // State
    user: readonly(user),
    isAuthenticated: readonly(isAuthenticated),
    isLoading: readonly(isLoading),
    error: readonly(error),
    fullName: readonly(fullName),

    // Actions
    register,
    login,
    logout,
    refreshUser,
    updateProfile,
    changePassword,
    clearError
  }
}
