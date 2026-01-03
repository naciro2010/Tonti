# Déploiement sur Railway

Guide de déploiement de Tonti (Frontend + Backend) sur Railway.

## Prérequis

- Compte [Railway](https://railway.app)
- Compte [Stripe](https://stripe.com)
- Ce repository connecté à Railway

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│    Backend      │────▶│   PostgreSQL    │
│   (Vue.js)      │     │  (Spring Boot)  │     │   (Railway)     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌─────────────────┐
                        │     Stripe      │
                        │  (Paiements)    │
                        └─────────────────┘
```

## Étape 1: Créer le projet Railway

1. Aller sur [railway.app](https://railway.app)
2. Cliquer sur "New Project"
3. Sélectionner "Deploy from GitHub repo"
4. Connecter votre compte GitHub et sélectionner ce repository

## Étape 2: Déployer la base de données

1. Dans votre projet Railway, cliquer sur "+ New"
2. Sélectionner "Database" → "PostgreSQL"
3. Railway crée automatiquement la variable `DATABASE_URL`

## Étape 3: Déployer le Backend

1. Cliquer sur "+ New" → "GitHub Repo"
2. Sélectionner ce repo
3. Dans les settings du service:
   - **Root Directory**: `backend`
   - **Build Command**: `./gradlew bootJar -x test`
   - **Start Command**: `java -jar build/libs/tonti-backend.jar`

4. Configurer les **Variables d'environnement**:

```env
# Base de données (automatique si PostgreSQL est dans le projet)
DATABASE_URL=${{Postgres.DATABASE_URL}}

# Profil Spring
SPRING_PROFILES_ACTIVE=prod

# JWT (générer une clé sécurisée)
JWT_SECRET=votre-cle-secrete-256-bits-minimum

# Stripe
STRIPE_SECRET_KEY=sk_live_...
STRIPE_PUBLISHABLE_KEY=pk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# CORS (URL du frontend)
ALLOWED_ORIGINS=https://votre-frontend.railway.app
```

5. Dans "Settings" → "Networking", générer un domaine public

## Étape 4: Déployer le Frontend

1. Cliquer sur "+ New" → "GitHub Repo"
2. Sélectionner ce repo (racine `/`)
3. Dans les settings du service:
   - **Root Directory**: `/` (racine)
   - **Build Command**: `npm run build`
   - **Start Command**: `npx serve dist -s -l $PORT`

4. Configurer les **Variables d'environnement**:

```env
# URL du backend
VITE_API_URL=https://votre-backend.railway.app/api/v1

# Stripe (clé publique)
VITE_STRIPE_PUBLISHABLE_KEY=pk_live_...

# Base URL
VITE_BASE_URL=/
```

5. Dans "Settings" → "Networking", générer un domaine public

## Étape 5: Configurer Stripe Webhook

1. Aller sur [dashboard.stripe.com/webhooks](https://dashboard.stripe.com/webhooks)
2. Cliquer sur "Add endpoint"
3. URL: `https://votre-backend.railway.app/api/webhooks/stripe`
4. Événements à sélectionner:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `payment_intent.canceled`
   - `payment_method.attached`
   - `payment_method.detached`
   - `charge.refunded`
5. Copier le "Signing secret" et l'ajouter comme `STRIPE_WEBHOOK_SECRET`

## Étape 6: Configurer Apple Pay (Production)

1. Sur [Stripe Dashboard](https://dashboard.stripe.com/settings/payments/apple_pay)
2. Ajouter votre domaine frontend
3. Télécharger le fichier de vérification
4. L'ajouter à `public/.well-known/apple-developer-merchantid-domain-association`

## Variables d'environnement récapitulatif

### Backend

| Variable | Description | Exemple |
|----------|-------------|---------|
| `DATABASE_URL` | URL PostgreSQL | `postgresql://...` |
| `SPRING_PROFILES_ACTIVE` | Profil Spring | `prod` |
| `JWT_SECRET` | Clé secrète JWT (256+ bits) | `votre-cle-super-secrete` |
| `STRIPE_SECRET_KEY` | Clé secrète Stripe | `sk_live_...` |
| `STRIPE_PUBLISHABLE_KEY` | Clé publique Stripe | `pk_live_...` |
| `STRIPE_WEBHOOK_SECRET` | Secret webhook Stripe | `whsec_...` |
| `ALLOWED_ORIGINS` | URLs CORS autorisées | `https://tonti.railway.app` |

### Frontend

| Variable | Description | Exemple |
|----------|-------------|---------|
| `VITE_API_URL` | URL de l'API backend | `https://api.tonti.railway.app/api/v1` |
| `VITE_STRIPE_PUBLISHABLE_KEY` | Clé publique Stripe | `pk_live_...` |
| `VITE_BASE_URL` | Base URL de l'app | `/` |

## Commandes utiles

```bash
# Voir les logs du backend
railway logs -s backend

# Voir les logs du frontend
railway logs -s frontend

# Redéployer
railway up

# Ouvrir la console PostgreSQL
railway connect postgres
```

## Monitoring

- **Health check backend**: `https://votre-backend.railway.app/api/health`
- **Swagger UI**: `https://votre-backend.railway.app/swagger-ui.html`
- **Metrics**: `https://votre-backend.railway.app/actuator/metrics`

## Troubleshooting

### Le backend ne démarre pas
- Vérifier les logs: `railway logs`
- Vérifier que `DATABASE_URL` est configuré
- Vérifier que le port `$PORT` est utilisé

### Erreurs CORS
- Vérifier que `ALLOWED_ORIGINS` contient l'URL du frontend
- Format: `https://domain1.railway.app,https://domain2.railway.app`

### Paiements échouent
- Vérifier les clés Stripe (test vs live)
- Vérifier le webhook secret
- Consulter les logs Stripe Dashboard

### Apple Pay ne fonctionne pas
- Vérifier la vérification de domaine
- Tester sur Safari avec un appareil Apple
- Vérifier que le site est en HTTPS
