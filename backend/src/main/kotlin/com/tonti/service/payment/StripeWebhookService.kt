package com.tonti.service.payment

import com.stripe.model.Event
import com.tonti.entity.Payment
import com.tonti.entity.StripeEvent
import com.tonti.repository.PaymentRepository
import com.tonti.repository.StripeEventRepository
import com.tonti.service.PaymentService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Traitement des webhooks Stripe : vérification de signature, idempotence (un événement n'est
 * traité qu'une fois), puis mise à jour du paiement correspondant.
 */
@Service
class StripeWebhookService(
    private val stripeGateway: StripeCheckoutGateway,
    private val paymentService: PaymentService,
    private val paymentRepository: PaymentRepository,
    private val stripeEventRepository: StripeEventRepository
) {

    @Transactional
    fun handle(payload: String, signature: String) {
        val event = stripeGateway.constructEvent(payload, signature)

        if (stripeEventRepository.existsByStripeEventId(event.id)) {
            logger.info { "Stripe event ${event.id} already processed, skipping" }
            return
        }

        val record = stripeEventRepository.save(
            StripeEvent(stripeEventId = event.id, type = event.type, data = event.data.toJson())
        )

        dispatch(event)

        record.processed = true
        record.processedAt = Instant.now()
        stripeEventRepository.save(record)
        logger.info { "Processed Stripe event ${event.id} (${event.type})" }
    }

    private fun dispatch(event: Event) {
        when (event.type) {
            "checkout.session.completed",
            "checkout.session.async_payment_succeeded" -> {
                val session = StripeCheckoutGateway.sessionOf(event) ?: return
                val payment = findPayment(session.clientReferenceId, session.id) ?: return
                // Les moyens de paiement différés (virement) arrivent avec payment_status = "unpaid"
                if (session.paymentStatus == "paid" || session.paymentStatus == "no_payment_required") {
                    paymentService.onPaymentSucceeded(payment, session.paymentIntent)
                }
            }

            "checkout.session.async_payment_failed" -> {
                val session = StripeCheckoutGateway.sessionOf(event) ?: return
                val payment = findPayment(session.clientReferenceId, session.id) ?: return
                paymentService.onPaymentFailed(payment, "Le paiement a été refusé", "async_payment_failed")
            }

            "checkout.session.expired" -> {
                val session = StripeCheckoutGateway.sessionOf(event) ?: return
                val payment = findPayment(session.clientReferenceId, session.id) ?: return
                paymentService.onPaymentExpired(payment)
            }

            "charge.refunded" -> {
                val charge = StripeCheckoutGateway.chargeOf(event) ?: return
                val payment = charge.paymentIntent?.let { paymentRepository.findByStripePaymentIntentId(it) } ?: return
                paymentService.onRefunded(payment, fullyRefunded = charge.refunded == true)
            }

            else -> logger.debug { "Ignoring Stripe event type ${event.type}" }
        }
    }

    private fun findPayment(clientReferenceId: String?, sessionId: String?): Payment? {
        val payment = clientReferenceId
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let { paymentRepository.findById(it).orElse(null) }
            ?: sessionId?.let { paymentRepository.findByProviderOrderId(it) }
        if (payment == null) {
            logger.warn { "No payment found for Stripe session $sessionId (ref=$clientReferenceId)" }
        }
        return payment
    }
}
