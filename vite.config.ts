import { fileURLToPath, URL } from 'node:url';

import vue from '@vitejs/plugin-vue';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const isMobile = mode === 'mobile';

  // Un build natif ne doit jamais embarquer l'URL de l'API locale par défaut
  if (isMobile) {
    for (const key of ['VITE_API_URL', 'VITE_PUBLIC_WEB_URL']) {
      if (!env[key]?.startsWith('https://')) {
        throw new Error(`${key} must be set to an https:// URL for mobile builds`);
      }
    }
  }

  return {
    // '/' pour l'application native et Railway, '/Tonti/' pour GitHub Pages
    base: isMobile ? '/' : env.VITE_BASE_URL || '/',
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      port: 5173,
    },
    build: {
      outDir: 'dist',
      sourcemap: mode !== 'production' && !isMobile,
      // WKWebView iOS 15+ (cible de déploiement minimale de Capacitor 8)
      target: isMobile ? ['es2020', 'safari15'] : 'modules',
    },
    define: {
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version ?? '0.0.0'),
    },
  };
});
