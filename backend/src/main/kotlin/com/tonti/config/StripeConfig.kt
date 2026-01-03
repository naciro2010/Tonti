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
        Stripe.apiKey = appProperties.stripe.secretKey
        Stripe.setAppInfo(
            "Tonti Backend",
            "1.0.0",
            "https://tonti.app"
        )
        logger.info { "Stripe SDK initialized successfully" }
    }
}
