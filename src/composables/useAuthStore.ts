import { ref, computed, readonly } from 'vue'
import { authApi, type UserResponse } from '@/services/api'

const user = ref<UserResponse | null>(null)
const loading = ref(false)
const initialized = ref(false)

// Initialize from localStorage on load
function initFromStorage() {
  if (initialized.value) return
  const stored = authApi.getUser()
  if (stored) user.value = stored
  initialized.value = true
}

export function useAuthStore() {
  initFromStorage()

  const isAuthenticated = computed(() => !!user.value && authApi.isAuthenticated())
  const fullName = computed(() => user.value ? `${user.value.firstName} ${user.value.lastName}` : '')
  const initials = computed(() => {
    if (!user.value) return ''
    return `${user.value.firstName[0]}${user.value.lastName[0]}`.toUpperCase()
  })

  async function login(email: string, password: string) {
    loading.value = true
    try {
      const response = await authApi.login({ email, password })
      if (response.success && response.data) {
        user.value = response.data.user
      }
      return response
    } finally {
      loading.value = false
    }
  }

  async function register(data: { email: string; password: string; firstName: string; lastName: string; phone?: string }) {
    loading.value = true
    try {
      const response = await authApi.register(data)
      if (response.success && response.data) {
        user.value = response.data.user
      }
      return response
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      user.value = null
    }
  }

  async function fetchProfile() {
    if (!authApi.isAuthenticated()) return
    try {
      const response = await authApi.getMe()
      if (response.success && response.data) {
        user.value = response.data
      }
    } catch {
      // Token invalid, clear
      user.value = null
      authApi.clearAuth()
    }
  }

  return {
    user: readonly(user),
    loading: readonly(loading),
    isAuthenticated,
    fullName,
    initials,
    login,
    register,
    logout,
    fetchProfile,
  }
}
