package com.tonti.controller

import com.tonti.exception.PaymentException
import com.tonti.service.payment.StripeWebhookService
import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@Hidden
@RestController
@RequestMapping("/api/webhooks")
class WebhookController(
    private val stripeWebhookService: StripeWebhookService
) {

    /**
     * Webhook Stripe. Une réponse non-2xx déclenche une nouvelle tentative côté Stripe
     * (jusqu'à 3 jours), d'où le 500 en cas d'erreur de traitement.
     */
    @PostMapping("/stripe")
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<Void> = try {
        stripeWebhookService.handle(payload, signature)
        ResponseEntity.ok().build()
    } catch (e: PaymentException) {
        logger.warn { "Rejected Stripe webhook: ${e.message}" }
        ResponseEntity.badRequest().build()
    } catch (e: Exception) {
        logger.error(e) { "Error processing Stripe webhook" }
        ResponseEntity.internalServerError().build()
    }
}
