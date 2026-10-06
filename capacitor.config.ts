import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'app.tonti.ios',
  appName: 'Tonti',
  webDir: 'dist',
  // Le contenu web est embarqué dans l'application (aucun chargement distant du code)
  ios: {
    scheme: 'Tonti',
    contentInset: 'never',
    backgroundColor: '#0B0F1A',
    limitsNavigationsToAppBoundDomains: false,
    preferredContentMode: 'mobile',
  },
  plugins: {
    SplashScreen: {
      launchAutoHide: false,
      backgroundColor: '#0B0F1A',
      showSpinner: false,
    },
    StatusBar: {
      style: 'DARK',
      overlaysWebView: true,
    },
  },
};

export default config;
