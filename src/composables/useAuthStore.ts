import { computed, readonly, ref } from 'vue';

import { authApi, type RegisterRequest, type UserResponse } from '@/services/api';

const user = ref<UserResponse | null>(null);
let hydrated = false;

function hydrate() {
  if (hydrated) return;
  user.value = authApi.hasSession() ? authApi.storedUser() : null;
  hydrated = true;
}

/** Session de l'utilisateur connecté (état partagé dans toute l'application). */
export function useAuthStore() {
  hydrate();

  const isAuthenticated = computed(() => user.value !== null);
  const fullName = computed(() => (user.value ? `${user.value.firstName} ${user.value.lastName}` : ''));
  const initials = computed(() =>
    user.value ? `${user.value.firstName.charAt(0)}${user.value.lastName.charAt(0)}`.toUpperCase() : '',
  );

  async function login(email: string, password: string) {
    user.value = await authApi.login(email, password);
  }

  async function register(data: RegisterRequest) {
    user.value = await authApi.register(data);
  }

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      user.value = null;
    }
  }

  async function refreshProfile() {
    if (!authApi.hasSession()) return;
    user.value = await authApi.me();
  }

  async function updateProfile(data: { firstName?: string; lastName?: string; phone?: string }) {
    user.value = await authApi.updateProfile(data);
  }

  async function changePassword(oldPassword: string, newPassword: string) {
    await authApi.changePassword(oldPassword, newPassword);
    user.value = null;
  }

  async function deleteAccount(password: string) {
    await authApi.deleteAccount(password);
    user.value = null;
  }

  /** Session expirée côté serveur : on oublie l'utilisateur localement. */
  function invalidate() {
    authApi.clearSession();
    user.value = null;
  }

  return {
    user: readonly(user),
    isAuthenticated,
    fullName,
    initials,
    login,
    register,
    logout,
    refreshProfile,
    updateProfile,
    changePassword,
    deleteAccount,
    invalidate,
  };
}
