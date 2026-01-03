/**
 * useApiPayment - Composable pour la gestion des paiements avec Stripe
 */

import { ref, computed, readonly } from 'vue'
import {
  paymentApi,
  type PaymentResponse,
  type PaymentMethodResponse,
  type PaymentIntentResponse,
  type WalletConfigResponse,
  type CreatePaymentIntentRequest,
  ApiError
} from '../services/api'

// État global
const payments = ref<PaymentResponse[]>([])
const paymentMethods = ref<PaymentMethodResponse[]>([])
const walletConfig = ref<WalletConfigResponse | null>(null)
const currentPaymentIntent = ref<PaymentIntentResponse | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

export function useApiPayment() {
  // Computed
  const defaultPaymentMethod = computed(() =>
    paymentMethods.value.find(pm => pm.isDefault)
  )

  const cardPaymentMethods = computed(() =>
    paymentMethods.value.filter(pm => pm.type === 'CARD' && !pm.walletType)
  )

  const walletPaymentMethods = computed(() =>
    paymentMethods.value.filter(pm => pm.walletType)
  )

  const isApplePayAvailable = computed(() =>
    walletConfig.value?.applePayEnabled &&
    typeof window !== 'undefined' &&
    'ApplePaySession' in window
  )

  const isGooglePayAvailable = computed(() =>
    walletConfig.value?.googlePayEnabled
  )

  // Actions - Payment Intents
  async function createPaymentIntent(data: CreatePaymentIntentRequest): Promise<PaymentIntentResponse | null> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.createPaymentIntent(data)
      if (response.success && response.data) {
        currentPaymentIntent.value = response.data
        return response.data
      }
      error.value = response.message || 'Erreur lors de la création du paiement'
      return null
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la création du paiement'
      }
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function confirmPayment(paymentIntentId: string, paymentMethodId: string): Promise<PaymentResponse | null> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.confirmPayment(paymentIntentId, paymentMethodId)
      if (response.success && response.data) {
        currentPaymentIntent.value = null
        // Ajouter à la liste des paiements
        payments.value = [response.data, ...payments.value]
        return response.data
      }
      error.value = response.message || 'Erreur lors de la confirmation du paiement'
      return null
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la confirmation du paiement'
      }
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function cancelPayment(id: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.cancelPayment(id)
      if (response.success) {
        payments.value = payments.value.filter(p => p.id !== id)
        return true
      }
      error.value = response.message || 'Erreur lors de l\'annulation'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de l\'annulation du paiement'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function fetchMyPayments(): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.getMyPayments()
      if (response.success && response.data) {
        payments.value = response.data.content
      }
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du chargement des paiements'
      }
    } finally {
      isLoading.value = false
    }
  }

  async function fetchRoundPayments(daretId: string, roundId: string): Promise<PaymentResponse[]> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.getRoundPayments(daretId, roundId)
      if (response.success && response.data) {
        return response.data
      }
      return []
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du chargement des paiements'
      }
      return []
    } finally {
      isLoading.value = false
    }
  }

  // Actions - Payment Methods
  async function fetchPaymentMethods(): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.listPaymentMethods()
      if (response.success && response.data) {
        paymentMethods.value = response.data
      }
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du chargement des méthodes de paiement'
      }
    } finally {
      isLoading.value = false
    }
  }

  async function addPaymentMethod(paymentMethodId: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.attachPaymentMethod(paymentMethodId)
      if (response.success && response.data) {
        paymentMethods.value = [...paymentMethods.value, response.data]
        return true
      }
      error.value = response.message || 'Erreur lors de l\'ajout'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de l\'ajout de la méthode de paiement'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function removePaymentMethod(paymentMethodId: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.removePaymentMethod(paymentMethodId)
      if (response.success) {
        paymentMethods.value = paymentMethods.value.filter(pm =>
          pm.id !== paymentMethodId
        )
        return true
      }
      error.value = response.message || 'Erreur lors de la suppression'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la suppression de la méthode de paiement'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function setDefaultPaymentMethod(paymentMethodId: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.setDefaultPaymentMethod(paymentMethodId)
      if (response.success) {
        // Mettre à jour localement
        paymentMethods.value = paymentMethods.value.map(pm => ({
          ...pm,
          isDefault: pm.id === paymentMethodId
        }))
        return true
      }
      error.value = response.message || 'Erreur'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la définition par défaut'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function createSetupIntent(): Promise<{ clientSecret: string } | null> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.createSetupIntent()
      if (response.success && response.data) {
        return { clientSecret: response.data.clientSecret }
      }
      error.value = response.message || 'Erreur'
      return null
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors de la création du setup intent'
      }
      return null
    } finally {
      isLoading.value = false
    }
  }

  // Actions - Wallet Config
  async function fetchWalletConfig(): Promise<void> {
    try {
      const response = await paymentApi.getWalletConfig()
      if (response.success && response.data) {
        walletConfig.value = response.data
      }
    } catch (e) {
      console.error('Failed to fetch wallet config:', e)
    }
  }

  // Refunds
  async function createRefund(paymentId: string, amount?: number, reason?: string): Promise<boolean> {
    isLoading.value = true
    error.value = null

    try {
      const response = await paymentApi.createRefund(paymentId, amount, reason)
      if (response.success) {
        // Rafraîchir les paiements
        await fetchMyPayments()
        return true
      }
      error.value = response.message || 'Erreur lors du remboursement'
      return false
    } catch (e) {
      if (e instanceof ApiError) {
        error.value = e.message
      } else {
        error.value = 'Erreur lors du remboursement'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  // Helpers
  function formatPaymentMethod(pm: PaymentMethodResponse): string {
    if (pm.walletType === 'APPLE_PAY') return 'Apple Pay'
    if (pm.walletType === 'GOOGLE_PAY') return 'Google Pay'
    if (pm.brand && pm.last4) {
      return `${pm.brand.toUpperCase()} •••• ${pm.last4}`
    }
    return pm.type
  }

  function getPaymentMethodIcon(pm: PaymentMethodResponse): string {
    if (pm.walletType === 'APPLE_PAY') return '🍎'
    if (pm.walletType === 'GOOGLE_PAY') return '🔵'
    switch (pm.brand?.toLowerCase()) {
      case 'visa': return '💳'
      case 'mastercard': return '💳'
      case 'amex': return '💳'
      default: return '💳'
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    // State
    payments: readonly(payments),
    paymentMethods: readonly(paymentMethods),
    defaultPaymentMethod: readonly(defaultPaymentMethod),
    cardPaymentMethods: readonly(cardPaymentMethods),
    walletPaymentMethods: readonly(walletPaymentMethods),
    walletConfig: readonly(walletConfig),
    currentPaymentIntent: readonly(currentPaymentIntent),
    isLoading: readonly(isLoading),
    error: readonly(error),
    isApplePayAvailable: readonly(isApplePayAvailable),
    isGooglePayAvailable: readonly(isGooglePayAvailable),

    // Actions - Payments
    createPaymentIntent,
    confirmPayment,
    cancelPayment,
    fetchMyPayments,
    fetchRoundPayments,
    createRefund,

    // Actions - Payment Methods
    fetchPaymentMethods,
    addPaymentMethod,
    removePaymentMethod,
    setDefaultPaymentMethod,
    createSetupIntent,

    // Actions - Config
    fetchWalletConfig,

    // Helpers
    formatPaymentMethod,
    getPaymentMethodIcon,
    clearError
  }
}
