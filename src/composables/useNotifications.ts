import { ref, readonly } from 'vue'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
const unreadCount = ref(0)
const polling = ref<ReturnType<typeof setInterval> | null>(null)

async function fetchCount() {
  const token = localStorage.getItem('tonti:accessToken')
  if (!token) return

  try {
    const res = await fetch(`${API_BASE_URL}/notifications/unread/count`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.ok) {
      const data = await res.json()
      if (data.success && data.data) {
        unreadCount.value = data.data.count ?? 0
      }
    }
  } catch {
    // silently fail
  }
}

export function useNotifications() {
  function startPolling(intervalMs = 30000) {
    if (polling.value) return
    fetchCount()
    polling.value = setInterval(fetchCount, intervalMs)
  }

  function stopPolling() {
    if (polling.value) {
      clearInterval(polling.value)
      polling.value = null
    }
  }

  function reset() {
    unreadCount.value = 0
    stopPolling()
  }

  return {
    unreadCount: readonly(unreadCount),
    startPolling,
    stopPolling,
    reset,
    refresh: fetchCount,
  }
}
