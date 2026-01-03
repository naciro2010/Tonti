package com.tonti.controller

import com.tonti.service.payment.StripeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Endpoints pour les webhooks")
class WebhookController(
    private val stripeService: StripeService
) {

    @PostMapping("/stripe")
    @Operation(summary = "Webhook Stripe")
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<String> {
        return try {
            val result = stripeService.handleWebhook(payload, signature)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error(e) { "Error processing Stripe webhook" }
            ResponseEntity.badRequest().body("Webhook error: ${e.message}")
        }
    }
}
