package com.tonti.service.payment

import com.stripe.exception.SignatureVerificationException
import com.stripe.exception.StripeException
import com.stripe.model.*
import com.stripe.net.Webhook
import com.stripe.param.*
import com.tonti.config.AppProperties
import com.tonti.dto.payment.*
import com.tonti.entity.*
import com.tonti.exception.PaymentException
import com.tonti.repository.*
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class StripeService(
    private val appProperties: AppProperties,
    private val userRepository: UserRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val stripeEventRepository: StripeEventRepository,
    private val refundRepository: RefundRepository
) {

    // ==========================================
    // Customer Management
    // ==========================================

    fun createCustomer(user: User): String {
        try {
            val params = CustomerCreateParams.builder()
                .setEmail(user.email)
                .setName(user.fullName())
                .setPhone(user.phone)
                .putMetadata("user_id", user.id.toString())
                .build()

            val customer = Customer.create(params)
            logger.info { "Created Stripe customer ${customer.id} for user ${user.id}" }
            return customer.id
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create Stripe customer for user ${user.id}" }
            throw PaymentException("Impossible de créer le client Stripe", e)
        }
    }

    fun getOrCreateCustomer(user: User): String {
        return user.stripeCustomerId ?: run {
            val customerId = createCustomer(user)
            user.stripeCustomerId = customerId
            userRepository.save(user)
            customerId
        }
    }

    fun updateCustomer(customerId: String, user: User) {
        try {
            val customer = Customer.retrieve(customerId)
            val params = CustomerUpdateParams.builder()
                .setEmail(user.email)
                .setName(user.fullName())
                .setPhone(user.phone)
                .build()
            customer.update(params)
            logger.info { "Updated Stripe customer $customerId" }
        } catch (e: StripeException) {
            logger.error(e) { "Failed to update Stripe customer $customerId" }
            throw PaymentException("Impossible de mettre à jour le client Stripe", e)
        }
    }

    // ==========================================
    // Payment Intent (pour Card, Apple Pay, Google Pay)
    // ==========================================

    @Transactional
    fun createPaymentIntent(request: CreatePaymentIntentRequest, user: User): PaymentIntentResponse {
        try {
            val customerId = getOrCreateCustomer(user)

            val paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(convertToStripeAmount(request.amount, request.currency))
                .setCurrency(request.currency.name.lowercase())
                .setCustomer(customerId)
                .setDescription(request.description)
                .putMetadata("user_id", user.id.toString())
                .putMetadata("daret_id", request.daretId.toString())
                .putMetadata("round_id", request.roundId.toString())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )

            // Configuration pour Apple Pay et Google Pay
            // Ces wallets sont automatiquement activés avec automatic_payment_methods

            request.paymentMethodId?.let {
                paramsBuilder.setPaymentMethod(it)
                paramsBuilder.setConfirm(true)
                paramsBuilder.setReturnUrl(request.returnUrl ?: "${appProperties.cors.allowedOrigins.split(",").first()}/payment/callback")
            }

            val paymentIntent = PaymentIntent.create(paramsBuilder.build())

            logger.info { "Created PaymentIntent ${paymentIntent.id} for user ${user.id}" }

            return PaymentIntentResponse(
                paymentIntentId = paymentIntent.id,
                clientSecret = paymentIntent.clientSecret,
                status = paymentIntent.status,
                amount = request.amount,
                currency = request.currency
            )
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create PaymentIntent for user ${user.id}" }
            throw PaymentException("Impossible de créer le paiement: ${e.message}", e)
        }
    }

    fun confirmPaymentIntent(paymentIntentId: String, paymentMethodId: String): PaymentIntent {
        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            val params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .build()
            return paymentIntent.confirm(params)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to confirm PaymentIntent $paymentIntentId" }
            throw PaymentException("Impossible de confirmer le paiement", e)
        }
    }

    fun cancelPaymentIntent(paymentIntentId: String): PaymentIntent {
        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            return paymentIntent.cancel()
        } catch (e: StripeException) {
            logger.error(e) { "Failed to cancel PaymentIntent $paymentIntentId" }
            throw PaymentException("Impossible d'annuler le paiement", e)
        }
    }

    fun retrievePaymentIntent(paymentIntentId: String): PaymentIntent {
        try {
            return PaymentIntent.retrieve(paymentIntentId)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to retrieve PaymentIntent $paymentIntentId" }
            throw PaymentException("Impossible de récupérer le paiement", e)
        }
    }

    // ==========================================
    // Setup Intent (pour sauvegarder des méthodes de paiement)
    // ==========================================

    fun createSetupIntent(user: User): SetupIntentResponse {
        try {
            val customerId = getOrCreateCustomer(user)

            val params = SetupIntentCreateParams.builder()
                .setCustomer(customerId)
                .addPaymentMethodType("card")
                .addPaymentMethodType("link") // Stripe Link (wallet rapide)
                .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
                .putMetadata("user_id", user.id.toString())
                .build()

            val setupIntent = SetupIntent.create(params)

            logger.info { "Created SetupIntent ${setupIntent.id} for user ${user.id}" }

            return SetupIntentResponse(
                setupIntentId = setupIntent.id,
                clientSecret = setupIntent.clientSecret,
                status = setupIntent.status
            )
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create SetupIntent for user ${user.id}" }
            throw PaymentException("Impossible de créer la configuration de paiement", e)
        }
    }

    // ==========================================
    // Payment Methods Management
    // ==========================================

    @Transactional
    fun attachPaymentMethod(paymentMethodId: String, user: User): UserPaymentMethod {
        try {
            val customerId = getOrCreateCustomer(user)

            val paymentMethod = PaymentMethod.retrieve(paymentMethodId)
            paymentMethod.attach(
                PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build()
            )

            // Déterminer le type de méthode
            val (type, walletType) = determinePaymentMethodType(paymentMethod)

            // Sauvegarder en base
            val userPaymentMethod = UserPaymentMethod(
                user = user,
                type = type,
                stripePaymentMethodId = paymentMethodId,
                brand = paymentMethod.card?.brand,
                last4 = paymentMethod.card?.last4,
                expMonth = paymentMethod.card?.expMonth?.toInt(),
                expYear = paymentMethod.card?.expYear?.toInt(),
                walletType = walletType,
                isDefault = paymentMethodRepository.findByUserId(user.id!!).isEmpty()
            )

            logger.info { "Attached payment method $paymentMethodId for user ${user.id}" }

            return paymentMethodRepository.save(userPaymentMethod)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to attach payment method $paymentMethodId" }
            throw PaymentException("Impossible d'ajouter la méthode de paiement", e)
        }
    }

    fun listPaymentMethods(user: User): List<UserPaymentMethod> {
        return paymentMethodRepository.findByUserId(user.id!!)
    }

    @Transactional
    fun detachPaymentMethod(paymentMethodId: String, user: User) {
        try {
            val paymentMethod = PaymentMethod.retrieve(paymentMethodId)
            paymentMethod.detach()

            paymentMethodRepository.deactivateByStripeId(paymentMethodId)

            logger.info { "Detached payment method $paymentMethodId for user ${user.id}" }
        } catch (e: StripeException) {
            logger.error(e) { "Failed to detach payment method $paymentMethodId" }
            throw PaymentException("Impossible de supprimer la méthode de paiement", e)
        }
    }

    @Transactional
    fun setDefaultPaymentMethod(paymentMethodId: String, user: User) {
        val customerId = getOrCreateCustomer(user)

        try {
            val customer = Customer.retrieve(customerId)
            customer.update(
                CustomerUpdateParams.builder()
                    .setInvoiceSettings(
                        CustomerUpdateParams.InvoiceSettings.builder()
                            .setDefaultPaymentMethod(paymentMethodId)
                            .build()
                    )
                    .build()
            )

            // Mettre à jour en base
            paymentMethodRepository.clearDefaultForUser(user.id!!)
            paymentMethodRepository.findByStripePaymentMethodId(paymentMethodId)?.let {
                it.isDefault = true
                paymentMethodRepository.save(it)
            }

            logger.info { "Set default payment method $paymentMethodId for user ${user.id}" }
        } catch (e: StripeException) {
            logger.error(e) { "Failed to set default payment method $paymentMethodId" }
            throw PaymentException("Impossible de définir la méthode de paiement par défaut", e)
        }
    }

    // ==========================================
    // Apple Pay Domain Verification
    // ==========================================

    fun registerApplePayDomain(domain: String) {
        try {
            val params = ApplePayDomainCreateParams.builder()
                .setDomainName(domain)
                .build()

            ApplePayDomain.create(params)
            logger.info { "Registered Apple Pay domain: $domain" }
        } catch (e: StripeException) {
            logger.error(e) { "Failed to register Apple Pay domain: $domain" }
            throw PaymentException("Impossible d'enregistrer le domaine Apple Pay", e)
        }
    }

    fun listApplePayDomains(): List<ApplePayDomain> {
        return try {
            ApplePayDomain.list(ApplePayDomainListParams.builder().build()).data
        } catch (e: StripeException) {
            logger.error(e) { "Failed to list Apple Pay domains" }
            emptyList()
        }
    }

    // ==========================================
    // Refunds
    // ==========================================

    @Transactional
    fun createRefund(request: CreateRefundRequest, user: User): Refund {
        val payment = paymentRepository.findById(request.paymentId)
            .orElseThrow { PaymentException("Paiement non trouvé") }

        if (payment.stripePaymentIntentId == null) {
            throw PaymentException("Ce paiement ne peut pas être remboursé")
        }

        try {
            val paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(payment.stripePaymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)

            request.amount?.let {
                paramsBuilder.setAmount(convertToStripeAmount(it, payment.devise))
            }

            request.reason?.let {
                paramsBuilder.putMetadata("reason", it)
            }

            val stripeRefund = com.stripe.model.Refund.create(paramsBuilder.build())

            val refund = com.tonti.entity.Refund(
                payment = payment,
                montant = request.amount ?: payment.montant,
                stripeRefundId = stripeRefund.id,
                statut = mapRefundStatus(stripeRefund.status),
                raison = request.reason
            )

            if (stripeRefund.status == "succeeded") {
                refund.completedAt = Instant.now()
                payment.statut = if (request.amount == null || request.amount >= payment.montant) {
                    PaymentStatus.REFUNDED
                } else {
                    PaymentStatus.PARTIALLY_REFUNDED
                }
                payment.refundedAt = Instant.now()
                paymentRepository.save(payment)
            }

            logger.info { "Created refund ${stripeRefund.id} for payment ${payment.id}" }

            return refundRepository.save(refund)
        } catch (e: StripeException) {
            logger.error(e) { "Failed to create refund for payment ${payment.id}" }
            throw PaymentException("Impossible de créer le remboursement: ${e.message}", e)
        }
    }

    // ==========================================
    // Webhooks
    // ==========================================

    @Transactional
    fun handleWebhook(payload: String, signature: String): String {
        val event = try {
            Webhook.constructEvent(payload, signature, appProperties.stripe.webhookSecret)
        } catch (e: SignatureVerificationException) {
            logger.error(e) { "Invalid webhook signature" }
            throw PaymentException("Signature webhook invalide")
        }

        // Vérifier si l'événement a déjà été traité (idempotence)
        if (stripeEventRepository.existsByStripeEventId(event.id)) {
            logger.info { "Event ${event.id} already processed, skipping" }
            return "Event already processed"
        }

        // Enregistrer l'événement
        val stripeEvent = StripeEvent(
            stripeEventId = event.id,
            type = event.type,
            data = event.data.toJson()
        )
        stripeEventRepository.save(stripeEvent)

        try {
            when (event.type) {
                "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event)
                "payment_intent.payment_failed" -> handlePaymentIntentFailed(event)
                "payment_intent.canceled" -> handlePaymentIntentCanceled(event)
                "payment_method.attached" -> handlePaymentMethodAttached(event)
                "payment_method.detached" -> handlePaymentMethodDetached(event)
                "charge.refunded" -> handleChargeRefunded(event)
                "customer.deleted" -> handleCustomerDeleted(event)
                else -> logger.info { "Unhandled event type: ${event.type}" }
            }

            stripeEvent.processed = true
            stripeEvent.processedAt = Instant.now()
            stripeEventRepository.save(stripeEvent)

            logger.info { "Processed webhook event ${event.id} of type ${event.type}" }
            return "Webhook processed successfully"
        } catch (e: Exception) {
            stripeEvent.error = e.message
            stripeEventRepository.save(stripeEvent)
            throw e
        }
    }

    private fun handlePaymentIntentSucceeded(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
            ?: return

        paymentRepository.findByStripePaymentIntentId(paymentIntent.id)?.let { payment ->
            payment.markAsSucceeded()
            payment.stripeChargeId = paymentIntent.latestCharge
            paymentRepository.save(payment)
            logger.info { "Payment ${payment.id} marked as succeeded" }
        }
    }

    private fun handlePaymentIntentFailed(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
            ?: return

        paymentRepository.findByStripePaymentIntentId(paymentIntent.id)?.let { payment ->
            payment.markAsFailed(
                paymentIntent.lastPaymentError?.message,
                paymentIntent.lastPaymentError?.code
            )
            paymentRepository.save(payment)
            logger.info { "Payment ${payment.id} marked as failed" }
        }
    }

    private fun handlePaymentIntentCanceled(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
            ?: return

        paymentRepository.findByStripePaymentIntentId(paymentIntent.id)?.let { payment ->
            payment.statut = PaymentStatus.CANCELLED
            paymentRepository.save(payment)
            logger.info { "Payment ${payment.id} marked as cancelled" }
        }
    }

    private fun handlePaymentMethodAttached(event: Event) {
        val paymentMethod = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentMethod
            ?: return

        val customerId = paymentMethod.customer
        userRepository.findByStripeCustomerId(customerId)?.let { user ->
            // La méthode est probablement déjà attachée via notre API
            // On met juste à jour si nécessaire
            logger.info { "Payment method ${paymentMethod.id} attached to customer $customerId" }
        }
    }

    private fun handlePaymentMethodDetached(event: Event) {
        val paymentMethod = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentMethod
            ?: return

        paymentMethodRepository.deactivateByStripeId(paymentMethod.id)
        logger.info { "Payment method ${paymentMethod.id} detached" }
    }

    private fun handleChargeRefunded(event: Event) {
        val charge = event.dataObjectDeserializer.`object`.orElse(null) as? Charge
            ?: return

        paymentRepository.findByStripePaymentIntentId(charge.paymentIntent)?.let { payment ->
            if (charge.refunded) {
                payment.statut = PaymentStatus.REFUNDED
            } else if (charge.amountRefunded > 0) {
                payment.statut = PaymentStatus.PARTIALLY_REFUNDED
            }
            payment.refundedAt = Instant.now()
            paymentRepository.save(payment)
            logger.info { "Payment ${payment.id} refund status updated" }
        }
    }

    private fun handleCustomerDeleted(event: Event) {
        val customer = event.dataObjectDeserializer.`object`.orElse(null) as? Customer
            ?: return

        userRepository.findByStripeCustomerId(customer.id)?.let { user ->
            user.stripeCustomerId = null
            userRepository.save(user)
            logger.info { "Customer ${customer.id} deleted, user ${user.id} updated" }
        }
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private fun convertToStripeAmount(amount: BigDecimal, currency: Currency): Long {
        // Stripe utilise les centimes pour la plupart des devises
        return when (currency) {
            Currency.MAD, Currency.EUR, Currency.USD -> (amount * BigDecimal(100)).toLong()
        }
    }

    private fun determinePaymentMethodType(pm: PaymentMethod): Pair<PaymentMethodType, WalletType?> {
        return when {
            pm.card?.wallet?.type == "apple_pay" -> PaymentMethodType.APPLE_PAY to WalletType.APPLE_PAY
            pm.card?.wallet?.type == "google_pay" -> PaymentMethodType.GOOGLE_PAY to WalletType.GOOGLE_PAY
            pm.card?.wallet?.type == "samsung_pay" -> PaymentMethodType.CARD to WalletType.SAMSUNG_PAY
            pm.card?.wallet?.type == "link" -> PaymentMethodType.CARD to WalletType.LINK
            pm.card != null -> PaymentMethodType.CARD to null
            pm.sepaDebit != null -> PaymentMethodType.SEPA_DEBIT to null
            else -> PaymentMethodType.CARD to null
        }
    }

    private fun mapRefundStatus(status: String): RefundStatus {
        return when (status) {
            "succeeded" -> RefundStatus.SUCCEEDED
            "pending" -> RefundStatus.PENDING
            "failed" -> RefundStatus.FAILED
            "canceled" -> RefundStatus.CANCELLED
            else -> RefundStatus.PENDING
        }
    }
}
