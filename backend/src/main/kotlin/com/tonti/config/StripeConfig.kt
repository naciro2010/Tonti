package com.tonti.config

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

@Configuration
class StripeConfig(
    private val appProperties: AppProperties
) {
    @PostConstruct
    fun init() {
        val stripe = appProperties.stripe
        if (!stripe.enabled) {
            logger.info { "Stripe disabled (app.stripe.enabled=false): EUR/USD online payments unavailable" }
            return
        }
        require(stripe.secretKey.isNotBlank()) { "app.stripe.secret-key is required when Stripe is enabled" }
        require(stripe.webhookSecret.isNotBlank()) { "app.stripe.webhook-secret is required when Stripe is enabled" }
        Stripe.apiKey = stripe.secretKey
        Stripe.setAppInfo("Tonti Backend", "1.1.0", "https://tonti.ma")
        logger.info { "Stripe SDK initialized" }
    }
}
