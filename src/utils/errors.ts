import { ApiError } from '@/services/api';

/** Message lisible pour une erreur d'API ou réseau. */
export function errorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) return error.message || fallback;
  return fallback;
}

/** Erreurs de validation par champ renvoyées par l'API (400). */
export function fieldErrors(error: unknown): Record<string, string> {
  return error instanceof ApiError && error.errors ? { ...error.errors } : {};
}

export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const PHONE_PATTERN = /^\+?[0-9 ]{8,20}$/;
