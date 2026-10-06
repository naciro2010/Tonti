# Tonti

Application de gestion de **Darets** (tontines) : un groupe de proches cotise chaque mois le même
montant et, à tour de rôle, chaque membre reçoit la cagnotte. Tonti gère les invitations, le tirage
au sort de l'ordre de passage, le suivi des cotisations et leur **paiement en ligne via Stripe**.

Disponible sur le web et sur **iOS** (Capacitor), en français et en arabe (RTL).

## Architecture

| Couche | Technologies | Dossier |
| --- | --- | --- |
| API | Kotlin 2, Spring Boot 3.3, PostgreSQL, Flyway, JWT, Stripe Checkout | `backend/` |
| Web | Vue 3, TypeScript, Vite, Tailwind, vue-i18n | `src/` |
| iOS | Capacitor 8 (Swift Package Manager), fastlane | `ios/` |

Paiement : le client demande une session (`POST /api/v1/payments/checkout`), l'API calcule le montant
à partir du Daret et renvoie l'URL de la page Stripe Checkout (carte, Apple Pay, Google Pay,
3-D Secure). Le statut n'est mis à jour que par les webhooks signés de Stripe ; aucune donnée de
carte ne transite par Tonti (PCI-DSS SAQ-A).

## Démarrer en local

```bash
# API + PostgreSQL
cd backend
docker compose -f docker-compose.dev.yml up -d   # PostgreSQL
./gradlew bootRun                                # http://localhost:8080 (Swagger : /swagger-ui.html)

# Web
cd ..
npm ci
npm run dev                                      # http://localhost:5173
```

Sans clés Stripe (`STRIPE_ENABLED=false`, valeur par défaut en dev), tout fonctionne sauf le paiement
en ligne. Variables : `.env.example` (web) et `backend/.env.example` (API).

## Scripts

| Commande | Rôle |
| --- | --- |
| `npm run dev` / `npm run build` | Serveur de dev / build web |
| `npm run lint`, `npm run typecheck`, `npm run test:run`, `npm run format:check` | Qualité |
| `npm run build:mobile` | Build web pour l'app native (exige des URLs https) |
| `npm run cap:ios` | Build mobile, synchronisation et ouverture du projet Xcode |
| `npm run assets:generate` | Icônes et splash iOS depuis `assets/` |
| `cd backend && ./gradlew test` | Tests de l'API |

## Structure du front

```
src/
  services/     client HTTP (rafraîchissement de jeton), API typée, stockage Keychain, intégration native
  composables/  session, notifications, devises, dates, RTL, toasts
  pages/        accueil, auth, mes Darets, création, adhésion, tableau de bord, résultat de paiement,
                notifications, compte (suppression), pages légales
  components/   composants UI accessibles
  i18n/         fr.json, ar.json
  config/       informations légales de l'éditeur (à compléter)
```

## Documentation

- [DEPLOY.md](DEPLOY.md) — déploiement de l'API, Stripe, site web, check-list de production
- [docs/app-store.md](docs/app-store.md) — publication iOS : secrets, TestFlight, fiche App Store, revue Apple
- [backend/README.md](backend/README.md) — API REST
