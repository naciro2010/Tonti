# Tonti Backend

Backend de paiement production-ready avec **Kotlin**, **Spring Boot 3**, **Stripe**, **Apple Pay** et **Google Pay**.

## Stack Technique

- **Kotlin 2.0** + **Spring Boot 3.3**
- **PostgreSQL** + **Flyway** migrations
- **Spring Security** + **JWT** authentication
- **Stripe SDK** avec support **Apple Pay** & **Google Pay**
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
| POST | `/api/v1/auth/register` | Inscription |
| POST | `/api/v1/auth/login` | Connexion |
| POST | `/api/v1/auth/refresh` | Rafraîchir le token |
| GET | `/api/v1/darets` | Lister mes Darets |
| POST | `/api/v1/darets` | Créer un Daret |
| POST | `/api/v1/darets/join` | Rejoindre un Daret |
| POST | `/api/v1/payments/intent` | Créer un paiement |
| GET | `/api/v1/payments/wallet-config` | Config Apple/Google Pay |
| POST | `/api/webhooks/stripe` | Webhook Stripe |

**Documentation complète**: `http://localhost:8080/swagger-ui.html`

## Configuration Stripe

### 1. Clés API

Récupérez vos clés sur [dashboard.stripe.com/apikeys](https://dashboard.stripe.com/apikeys):

```env
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
```

### 2. Webhook

```bash
# En développement avec Stripe CLI
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# Copiez le webhook secret
STRIPE_WEBHOOK_SECRET=whsec_...
```

### 3. Apple Pay

1. Enregistrez votre domaine sur [Stripe Dashboard](https://dashboard.stripe.com/settings/payments/apple_pay)
2. Téléchargez le fichier de vérification
3. Placez-le à `/.well-known/apple-developer-merchantid-domain-association`

### 4. Google Pay

Google Pay fonctionne automatiquement avec Stripe en mode TEST. Pour la production:
1. [Enregistrez-vous sur Google Pay](https://pay.google.com/business/console/)
2. Configurez votre `GOOGLE_PAY_MERCHANT_ID`

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
    └── payment/     # Services Stripe
```

## Modèle de Données

```
User ──┬── Session
       ├── Membre ──── Daret ──── Round
       ├── Payment ──── Refund
       └── PaymentMethod
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
- **Actuator**: `/actuator/health`, `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## Sécurité

- Mots de passe hashés avec BCrypt (12 rounds)
- JWT avec expiration configurable
- Rate limiting recommandé (à implémenter avec Redis)
- CORS configuré
- Helmet-like headers via Spring Security

## License

MIT
