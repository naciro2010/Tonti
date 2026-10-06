import { tokenStore } from './storage';

export const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1').replace(
  /\/$/,
  '',
);

export interface ApiEnvelope<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly errors?: Record<string, string>,
  ) {
    super(message);
    this.name = 'ApiError';
  }

  get isNetworkError(): boolean {
    return this.status === 0;
  }
}

type UnauthorizedHandler = () => void;
let onUnauthorized: UnauthorizedHandler = () => undefined;

/** Appelé lorsque la session a expiré et ne peut pas être rafraîchie (ex : redirection vers /login). */
export function setUnauthorizedHandler(handler: UnauthorizedHandler): void {
  onUnauthorized = handler;
}

let refreshInFlight: Promise<boolean> | null = null;

/** Rafraîchit le jeton d'accès ; les appels concurrents partagent la même requête. */
function refreshTokens(): Promise<boolean> {
  if (refreshInFlight) return refreshInFlight;

  const refreshToken = tokenStore.get('refreshToken');
  if (!refreshToken) return Promise.resolve(false);

  refreshInFlight = fetch(`${API_BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
    .then(async (response) => {
      if (!response.ok) return false;
      const body = (await response.json()) as ApiEnvelope<{ accessToken: string; refreshToken: string }>;
      tokenStore.set('accessToken', body.data.accessToken);
      tokenStore.set('refreshToken', body.data.refreshToken);
      return true;
    })
    .catch(() => false)
    .finally(() => {
      refreshInFlight = null;
    });

  return refreshInFlight;
}

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  body?: unknown;
  /** Requête publique : pas de jeton, pas de tentative de rafraîchissement. */
  anonymous?: boolean;
  signal?: AbortSignal;
}

export async function request<T>(
  path: string,
  options: RequestOptions = {},
  retry = true,
): Promise<ApiEnvelope<T>> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (options.body !== undefined) headers['Content-Type'] = 'application/json';

  const accessToken = tokenStore.get('accessToken');
  if (accessToken && !options.anonymous) headers.Authorization = `Bearer ${accessToken}`;

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      signal: options.signal,
    });
  } catch (error) {
    if ((error as Error).name === 'AbortError') throw error;
    throw new ApiError(0, 'Impossible de contacter le serveur. Vérifiez votre connexion.');
  }

  if (response.status === 401 && !options.anonymous && retry && tokenStore.get('refreshToken')) {
    if (await refreshTokens()) return request<T>(path, options, false);
    tokenStore.clear();
    onUnauthorized();
    throw new ApiError(401, 'Votre session a expiré, veuillez vous reconnecter.');
  }

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    if (response.status === 401 && !options.anonymous) {
      tokenStore.clear();
      onUnauthorized();
    }
    throw new ApiError(
      response.status,
      (payload as { message?: string }).message || defaultMessage(response.status),
      (payload as { errors?: Record<string, string> }).errors,
    );
  }

  return payload as ApiEnvelope<T>;
}

function defaultMessage(status: number): string {
  if (status === 429) return 'Trop de tentatives, veuillez patienter quelques instants.';
  if (status >= 500) return 'Le service est momentanément indisponible.';
  return 'Une erreur est survenue.';
}
