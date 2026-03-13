package com.tonti.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val stripe: StripeProperties,
    val cors: CorsProperties,
    val ollama: OllamaProperties = OllamaProperties()
)

data class JwtProperties(
    val secret: String,
    val expiration: Long,
    val refreshExpiration: Long
)

data class StripeProperties(
    val secretKey: String,
    val publishableKey: String,
    val webhookSecret: String
)

data class CorsProperties(
    val allowedOrigins: String
)

data class OllamaProperties(
    val baseUrl: String = "http://localhost:11434",
    val model: String = "mistral",
    val timeoutSeconds: Long = 120
)
