import { App as CapacitorApp, type URLOpenListenerEvent } from '@capacitor/app';
import { Browser } from '@capacitor/browser';
import { Capacitor } from '@capacitor/core';
import { SplashScreen } from '@capacitor/splash-screen';
import { StatusBar, Style } from '@capacitor/status-bar';
import type { Router } from 'vue-router';

import type { CheckoutChannel } from './api';

/** Schéma de deep link déclaré dans l'application iOS (Info.plist → CFBundleURLSchemes). */
export const APP_URL_SCHEME = 'tonti';

export const isNative = Capacitor.isNativePlatform();

export const checkoutChannel: CheckoutChannel = isNative ? 'APP' : 'WEB';

/**
 * Ouvre une page externe (page de paiement Stripe, pages légales…).
 * Sur mobile : navigateur in-app (SFSafariViewController), qui affiche la barre d'adresse
 * et le cadenas, conformément aux recommandations Apple pour les paiements web.
 */
export async function openExternal(url: string): Promise<void> {
  if (isNative) {
    await Browser.open({ url, presentationStyle: 'popover' });
  } else {
    window.location.assign(url);
  }
}

/**
 * Initialisation native : barre d'état, splash screen et deep links
 * (`tonti://paiement/resultat?paymentId=…`, `tonti://daret/rejoindre?code=…`).
 */
export async function setupNativeShell(router: Router): Promise<void> {
  if (!isNative) return;

  await CapacitorApp.addListener('appUrlOpen', (event: URLOpenListenerEvent) => {
    const route = routeFromDeepLink(event.url);
    if (!route) return;
    void Browser.close().catch(() => undefined);
    void router.push(route);
  });

  await CapacitorApp.addListener('backButton', ({ canGoBack }) => {
    if (canGoBack) router.back();
    else void CapacitorApp.exitApp();
  });

  await StatusBar.setStyle({ style: Style.Dark }).catch(() => undefined);
  await SplashScreen.hide();
}

/** Convertit `tonti://chemin?query` ou un lien universel https en route de l'application. */
export function routeFromDeepLink(url: string): string | null {
  try {
    const parsed = new URL(url);
    if (parsed.protocol === `${APP_URL_SCHEME}:`) {
      // tonti://paiement/resultat → host = "paiement", pathname = "/resultat"
      return `/${parsed.host}${parsed.pathname}${parsed.search}`.replace(/\/+$/, '') || '/';
    }
    if (parsed.protocol === 'https:') {
      return `${parsed.pathname}${parsed.search}`;
    }
  } catch {
    // URL invalide : ignorée
  }
  return null;
}

/**
 * URL publique partageable (liens d'invitation). Dans l'application native, l'origine est
 * `capacitor://localhost` : on utilise donc l'URL publique du site web.
 */
export function publicUrl(path: string): string {
  const base = (
    import.meta.env.VITE_PUBLIC_WEB_URL || window.location.origin + import.meta.env.BASE_URL
  ).replace(/\/$/, '');
  return `${base}${path.startsWith('/') ? path : `/${path}`}`;
}
