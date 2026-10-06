package com.tonti.service.payment

import com.stripe.exception.SignatureVerificationException
import com.stripe.exception.StripeException
import com.stripe.model.Charge
import com.stripe.model.Event
import com.stripe.model.checkout.Session
import com.stripe.net.RequestOptions
import com.stripe.net.Webhook
import com.stripe.param.RefundCreateParams
import com.stripe.param.checkout.SessionCreateParams
import com.tonti.config.AppProperties
import com.tonti.entity.Currency
import com.tonti.entity.Payment
import com.tonti.entity.PaymentProvider
import com.tonti.entity.User
import com.tonti.exception.PaymentException
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Stripe Checkout : page de paiement hébergée par Stripe (carte, Apple Pay, Google Pay, 3-D Secure).
 *
 * Tonti ne manipule jamais de données de carte (périmètre PCI-DSS SAQ-A). Le statut du paiement
 * n'est mis à jour que par les webhooks signés (`checkout.session.*`).
 */
@Component
class StripeCheckoutGateway(
    private val appProperties: AppProperties,
    private val links: CheckoutLinkService,
    private val clock: Clock
) : PaymentGateway {

    override val provider = PaymentProvider.STRIPE

    override fun supports(currency: Currency): Boolean =
        appProperties.stripe.enabled && currency in SUPPORTED_CURRENCIES

    override fun createCheckout(payment: Payment, customer: User, locale: String): CheckoutSession {
        val paymentId = payment.id!!
        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setClientReferenceId(paymentId.toString())
            .setCustomerEmail(customer.email)
            .setLocale(stripeLocale(locale))
            .setSuccessUrl(links.returnUrl(paymentId))
            .setCancelUrl(links.returnUrl(paymentId))
            .setExpiresAt(Instant.now(clock).plus(SESSION_LIFETIME).epochSecond)
            .putMetadata(METADATA_PAYMENT_ID, paymentId.toString())
            .setPaymentIntentData(
                SessionCreateParams.PaymentIntentData.builder()
                    .putMetadata(METADATA_PAYMENT_ID, paymentId.toString())
                    .setDescription("Tonti · ${payment.daret.nom} · round ${payment.round.numero}")
                    .build()
            )
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(payment.devise.name.lowercase())
                            .setUnitAmount(toMinorUnits(payment.montant))
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Cotisation ${payment.daret.nom} — round ${payment.round.numero}")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        return try {
            val options = RequestOptions.builder().setIdempotencyKey("checkout-$paymentId").build()
            val session = Session.create(params, options)
            logger.info { "Created Stripe Checkout session ${session.id} for payment $paymentId" }
            CheckoutSession(redirectUrl = session.url, providerOrderId = session.id)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create Stripe Checkout session for payment $paymentId" }
            throw PaymentException("Impossible d'initialiser le paiement", e)
        }
    }

    override fun cancelCheckout(payment: Payment) {
        val sessionId = payment.providerOrderId?.takeIf { it.startsWith("cs_") } ?: return
        try {
            val session = Session.retrieve(sessionId)
            if (session.status == "open") {
                session.expire()
                logger.info { "Expired Stripe Checkout session $sessionId" }
            }
        } catch (e: StripeException) {
            throw PaymentException("Impossible d'annuler la session de paiement", e)
        }
    }

    fun constructEvent(payload: String, signature: String): Event = try {
        Webhook.constructEvent(payload, signature, appProperties.stripe.webhookSecret)
    } catch (e: SignatureVerificationException) {
        throw PaymentException("Signature webhook invalide", e)
    }

    fun refund(paymentIntentId: String, amount: BigDecimal?, reason: String?): com.stripe.model.Refund = try {
        val builder = RefundCreateParams.builder()
            .setPaymentIntent(paymentIntentId)
            .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
        amount?.let { builder.setAmount(toMinorUnits(it)) }
        reason?.let { builder.putMetadata("reason", it.take(500)) }
        com.stripe.model.Refund.create(builder.build())
    } catch (e: StripeException) {
        logger.error(e) { "Failed to refund PaymentIntent $paymentIntentId" }
        throw PaymentException("Impossible de créer le remboursement", e)
    }

    private fun toMinorUnits(amount: BigDecimal): Long =
        amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()

    private fun stripeLocale(locale: String): SessionCreateParams.Locale = when (locale.lowercase().take(2)) {
        "en" -> SessionCreateParams.Locale.EN
        // Stripe Checkout ne propose pas l'arabe : repli sur le français
        else -> SessionCreateParams.Locale.FR
    }

    companion object {
        const val METADATA_PAYMENT_ID = "payment_id"
        private val SUPPORTED_CURRENCIES = setOf(Currency.MAD, Currency.EUR, Currency.USD)
        /** Stripe impose une durée de vie de session comprise entre 30 minutes et 24 heures. */
        private val SESSION_LIFETIME: Duration = Duration.ofMinutes(30)

        fun sessionOf(event: Event): Session? = event.dataObjectDeserializer.`object`.orElse(null) as? Session

        fun chargeOf(event: Event): Charge? = event.dataObjectDeserializer.`object`.orElse(null) as? Charge
    }
}
