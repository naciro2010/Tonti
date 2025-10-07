# Tonti · Cagnotte digitale

Application front-end 100 % statique construite avec **Vue 3 + Vite + Tailwind CSS** pour présenter l’expérience Tonti. Toutes les
fonctionnalités (création, partage, contribution) sont simulées côté client et conformes aux exigences de la loi 15-18.

## Stack

- Vue 3 (Composition API, TypeScript)
- Vite (SSG-ready build)
- Tailwind CSS + Headless UI (styles de formulaires et typographie gérés via une couche `@layer base` personnalisée)
- Zod + vueuse pour l’autosauvegarde et la validation
- i18n complet français / arabe (RTL)
- Analytics Plausible (script léger)

## Scripts

```bash
npm install      # installe les dépendances
npm run dev      # lance le serveur Vite (http://localhost:5173)
npm run build    # génère la version statique (dist/)
npm run preview  # prévisualise la version buildée
npm run lint     # vérifie le formatage Prettier
npm run typecheck# vérifie les types avec vue-tsc
```

## Déploiement statique

Le workflow GitHub Actions `build-and-deploy` construit l’application avec Node 20 puis publie `dist/` vers GitHub Pages. Le router utilise l’historique HTML5 basé sur `import.meta.env.BASE_URL` et un `404.html` dédié redirige immédiatement vers la racine pour prendre en charge l’accès direct aux routes dynamiques.

## Données mockées

Les fichiers JSON dans `src/data/` contiennent des exemples de cagnottes, d’utilisateurs et de transactions. Ils sont utilisés pour
les pages publiques et le formulaire multi-étapes de création.

## Sécurité

- Aucun paiement réel n’est effectué : `/public/api/payment/simulate/index.json` simule une réponse.
- Les brouillons sont stockés localement (localStorage) et peuvent être supprimés à tout moment.
- Aucune donnée personnelle sensible n’est collectée.
