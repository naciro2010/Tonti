/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Base URL of the Tonti REST API, e.g. https://api.tonti.ma/api/v1 */
  readonly VITE_API_URL?: string;
  /** Public base path of the web build ('/' for native and Railway, '/Tonti/' for GitHub Pages) */
  readonly VITE_BASE_URL?: string;
  /** Public URL of the web app, used for shareable invitation links (required for native builds) */
  readonly VITE_PUBLIC_WEB_URL?: string;
  /** Plausible analytics domain (web only, disabled when empty) */
  readonly VITE_PLAUSIBLE_DOMAIN?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

declare const __APP_VERSION__: string;
