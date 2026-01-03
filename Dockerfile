# ============================================
# TONTI Frontend - Production Dockerfile
# ============================================

# Stage 1: Build
FROM node:20-alpine AS builder

WORKDIR /app

# Copier les fichiers package
COPY package*.json ./

# Installer les dépendances
RUN npm ci

# Copier le code source
COPY . .

# Variables d'environnement pour le build
ARG VITE_API_URL
ARG VITE_STRIPE_PUBLISHABLE_KEY
ARG VITE_BASE_URL=/

ENV VITE_API_URL=$VITE_API_URL
ENV VITE_STRIPE_PUBLISHABLE_KEY=$VITE_STRIPE_PUBLISHABLE_KEY
ENV VITE_BASE_URL=$VITE_BASE_URL

# Build l'application
RUN npm run build

# Stage 2: Serve
FROM node:20-alpine AS runner

WORKDIR /app

# Installer serve pour servir les fichiers statiques
RUN npm install -g serve

# Copier les fichiers buildés
COPY --from=builder /app/dist ./dist

# Exposer le port
EXPOSE 3000

# Commande de démarrage
CMD ["serve", "dist", "-s", "-l", "3000"]
