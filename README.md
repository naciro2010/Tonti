# Tonti

Prototype statique 100% TypeScript (sans React) construit avec esbuild pour la plateforme de cagnottes Tonti.

## Prérequis
- Node.js 20+
- npm

## Installation
```bash
npm install
```

## Développement
```bash
npm run dev
```
Le script lance un serveur esbuild sur http://localhost:4173, regénère le bundle et la feuille de style `src/styles/app.css` à chaque modification.

## Vérifications
```bash
npm run lint       # Vérifie le formatage Prettier
npm run typecheck  # Vérifie les types avec tsc --noEmit
```

## Build statique
```bash
npm run build
```

Le dossier `dist/` contient la version exportée prête à être servie statiquement (GitHub Pages, Netlify, etc.).
