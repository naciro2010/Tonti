import { computed } from 'vue';
import { useStorage } from '@vueuse/core';
import { nanoid } from 'nanoid';

import type { UserSession, UserRole } from '@/types';

const sessions = useStorage<UserSession[]>('tonti:sessions', []);

export function useAuth() {
  /**
   * Créer une nouvelle session pour un utilisateur
   */
  function createSession(params: {
    userName: string;
    role: UserRole;
    daretId: string;
  }): UserSession {
    const session: UserSession = {
      userId: nanoid(10),
      userName: params.userName,
      role: params.role,
      daretId: params.daretId,
      joinedAt: new Date().toISOString(),
    };

    // Supprimer toute session existante pour cette daret
    sessions.value = sessions.value.filter((s) => s.daretId !== params.daretId);
    sessions.value.push(session);

    return session;
  }

  /**
   * Obtenir la session active pour une Daret
   */
  function getSession(daretId: string): UserSession | undefined {
    return sessions.value.find((s) => s.daretId === daretId);
  }

  /**
   * Vérifier si l'utilisateur a un certain rôle
   */
  function hasRole(daretId: string, role: UserRole): boolean {
    const session = getSession(daretId);
    return session?.role === role;
  }

  /**
   * Vérifier si l'utilisateur est le créateur
   */
  function isCreateur(daretId: string): boolean {
    return hasRole(daretId, 'createur');
  }

  /**
   * Vérifier si l'utilisateur peut modifier la Daret
   */
  function canEdit(daretId: string): boolean {
    const session = getSession(daretId);
    return session?.role === 'createur' || session?.role === 'membre';
  }

  /**
   * Vérifier si l'utilisateur peut seulement voir
   */
  function isViewerOnly(daretId: string): boolean {
    return hasRole(daretId, 'viewer');
  }

  /**
   * Déconnecter l'utilisateur d'une Daret
   */
  function logout(daretId: string): void {
    sessions.value = sessions.value.filter((s) => s.daretId !== daretId);
  }

  /**
   * Obtenir toutes les sessions actives
   */
  const activeSessions = computed(() => sessions.value);

  /**
   * Obtenir le nom d'utilisateur pour une Daret
   */
  function getUserName(daretId: string): string | undefined {
    return getSession(daretId)?.userName;
  }

  /**
   * Obtenir le rôle pour une Daret
   */
  function getRole(daretId: string): UserRole | undefined {
    return getSession(daretId)?.role;
  }

  return {
    // Méthodes
    createSession,
    getSession,
    hasRole,
    isCreateur,
    canEdit,
    isViewerOnly,
    logout,
    getUserName,
    getRole,

    // Computed
    activeSessions,
  };
}
