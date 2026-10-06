package com.tonti.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val cors: CorsProperties,
    val urls: UrlProperties,
    @DefaultValue val stripe: StripeProperties,
    @DefaultValue val rateLimit: RateLimitProperties
)

data class JwtProperties(
    val secret: String,
    val expiration: Long,
    val refreshExpiration: Long
)

data class CorsProperties(
    val allowedOrigins: String
) {
    fun origins(): List<String> = allowedOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

/**
 * URLs publiques utilisées pour construire les redirections de paiement.
 */
data class UrlProperties(
    /** URL publique du backend (ex: https://api.tonti.ma), sans slash final. */
    val apiBaseUrl: String,
    /** URL publique de l'application web (ex: https://app.tonti.ma), sans slash final. */
    val webBaseUrl: String,
    /** Schéma de deep link de l'application mobile (tonti://). */
    @DefaultValue("tonti") val appScheme: String
)

/**
 * Stripe : PSP de Tonti (MAD, EUR, USD) via Stripe Checkout.
 * `enabled=false` permet de lancer l'API en local sans clés (le paiement en ligne est alors indisponible).
 */
data class StripeProperties(
    @DefaultValue("false") val enabled: Boolean,
    @DefaultValue("") val secretKey: String,
    @DefaultValue("") val webhookSecret: String
)

data class RateLimitProperties(
    /** Nombre de requêtes autorisées par IP et par fenêtre sur les endpoints d'authentification. */
    @DefaultValue("10") val authRequests: Int,
    @DefaultValue("1m") val authWindow: Duration
)
