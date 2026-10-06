# Déploiement

```
 App iOS (Capacitor) ─┐
                      ├──▶  API Spring Boot  ──▶  PostgreSQL
 Site web (Vue)  ─────┘          │   ▲
                                 │   │ webhooks signés
                                 ▼   │
                           Stripe Checkout (carte, Apple Pay, Google Pay, 3-D Secure)
```

## 1. API (backend)

L'API est une application Spring Boot conteneurisée (`backend/Dockerfile`), déployable sur Railway,
Fly.io, Render, Scaleway, AWS… Exemple Railway :

1. *New Project* → *Deploy from GitHub repo* → service **backend** (Root Directory `backend`,
   builder Dockerfile, health check `/api/health` — déjà dans `backend/railway.json`).
2. Ajouter un service **PostgreSQL**.
3. Variables du service backend :

| Variable | Exemple | Rôle |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Profil production |
| `DATABASE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` | URL **JDBC** (pas l'URL `postgresql://` brute) |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | `${{Postgres.PGUSER}}` / `${{Postgres.PGPASSWORD}}` | Identifiants |
| `JWT_SECRET` | `openssl rand -base64 64` | Obligatoire, ≥ 256 bits |
| `API_BASE_URL` | `https://api.tonti.app` | URL publique de l'API (retours de paiement) |
| `WEB_BASE_URL` | `https://tonti.app` | URL publique du site web |
| `ALLOWED_ORIGINS` | `https://tonti.app,capacitor://localhost` | CORS : site web + application iOS |
| `STRIPE_SECRET_KEY` | `sk_live_…` | Clé secrète Stripe |
| `STRIPE_WEBHOOK_SECRET` | `whsec_…` | Secret de signature du webhook |
| `API_DOCS_ENABLED` | `false` | Swagger UI désactivé en production par défaut |

Les migrations Flyway s'appliquent au démarrage. Supervision : `GET /api/health`,
`GET /actuator/health` (probes `liveness` / `readiness`).

## 2. Stripe

1. Créer le compte Stripe au nom de la société (France) et activer les paiements.
2. *Developers → API keys* : récupérer `sk_live_…` (et `sk_test_…` pour la recette).
3. *Developers → Webhooks → Add endpoint* :
   - URL : `https://api.tonti.app/api/webhooks/stripe`
   - Événements : `checkout.session.completed`, `checkout.session.async_payment_succeeded`,
     `checkout.session.async_payment_failed`, `checkout.session.expired`, `charge.refunded`
   - Copier le *Signing secret* dans `STRIPE_WEBHOOK_SECRET`.
4. *Settings → Payment methods* : activer Cartes, Apple Pay et Google Pay. Avec Stripe Checkout,
   aucune vérification de domaine Apple Pay n'est nécessaire (page hébergée par Stripe).
5. Devises : MAD, EUR et USD sont encaissées et converties vers la devise de versement du compte.

En local : `stripe listen --forward-to localhost:8080/api/webhooks/stripe` puis `STRIPE_ENABLED=true`.

### Reversement des fonds au bénéficiaire — à trancher avant la mise en production

Les cotisations sont aujourd'hui encaissées sur le compte Stripe de la société ; la clôture d'un tour
calcule la cagnotte due au bénéficiaire mais **ne déclenche pas de virement**. Encaisser des fonds
pour le compte de tiers relève en France de la réglementation des services de paiement (ACPR) :

- **Recommandé** : **Stripe Connect** (comptes *Express*) — chaque membre fait vérifier son identité
  et son IBAN par Stripe, les cotisations sont transférées au bénéficiaire à la clôture du tour
  (*separate charges and transfers* avec `transfer_group` = round). Stripe porte l'agrément ; Tonti
  reste plateforme technique.
- Alternative : reversement manuel par virement, à faire valider par un juriste (statut d'agent de
  services de paiement ou exemption).

L'abstraction `PaymentGateway` (`backend/src/main/kotlin/com/tonti/service/payment`) permet d'ajouter
cette étape sans toucher au parcours de paiement.

## 3. Site web

Build statique (`npm run build` → `dist/`) à servir derrière n'importe quel CDN avec repli SPA
sur `index.html` (Railway : `npx serve dist -s`, déjà configuré dans `railway.json`).

| Variable de build | Exemple |
| --- | --- |
| `VITE_API_URL` | `https://api.tonti.app/api/v1` |
| `VITE_PUBLIC_WEB_URL` | `https://tonti.app` |
| `VITE_BASE_URL` | `/` (ou `/Tonti/` pour GitHub Pages) |

Le workflow GitHub Pages lit `VITE_API_URL` et `VITE_PUBLIC_WEB_URL` dans les *Variables* du dépôt.
Les pages `/confidentialite`, `/conditions` et `/support` doivent être accessibles publiquement :
leurs URLs sont déclarées dans la fiche App Store. Compléter `src/config/legal.ts` avec les
informations réelles de la société.

## 4. Application iOS

Voir [docs/app-store.md](docs/app-store.md).

## Check-list de mise en production

- [ ] `JWT_SECRET` aléatoire, base PostgreSQL sauvegardée
- [ ] Clés Stripe *live* et webhook configurés, paiement de bout en bout testé
- [ ] Modalités de reversement validées (Stripe Connect recommandé)
- [ ] `src/config/legal.ts` complété, CGU et politique de confidentialité relues par un juriste
- [ ] Icône définitive et captures d'écran App Store
- [ ] Compte de démonstration pour la revue Apple
