# Tonti Backend

API de Tonti (gestion de Darets) : **Kotlin**, **Spring Boot 3**, **PostgreSQL** et paiements **Stripe Checkout** (carte, Apple Pay, Google Pay).

## Stack Technique

- **Kotlin 2.0** + **Spring Boot 3.3**
- **PostgreSQL** + **Flyway** migrations
- **Spring Security** + **JWT** authentication
- **Stripe Checkout** (page hébergée) derrière une abstraction `PaymentGateway`
- **Docker** & **Docker Compose**
- **OpenAPI/Swagger** documentation

## Prérequis

- Java 21+
- Docker & Docker Compose
- Compte Stripe (pour les paiements)

## Démarrage Rapide

### 1. Configuration

```bash
# Copier le fichier d'environnement
cp .env.example .env

# Éditer les variables (Stripe, JWT secret, etc.)
nano .env
```

### 2. Lancer avec Docker

```bash
# Démarrer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f backend
```

### 3. Lancer en développement

```bash
# Démarrer PostgreSQL
docker-compose up -d postgres

# Lancer l'application
./gradlew bootRun
```

## API Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/auth/register` · `/login` · `/refresh` · `/logout` | Authentification (JWT + refresh token) |
| GET/PUT/DELETE | `/api/v1/auth/me` | Profil, mise à jour, suppression du compte (mot de passe requis) |
| POST | `/api/v1/auth/change-password` | Changement de mot de passe (révoque les sessions) |
| GET/POST | `/api/v1/darets` | Lister mes Darets / créer |
| GET | `/api/v1/darets/{id}` | Détail (membres, rounds, payeurs) |
| GET | `/api/v1/darets/code/{code}` | Aperçu public par code d'invitation |
| POST | `/api/v1/darets/join` | Rejoindre |
| POST | `/api/v1/darets/{id}/start` | Démarrer (tirage au sort de l'ordre) |
| POST | `/api/v1/darets/{id}/rounds/{roundId}/close` | Clôturer un round |
| POST | `/api/v1/payments/checkout` | Démarrer le paiement de sa cotisation → URL Stripe Checkout |
| GET | `/api/v1/payments/{id}` · `/api/v1/payments` | Statut d'un paiement / historique |
| GET | `/api/v1/payments/config` | Devises payables en ligne |
| POST | `/api/v1/payments/refunds` | Remboursement (administrateur du Daret) |
| GET/PUT | `/api/v1/notifications/**` | Notifications |
| POST | `/api/webhooks/stripe` | Webhook Stripe (signature vérifiée) |
| GET/POST | `/api/payments/return/{id}` | Retour navigateur après paiement (web ou deep link app) |

**Documentation complète**: `http://localhost:8080/swagger-ui.html`

## Paiements

1. Le client appelle `POST /api/v1/payments/checkout` avec `daretId`, `roundId` et `channel` (`WEB`/`APP`).
2. L'API vérifie les droits (membre, round ouvert, pas bénéficiaire, pas déjà payé), crée le paiement
   avec le montant **du Daret** (jamais celui du client) et une session Stripe Checkout.
3. Après paiement, Stripe redirige vers `/api/payments/return/{id}`, qui renvoie vers le site ou l'app.
4. Le webhook `checkout.session.completed` marque le paiement réussi (idempotent ; un double paiement
   est remboursé automatiquement).

Configuration Stripe et webhook : voir [../DEPLOY.md](../DEPLOY.md).

## Architecture

```
src/main/kotlin/com/tonti/
├── config/          # Configuration Spring
├── controller/      # REST Controllers
├── dto/             # Data Transfer Objects
├── entity/          # Entités JPA
├── exception/       # Gestion des erreurs
├── repository/      # Repositories JPA
├── security/        # JWT & Spring Security
└── service/         # Logique métier
    └── payment/     # PaymentGateway, Stripe Checkout, webhooks
```

## Modèle de Données

```
User ──┬── Session
       ├── Membre ──── Daret ──── Round
       ├── Payment ──── Refund
       └── Notification
```

## Tests

```bash
# Lancer les tests
./gradlew test

# Tests avec couverture
./gradlew test jacocoTestReport
```

## Production

### Docker

```bash
# Build l'image
docker build -t tonti-backend .

# Run
docker run -p 8080:8080 \
  -e DATABASE_URL=... \
  -e JWT_SECRET=... \
  -e STRIPE_SECRET_KEY=... \
  tonti-backend
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tonti-backend
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: backend
        image: tonti-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

## Monitoring

- **Health Check**: `GET /api/health`
- **Actuator**: `/actuator/health` (probes liveness/readiness), `/actuator/info`

## Sécurité

- Mots de passe hashés avec BCrypt (12 rounds)
- JWT avec expiration configurable
- Limitation de débit sur les endpoints d'authentification
- CORS restreint (web + `capacitor://localhost`), en-têtes de sécurité (HSTS, nosniff, frame deny)
- Aucune donnée de carte stockée (Stripe Checkout)

## License

MIT
