/**
 * API Service - Client HTTP pour communiquer avec le backend
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

// Types de réponse API
export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
  timestamp: string
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  isLast: boolean
}

// Erreur API personnalisée
export class ApiError extends Error {
  constructor(
    public status: number,
    public statusText: string,
    message: string,
    public errors?: Record<string, string>
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

// Gestionnaire de tokens
const tokenManager = {
  getAccessToken: (): string | null => localStorage.getItem('tonti:accessToken'),
  getRefreshToken: (): string | null => localStorage.getItem('tonti:refreshToken'),

  setTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem('tonti:accessToken', accessToken)
    localStorage.setItem('tonti:refreshToken', refreshToken)
  },

  clearTokens: () => {
    localStorage.removeItem('tonti:accessToken')
    localStorage.removeItem('tonti:refreshToken')
    localStorage.removeItem('tonti:user')
  },

  getUser: () => {
    const user = localStorage.getItem('tonti:user')
    return user ? JSON.parse(user) : null
  },

  setUser: (user: any) => {
    localStorage.setItem('tonti:user', JSON.stringify(user))
  }
}

// Fonction pour rafraîchir le token
async function refreshAccessToken(): Promise<boolean> {
  const refreshToken = tokenManager.getRefreshToken()
  if (!refreshToken) return false

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    })

    if (response.ok) {
      const data: ApiResponse<{ accessToken: string; refreshToken: string }> = await response.json()
      if (data.success && data.data) {
        tokenManager.setTokens(data.data.accessToken, data.data.refreshToken)
        return true
      }
    }
  } catch (error) {
    console.error('Token refresh failed:', error)
  }

  tokenManager.clearTokens()
  return false
}

// Client HTTP principal
async function request<T>(
  endpoint: string,
  options: RequestInit = {},
  retry = true
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`
  const accessToken = tokenManager.getAccessToken()

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers
  }

  if (accessToken) {
    ;(headers as Record<string, string>)['Authorization'] = `Bearer ${accessToken}`
  }

  const config: RequestInit = {
    ...options,
    headers
  }

  try {
    const response = await fetch(url, config)

    // Si 401 et qu'on peut réessayer, tenter de refresh le token
    if (response.status === 401 && retry) {
      const refreshed = await refreshAccessToken()
      if (refreshed) {
        return request<T>(endpoint, options, false)
      }
      // Rediriger vers login si refresh échoue
      window.location.href = '/login'
      throw new ApiError(401, 'Unauthorized', 'Session expirée')
    }

    // Parser la réponse
    const data = await response.json().catch(() => ({}))

    if (!response.ok) {
      throw new ApiError(
        response.status,
        response.statusText,
        data.message || 'Une erreur est survenue',
        data.errors
      )
    }

    return data as T
  } catch (error) {
    if (error instanceof ApiError) throw error
    throw new ApiError(0, 'Network Error', 'Impossible de contacter le serveur')
  }
}

// ============================================
// API Authentication
// ============================================

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  phone?: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserResponse
}

export interface UserResponse {
  id: string
  email: string
  firstName: string
  lastName: string
  phone?: string
  avatarUrl?: string
  isVerified: boolean
  createdAt: string
}

export const authApi = {
  async register(data: RegisterRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await request<ApiResponse<AuthResponse>>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    })
    if (response.success && response.data) {
      tokenManager.setTokens(response.data.accessToken, response.data.refreshToken)
      tokenManager.setUser(response.data.user)
    }
    return response
  },

  async login(data: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    const response = await request<ApiResponse<AuthResponse>>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data)
    })
    if (response.success && response.data) {
      tokenManager.setTokens(response.data.accessToken, response.data.refreshToken)
      tokenManager.setUser(response.data.user)
    }
    return response
  },

  async logout(): Promise<void> {
    try {
      await request('/auth/logout', { method: 'POST' })
    } finally {
      tokenManager.clearTokens()
    }
  },

  async getMe(): Promise<ApiResponse<UserResponse>> {
    return request<ApiResponse<UserResponse>>('/auth/me')
  },

  async updateProfile(data: { firstName?: string; lastName?: string; phone?: string }): Promise<ApiResponse<UserResponse>> {
    const response = await request<ApiResponse<UserResponse>>('/auth/me', {
      method: 'PUT',
      body: JSON.stringify(data)
    })
    if (response.success && response.data) {
      tokenManager.setUser(response.data)
    }
    return response
  },

  async changePassword(oldPassword: string, newPassword: string): Promise<ApiResponse<void>> {
    return request<ApiResponse<void>>('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ oldPassword, newPassword })
    })
  },

  isAuthenticated: () => !!tokenManager.getAccessToken(),
  getUser: () => tokenManager.getUser(),
  clearAuth: () => tokenManager.clearTokens()
}

// ============================================
// API Darets
// ============================================

export interface CreateDaretRequest {
  nom: string
  description?: string
  devise: 'MAD' | 'EUR' | 'USD'
  montantMensuel: number
  taille: number
  visibilite?: 'PRIVEE' | 'NON_LISTEE' | 'PUBLIQUE'
  delaiGraceJours?: number
}

export interface DaretResponse {
  id: string
  nom: string
  description?: string
  devise: 'MAD' | 'EUR' | 'USD'
  montantMensuel: number
  taille: number
  etat: 'RECRUTEMENT' | 'VERROUILLEE' | 'ACTIVE' | 'TERMINEE' | 'ANNULEE'
  visibilite: 'PRIVEE' | 'NON_LISTEE' | 'PUBLIQUE'
  codeInvitation: string
  delaiGraceJours: number
  dateDebut?: string
  dateFin?: string
  createurId: string
  membresCount: number
  currentRound?: number
  createdAt: string
}

export interface DaretDetailResponse extends Omit<DaretResponse, 'createurId' | 'membresCount' | 'currentRound'> {
  createur: MembreResponse
  membres: MembreResponse[]
  rounds: RoundResponse[]
}

export interface MembreResponse {
  id: string
  userId: string
  firstName: string
  lastName: string
  email: string
  role: 'CREATEUR' | 'ADMIN' | 'MEMBRE'
  position?: number
  isActive: boolean
  joinedAt: string
}

export interface RoundResponse {
  id: string
  numero: number
  receveur: MembreResponse
  dateDebut: string
  dateFin: string
  estClos: boolean
  montantTotal: number
  paymentsCount: number
  paidCount: number
}

export const daretApi = {
  async create(data: CreateDaretRequest): Promise<ApiResponse<DaretResponse>> {
    return request<ApiResponse<DaretResponse>>('/darets', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },

  async list(): Promise<ApiResponse<DaretResponse[]>> {
    return request<ApiResponse<DaretResponse[]>>('/darets')
  },

  async getById(id: string): Promise<ApiResponse<DaretDetailResponse>> {
    return request<ApiResponse<DaretDetailResponse>>(`/darets/${id}`)
  },

  async getByCode(code: string): Promise<ApiResponse<DaretResponse>> {
    return request<ApiResponse<DaretResponse>>(`/darets/code/${code}`)
  },

  async update(id: string, data: Partial<CreateDaretRequest>): Promise<ApiResponse<DaretResponse>> {
    return request<ApiResponse<DaretResponse>>(`/darets/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    })
  },

  async join(codeInvitation: string): Promise<ApiResponse<MembreResponse>> {
    return request<ApiResponse<MembreResponse>>('/darets/join', {
      method: 'POST',
      body: JSON.stringify({ codeInvitation })
    })
  },

  async leave(id: string): Promise<ApiResponse<void>> {
    return request<ApiResponse<void>>(`/darets/${id}/leave`, {
      method: 'DELETE'
    })
  },

  async start(id: string, data?: { dateDebut?: string; roster?: string[] }): Promise<ApiResponse<DaretResponse>> {
    return request<ApiResponse<DaretResponse>>(`/darets/${id}/start`, {
      method: 'POST',
      body: JSON.stringify(data || {})
    })
  },

  async closeRound(daretId: string, roundId: string): Promise<ApiResponse<RoundResponse>> {
    return request<ApiResponse<RoundResponse>>(`/darets/${daretId}/rounds/${roundId}/close`, {
      method: 'POST'
    })
  },

  async getPublic(page = 0, size = 20): Promise<ApiResponse<PagedResponse<DaretResponse>>> {
    return request<ApiResponse<PagedResponse<DaretResponse>>>(`/darets/public?page=${page}&size=${size}`)
  }
}

// ============================================
// API Payments
// ============================================

export interface CreatePaymentIntentRequest {
  amount: number
  currency: 'MAD' | 'EUR' | 'USD'
  daretId: string
  roundId: string
  description?: string
  paymentMethodId?: string
  returnUrl?: string
}

export interface PaymentIntentResponse {
  paymentIntentId: string
  clientSecret: string
  status: string
  amount: number
  currency: 'MAD' | 'EUR' | 'USD'
}

export interface PaymentMethodResponse {
  id: string
  type: string
  brand?: string
  last4?: string
  expMonth?: number
  expYear?: number
  walletType?: string
  isDefault: boolean
}

export interface PaymentResponse {
  id: string
  daretId: string
  roundId: string
  amount: number
  currency: 'MAD' | 'EUR' | 'USD'
  status: 'PENDING' | 'PROCESSING' | 'REQUIRES_ACTION' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED' | 'REFUNDED'
  method: 'CARD' | 'APPLE_PAY' | 'GOOGLE_PAY' | 'BANK_TRANSFER' | 'MOBILE_MONEY'
  paidAt?: string
  createdAt: string
}

export interface WalletConfigResponse {
  stripePublishableKey: string
  merchantId: string
  merchantName: string
  countryCode: string
  supportedNetworks: string[]
  applePayEnabled: boolean
  applePayMerchantId?: string
  googlePayEnabled: boolean
  googlePayEnvironment: string
}

export const paymentApi = {
  async createPaymentIntent(data: CreatePaymentIntentRequest): Promise<ApiResponse<PaymentIntentResponse>> {
    return request<ApiResponse<PaymentIntentResponse>>('/payments/intent', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },

  async confirmPayment(paymentIntentId: string, paymentMethodId: string): Promise<ApiResponse<PaymentResponse>> {
    return request<ApiResponse<PaymentResponse>>('/payments/confirm', {
      method: 'POST',
      body: JSON.stringify({ paymentIntentId, paymentMethodId })
    })
  },

  async getPayment(id: string): Promise<ApiResponse<PaymentResponse>> {
    return request<ApiResponse<PaymentResponse>>(`/payments/${id}`)
  },

  async getMyPayments(page = 0, size = 20): Promise<ApiResponse<PagedResponse<PaymentResponse>>> {
    return request<ApiResponse<PagedResponse<PaymentResponse>>>(`/payments?page=${page}&size=${size}`)
  },

  async getRoundPayments(daretId: string, roundId: string): Promise<ApiResponse<PaymentResponse[]>> {
    return request<ApiResponse<PaymentResponse[]>>(`/payments/daret/${daretId}/round/${roundId}`)
  },

  async cancelPayment(id: string): Promise<ApiResponse<PaymentResponse>> {
    return request<ApiResponse<PaymentResponse>>(`/payments/${id}`, {
      method: 'DELETE'
    })
  },

  // Setup Intent pour ajouter une méthode de paiement
  async createSetupIntent(): Promise<ApiResponse<{ setupIntentId: string; clientSecret: string; status: string }>> {
    return request<ApiResponse<{ setupIntentId: string; clientSecret: string; status: string }>>('/payments/setup-intent', {
      method: 'POST'
    })
  },

  // Payment Methods
  async attachPaymentMethod(paymentMethodId: string): Promise<ApiResponse<PaymentMethodResponse>> {
    return request<ApiResponse<PaymentMethodResponse>>('/payments/methods', {
      method: 'POST',
      body: JSON.stringify({ paymentMethodId })
    })
  },

  async listPaymentMethods(): Promise<ApiResponse<PaymentMethodResponse[]>> {
    return request<ApiResponse<PaymentMethodResponse[]>>('/payments/methods')
  },

  async removePaymentMethod(paymentMethodId: string): Promise<ApiResponse<void>> {
    return request<ApiResponse<void>>(`/payments/methods/${paymentMethodId}`, {
      method: 'DELETE'
    })
  },

  async setDefaultPaymentMethod(paymentMethodId: string): Promise<ApiResponse<void>> {
    return request<ApiResponse<void>>(`/payments/methods/${paymentMethodId}/default`, {
      method: 'PUT'
    })
  },

  // Refunds
  async createRefund(paymentId: string, amount?: number, reason?: string): Promise<ApiResponse<any>> {
    return request<ApiResponse<any>>('/payments/refunds', {
      method: 'POST',
      body: JSON.stringify({ paymentId, amount, reason })
    })
  },

  // Wallet Config (Apple Pay / Google Pay)
  async getWalletConfig(): Promise<ApiResponse<WalletConfigResponse>> {
    return request<ApiResponse<WalletConfigResponse>>('/payments/wallet-config')
  }
}

// Export par défaut
export default {
  auth: authApi,
  darets: daretApi,
  payments: paymentApi,
  isAuthenticated: authApi.isAuthenticated,
  getUser: authApi.getUser
}
