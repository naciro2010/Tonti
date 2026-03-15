import { describe, expect, it, vi, beforeEach } from 'vitest'

import { useToast } from '../useToast'

describe('useToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  it('adds a toast and auto-dismisses after duration', () => {
    const { toasts, show } = useToast()

    show('Hello', 'info', 3000)
    expect(toasts.value.length).toBeGreaterThanOrEqual(1)
    const toast = toasts.value[toasts.value.length - 1]
    expect(toast.message).toBe('Hello')
    expect(toast.type).toBe('info')

    vi.advanceTimersByTime(3000)
    expect(toasts.value.find(t => t.message === 'Hello')).toBeUndefined()
  })

  it('success helper sets type to success', () => {
    const { toasts, success } = useToast()

    success('Done!')
    const toast = toasts.value[toasts.value.length - 1]
    expect(toast.type).toBe('success')
    expect(toast.message).toBe('Done!')
  })

  it('error helper sets type to error', () => {
    const { toasts, error } = useToast()

    error('Oops')
    const toast = toasts.value[toasts.value.length - 1]
    expect(toast.type).toBe('error')
  })

  it('dismiss removes a specific toast', () => {
    const { toasts, show, dismiss } = useToast()

    show('Keep', 'info', 10000)
    show('Remove', 'info', 10000)

    const toRemove = toasts.value.find(t => t.message === 'Remove')!
    dismiss(toRemove.id)

    expect(toasts.value.find(t => t.message === 'Remove')).toBeUndefined()
    expect(toasts.value.find(t => t.message === 'Keep')).toBeDefined()
  })
})
