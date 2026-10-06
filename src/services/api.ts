/**
 * Client typé de l'API Tonti (/api/v1).
 */
import { request } from './http';
import { tokenStore } from './storage';

export { ApiError, setUnauthorizedHandler } from './http';

// ============================================
// Types
// ============================================

export type Currency = 'MAD' | 'EUR' | 'USD';
export type DaretStatus = 'RECRUTEMENT' | 'VERROUILLEE' | 'ACTIVE' | 'TERMINEE' | 'ANNULEE';
export type Visibility = 'PRIVEE' | 'NON_LISTEE' | 'PUBLIQUE';
export type MembreRole = 'CREATEUR' | 'ADMIN' | 'MEMBRE';
export type PaymentStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'REQUIRES_ACTION'
  | 'SUCCEEDED'
  | 'FAILED'
  | 'CANCELLED'
  | 'REFUNDED'
  | 'PARTIALLY_REFUNDED';
export type CheckoutChannel = 'WEB' | 'APP';
export type Locale = 'fr' | 'ar' | 'en';

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}

export interface UserResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  avatarUrl?: string;
  isVerified: boolean;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface CreateDaretRequest {
  nom: string;
  description?: string;
  devise: Currency;
  montantMensuel: number;
  taille: number;
  visibilite: Visibility;
  delaiGraceJours: number;
}

export interface DaretResponse {
  id: string;
  nom: string;
  description?: string;
  devise: Currency;
  montantMensuel: number;
  taille: number;
  etat: DaretStatus;
  visibilite: Visibility;
  codeInvitation: string;
  delaiGraceJours: number;
  dateDebut?: string;
  dateFin?: string;
  createurId: string;
  membresCount: number;
  currentRound?: number;
  createdAt: string;
}

export interface MembreResponse {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  role: MembreRole;
  position?: number;
  isActive: boolean;
  joinedAt: string;
}

export interface RoundResponse {
  id: string;
  numero: number;
  receveur: MembreResponse;
  dateDebut: string;
  dateFin: string;
  estClos: boolean;
  montantTotal: number;
  paymentsCount: number;
  paidCount: number;
  paidUserIds: string[];
}

export interface DaretDetailResponse {
  id: string;
  nom: string;
  description?: string;
  devise: Currency;
  montantMensuel: number;
  taille: number;
  etat: DaretStatus;
  visibilite: Visibility;
  codeInvitation: string;
  delaiGraceJours: number;
  dateDebut?: string;
  dateFin?: string;
  createur: MembreResponse;
  membres: MembreResponse[];
  rounds: RoundResponse[];
  createdAt: string;
}

export interface CheckoutResponse {
  paymentId: string;
  provider: 'STRIPE';
  status: PaymentStatus;
  amount: number;
  currency: Currency;
  redirectUrl: string;
}

export interface PaymentResponse {
  id: string;
  daretId: string;
  roundId: string;
  roundNumero: number;
  userId: string;
  userName: string;
  amount: number;
  currency: Currency;
  status: PaymentStatus;
  provider: 'STRIPE';
  method: string;
  failureReason?: string;
  paidAt?: string;
  createdAt: string;
}

export interface PaymentConfigResponse {
  onlinePaymentCurrencies: Currency[];
  provider?: 'STRIPE';
}

export interface NotificationResponse {
  id: string;
  type: string;
  title: string;
  message: string;
  data?: string;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

// ============================================
// Authentification & compte
// ============================================

function storeSession(auth: AuthResponse): void {
  tokenStore.set('accessToken', auth.accessToken);
  tokenStore.set('refreshToken', auth.refreshToken);
  tokenStore.set('user', JSON.stringify(auth.user));
}

export const authApi = {
  async register(data: RegisterRequest): Promise<UserResponse> {
    const { data: auth } = await request<AuthResponse>('/auth/register', {
      method: 'POST',
      body: data,
      anonymous: true,
    });
    storeSession(auth);
    return auth.user;
  },

  async login(email: string, password: string): Promise<UserResponse> {
    const { data: auth } = await request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: { email, password },
      anonymous: true,
    });
    storeSession(auth);
    return auth.user;
  },

  async logout(): Promise<void> {
    try {
      await request<void>('/auth/logout', { method: 'POST' });
    } finally {
      tokenStore.clear();
    }
  },

  async me(): Promise<UserResponse> {
    const { data } = await request<UserResponse>('/auth/me');
    tokenStore.set('user', JSON.stringify(data));
    return data;
  },

  async updateProfile(data: {
    firstName?: string;
    lastName?: string;
    phone?: string;
  }): Promise<UserResponse> {
    const { data: user } = await request<UserResponse>('/auth/me', { method: 'PUT', body: data });
    tokenStore.set('user', JSON.stringify(user));
    return user;
  },

  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    await request<void>('/auth/change-password', { method: 'POST', body: { oldPassword, newPassword } });
    // Le backend révoque toutes les sessions après un changement de mot de passe
    tokenStore.clear();
  },

  async deleteAccount(password: string): Promise<void> {
    await request<void>('/auth/me', { method: 'DELETE', body: { password } });
    tokenStore.clear();
  },

  storedUser(): UserResponse | null {
    const raw = tokenStore.get('user');
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserResponse;
    } catch {
      return null;
    }
  },

  hasSession(): boolean {
    return tokenStore.get('accessToken') !== null;
  },

  clearSession(): void {
    tokenStore.clear();
  },
};

// ============================================
// Darets
// ============================================

export const daretApi = {
  create: (data: CreateDaretRequest) =>
    request<DaretResponse>('/darets', { method: 'POST', body: data }).then((r) => r.data),

  list: () => request<DaretResponse[]>('/darets').then((r) => r.data),

  get: (id: string) => request<DaretDetailResponse>(`/darets/${encodeURIComponent(id)}`).then((r) => r.data),

  preview: (code: string) =>
    request<DaretResponse>(`/darets/code/${encodeURIComponent(code)}`, { anonymous: true }).then(
      (r) => r.data,
    ),

  join: (codeInvitation: string) =>
    request<MembreResponse>('/darets/join', { method: 'POST', body: { codeInvitation } }).then((r) => r.data),

  leave: (id: string) => request<void>(`/darets/${encodeURIComponent(id)}/leave`, { method: 'DELETE' }),

  start: (id: string, data: { dateDebut?: string; roster?: string[] } = {}) =>
    request<DaretResponse>(`/darets/${encodeURIComponent(id)}/start`, { method: 'POST', body: data }).then(
      (r) => r.data,
    ),

  closeRound: (daretId: string, roundId: string) =>
    request<RoundResponse>(
      `/darets/${encodeURIComponent(daretId)}/rounds/${encodeURIComponent(roundId)}/close`,
      {
        method: 'POST',
      },
    ).then((r) => r.data),
};

// ============================================
// Paiements
// ============================================

export const paymentApi = {
  config: () => request<PaymentConfigResponse>('/payments/config', { anonymous: true }).then((r) => r.data),

  checkout: (data: { daretId: string; roundId: string; channel: CheckoutChannel; locale: Locale }) =>
    request<CheckoutResponse>('/payments/checkout', { method: 'POST', body: data }).then((r) => r.data),

  get: (id: string) => request<PaymentResponse>(`/payments/${encodeURIComponent(id)}`).then((r) => r.data),

  mine: (page = 0, size = 20) =>
    request<PagedResponse<PaymentResponse>>(`/payments?page=${page}&size=${size}`).then((r) => r.data),

  cancel: (id: string) =>
    request<PaymentResponse>(`/payments/${encodeURIComponent(id)}`, { method: 'DELETE' }).then((r) => r.data),
};

// ============================================
// Notifications
// ============================================

export const notificationApi = {
  list: (page = 0, size = 30) =>
    request<PagedResponse<NotificationResponse>>(`/notifications?page=${page}&size=${size}`).then(
      (r) => r.data,
    ),

  unreadCount: () => request<{ count: number }>('/notifications/unread/count').then((r) => r.data.count),

  markAsRead: (id: string) =>
    request<NotificationResponse>(`/notifications/${encodeURIComponent(id)}/read`, { method: 'PUT' }),

  markAllAsRead: () => request<void>('/notifications/read-all', { method: 'PUT' }),
};
