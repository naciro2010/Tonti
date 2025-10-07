# Tonti · Daret statique

Application Vue 3 (TypeScript) pour organiser une cagnotte rotative (Daret) avec persistance locale et génération statique via Vite + vite-plugin-ssg.

## Prérequis
- Node.js 20+
- npm 9+

## Installation
```bash
npm install
```

## Scripts
- `npm run dev` : serveur de développement Vite.
- `npm run build` : build SSG + copie du fallback 404.
- `npm run preview` : prévisualisation du build.
- `npm run lint` : ESLint sur le dossier `src`.
- `npm run lint:fix` : ESLint avec auto-fix.
- `npm run typecheck` : vérification des types via `vue-tsc`.
- `npm run test` : tests unitaires Vitest.

## Structure
```
src/
  components/        Composants UI accessibles (Headless UI, Tailwind)
  composables/       Stores/composables (autosave, devise, dates, Plausible…)
  data/              Seeds JSON (demo Darets)
  i18n/              Internationalisation FR/AR + RTL
  pages/             Pages Vue (Home, Wizard, Dashboard, Join)
  router/            Routes déclaratives Vite SSG
  styles/            Tailwind + @layer base
  utils/             Aides (roster RNG, ICS, maths)
```

## Fonctionnalités clés
- Wizard “Créer une Daret” (4 étapes) avec Zod + autosave (localStorage via VueUse).
- Dashboard : suivi des paiements, badges statut, clôture conditionnée.
- Conversion MAD ↔ EUR avec affichage combiné.
- Export calendrier `.ics` et bannière d’invitation (lien + QR code).
- Notifications locales (opt-in) pour les organisateurs.
- i18n FR/AR avec inversion RTL automatique (`useRtl`).
- Persistance locale + mocks (`darets.mock.json`) pour démo (aucun backend).
- Plausible Analytics via hook router.

## Statique & déploiement
- Build SSG (`npm run build`) → `dist/` prêt pour GitHub Pages.
- Fallback SPA généré (`dist/404.html`).
- Workflow GitHub Actions (`.github/workflows/deploy.yml`) : Node 20, build, déploiement sur Pages.

## Accessibilité & mobile
- Tailwind `@layer base` pour formulaires accessibles, focus visibles.
- Headless UI (Modal) avec piège du focus.
- Mise en page mobile-first (grilles adaptatives, stepper compact).

## Internationalisation / RTL
- `vue-i18n` avec fichiers `fr.json` et `ar.json` (UI + toasts + erreurs).
- `useRtl` force `document.dir` + classes utilitaires Tailwind (`rtl:`).
- Préférence de langue persistée (`tonti:locale`).

## Tests & qualité
- ESLint + Prettier.
- `vue-tsc` pour la validation des types.
- Vitest (`src/utils/__tests__`) pour la RNG du roster et les calculs financiers.

## Limitations connues
- Données stockées côté client (localStorage) : aucune synchronisation multi-appareil.
- Notifications locales dépendantes du navigateur (HTTPS requis pour production).
- Conversion MAD↔EUR statique (taux manuel).

## Checklist Lighthouse (cible ≥ 90 mobile)
- [x] Performances : bundle léger, pas de polices auto-hébergées inutiles.
- [x] Accessibilité : contrastes, navigation clavier, aria labels.
- [x] Best Practices : HTTPS requis, Plausible script léger.
- [x] SEO : meta `description`, balises Open Graph.
