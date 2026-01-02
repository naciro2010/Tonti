package com.tonti.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val stripe: StripeProperties,
    val cors: CorsProperties
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
