import { ref, readonly } from 'vue'

export interface ToastItem {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

let nextId = 0
const toasts = ref<ToastItem[]>([])

export function useToast() {
  function show(message: string, type: ToastItem['type'] = 'info', duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, duration)
  }

  function success(message: string) { show(message, 'success') }
  function error(message: string) { show(message, 'error', 5000) }
  function info(message: string) { show(message, 'info') }

  function dismiss(id: number) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  return {
    toasts: readonly(toasts),
    show,
    success,
    error,
    info,
    dismiss,
  }
}
