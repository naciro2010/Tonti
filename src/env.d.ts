/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_STRIPE_PUBLISHABLE_KEY: string
  readonly VITE_APPLE_PAY_MERCHANT_ID: string
  readonly VITE_GOOGLE_PAY_ENVIRONMENT: 'TEST' | 'PRODUCTION'
  readonly VITE_PLAUSIBLE_DOMAIN: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
