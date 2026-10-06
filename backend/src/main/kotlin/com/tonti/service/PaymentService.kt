package com.tonti.service

import com.tonti.dto.payment.CheckoutRequest
import com.tonti.dto.payment.CheckoutResponse
import com.tonti.dto.payment.CreateRefundRequest
import com.tonti.dto.payment.PaymentConfigResponse
import com.tonti.dto.payment.PaymentResponse
import com.tonti.dto.payment.RefundResponse
import com.tonti.entity.Currency
import com.tonti.entity.DaretStatus
import com.tonti.entity.Payment
import com.tonti.entity.PaymentStatus
import com.tonti.entity.Refund
import com.tonti.entity.RefundStatus
import com.tonti.entity.User
import com.tonti.event.PaymentCancelledEvent
import com.tonti.event.PaymentCreatedEvent
import com.tonti.event.PaymentFailedEvent
import com.tonti.event.PaymentSucceededEvent
import com.tonti.event.RefundCreatedEvent
import com.tonti.exception.BadRequestException
import com.tonti.exception.ConflictException
import com.tonti.exception.ForbiddenException
import com.tonti.exception.NotFoundException
import com.tonti.exception.PaymentUnavailableException
import com.tonti.repository.PaymentRepository
import com.tonti.repository.RefundRepository
import com.tonti.repository.RoundRepository
import com.tonti.service.payment.PaymentGateway
import com.tonti.service.payment.StripeCheckoutGateway
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class PaymentService(
    private val gateways: List<PaymentGateway>,
    private val stripeGateway: StripeCheckoutGateway,
    private val paymentRepository: PaymentRepository,
    private val refundRepository: RefundRepository,
    private val roundRepository: RoundRepository,
    private val daretService: DaretService,
    private val eventPublisher: ApplicationEventPublisher
) {

    // ==========================================
    // Checkout
    // ==========================================

    @Transactional
    fun checkout(request: CheckoutRequest, user: User): CheckoutResponse {
        val daret = daretService.findById(request.daretId)
        val membre = daretService.checkIsMember(request.daretId, user.id!!)

        if (daret.etat != DaretStatus.ACTIVE) {
            throw BadRequestException("Les cotisations ne sont ouvertes que pour un Daret actif")
        }

        val round = roundRepository.findById(request.roundId)
            .orElseThrow { NotFoundException("Round non trouvé") }

        if (round.daret.id != daret.id) {
            throw BadRequestException("Ce round n'appartient pas à ce Daret")
        }
        if (round.estClos) {
            throw BadRequestException("Ce round est déjà clos")
        }
        if (round.receveur.id == membre.id) {
            throw BadRequestException("Vous êtes le bénéficiaire de ce round et n'avez pas à payer")
        }
        if (paymentRepository.existsSucceededByUserIdAndRoundId(user.id!!, round.id!!)) {
            throw ConflictException("Vous avez déjà payé votre cotisation pour ce round")
        }

        val gateway = gatewayFor(daret.devise)

        // Une seule tentative ouverte à la fois : les précédentes sont abandonnées
        paymentRepository.findByUserIdAndRoundIdAndStatutIn(user.id!!, round.id!!, Payment.OPEN_STATUSES)
            .forEach { supersede(it) }

        val payment = paymentRepository.save(
            Payment(
                user = user,
                daret = daret,
                round = round,
                montant = daret.montantMensuel,
                devise = daret.devise,
                provider = gateway.provider,
                channel = request.channel,
                statut = PaymentStatus.PENDING
            )
        )

        val session = gateway.createCheckout(payment, user, request.locale)
        payment.providerOrderId = session.providerOrderId
        paymentRepository.save(payment)

        logger.info { "Checkout ${payment.id} (${gateway.provider}) for user ${user.id} on round ${round.id}" }

        eventPublisher.publishEvent(
            PaymentCreatedEvent(
                paymentId = payment.id!!,
                userId = user.id!!,
                daretId = daret.id!!,
                roundId = round.id!!,
                montant = payment.montant,
                devise = payment.devise
            )
        )

        return CheckoutResponse(
            paymentId = payment.id!!,
            provider = payment.provider,
            status = payment.statut,
            amount = payment.montant,
            currency = payment.devise,
            redirectUrl = session.redirectUrl
        )
    }

    fun paymentConfig(): PaymentConfigResponse {
        val currencies = Currency.entries.filter { currency -> gateways.any { it.supports(currency) } }
        return PaymentConfigResponse(
            onlinePaymentCurrencies = currencies,
            provider = gateways.firstOrNull { gateway -> currencies.any { gateway.supports(it) } }?.provider
        )
    }

    // ==========================================
    // Résultats PSP (appelés par les webhooks)
    // ==========================================

    /**
     * Le PSP confirme l'encaissement. Idempotent : un paiement déjà réussi est ignoré.
     * Si le membre a déjà une cotisation réussie pour ce round (double paiement), le
     * second encaissement est automatiquement remboursé.
     */
    @Transactional
    fun onPaymentSucceeded(payment: Payment, transactionId: String?) {
        if (payment.isSucceeded() || payment.statut in REFUND_STATUSES) {
            logger.info { "Payment ${payment.id} already settled (${payment.statut}), ignoring confirmation" }
            return
        }

        payment.providerTransactionId = transactionId
        payment.stripePaymentIntentId = transactionId

        val alreadyPaid = paymentRepository.existsSucceededByUserIdAndRoundId(payment.user.id!!, payment.round.id!!)
        if (alreadyPaid) {
            logger.warn { "Duplicate payment ${payment.id} for user ${payment.user.id} on round ${payment.round.id}: refunding" }
            refundDuplicate(payment)
            return
        }

        payment.markAsSucceeded()
        paymentRepository.save(payment)
        logger.info { "Payment ${payment.id} succeeded" }

        eventPublisher.publishEvent(
            PaymentSucceededEvent(
                paymentId = payment.id!!,
                userId = payment.user.id!!,
                userName = payment.user.fullName(),
                daretId = payment.daret.id!!,
                daretNom = payment.daret.nom,
                roundId = payment.round.id!!,
                roundNumero = payment.round.numero,
                receveurId = payment.round.receveur.user.id!!,
                montant = payment.montant,
                devise = payment.devise
            )
        )
    }

    @Transactional
    fun onPaymentFailed(payment: Payment, reason: String?, code: String?) {
        if (!payment.isOpen()) return
        payment.markAsFailed(reason, code)
        paymentRepository.save(payment)
        logger.info { "Payment ${payment.id} failed: $code" }

        eventPublisher.publishEvent(
            PaymentFailedEvent(
                paymentId = payment.id!!,
                userId = payment.user.id!!,
                daretId = payment.daret.id!!,
                roundId = payment.round.id!!,
                errorMessage = reason,
                errorCode = code
            )
        )
    }

    /** Session expirée ou abandonnée chez le PSP. */
    @Transactional
    fun onPaymentExpired(payment: Payment) {
        if (!payment.isOpen()) return
        payment.statut = PaymentStatus.CANCELLED
        paymentRepository.save(payment)
        publishCancelled(payment)
    }

    @Transactional
    fun onRefunded(payment: Payment, fullyRefunded: Boolean) {
        payment.statut = if (fullyRefunded) PaymentStatus.REFUNDED else PaymentStatus.PARTIALLY_REFUNDED
        payment.refundedAt = Instant.now()
        paymentRepository.save(payment)
    }

    // ==========================================
    // Consultation
    // ==========================================

    @Transactional(readOnly = true)
    fun getPayment(paymentId: UUID, user: User): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { NotFoundException("Paiement non trouvé") }

        if (payment.user.id != user.id) {
            daretService.checkIsMember(payment.daret.id!!, user.id!!)
        }

        return toPaymentResponse(payment)
    }

    @Transactional(readOnly = true)
    fun getUserPayments(userId: UUID, pageable: Pageable): Page<PaymentResponse> =
        paymentRepository.findByUserId(userId, pageable).map { toPaymentResponse(it) }

    @Transactional(readOnly = true)
    fun getRoundPayments(daretId: UUID, roundId: UUID, userId: UUID): List<PaymentResponse> {
        daretService.checkIsMember(daretId, userId)

        val round = roundRepository.findById(roundId)
            .orElseThrow { NotFoundException("Round non trouvé") }

        if (round.daret.id != daretId) {
            throw BadRequestException("Ce round n'appartient pas à ce Daret")
        }

        return paymentRepository.findByRoundId(roundId).map { toPaymentResponse(it) }
    }

    @Transactional
    fun cancelPayment(paymentId: UUID, user: User): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { NotFoundException("Paiement non trouvé") }

        if (payment.user.id != user.id) {
            throw ForbiddenException("Ce paiement ne vous appartient pas")
        }
        if (!payment.isOpen()) {
            throw BadRequestException("Ce paiement ne peut plus être annulé")
        }

        supersede(payment)
        logger.info { "Cancelled payment ${payment.id}" }
        return toPaymentResponse(payment)
    }

    // ==========================================
    // Remboursements (administrateurs du Daret)
    // ==========================================

    @Transactional
    fun createRefund(request: CreateRefundRequest, user: User): RefundResponse {
        val payment = paymentRepository.findById(request.paymentId)
            .orElseThrow { NotFoundException("Paiement non trouvé") }

        daretService.checkIsAdmin(payment.daret, user.id!!)

        if (!payment.isSucceeded()) {
            throw BadRequestException("Seul un paiement réussi peut être remboursé")
        }
        if (payment.round.estClos) {
            throw BadRequestException("Le round est clos : la cagnotte a déjà été attribuée")
        }
        val amount = request.amount ?: payment.montant
        if (amount > payment.montant) {
            throw BadRequestException("Le montant remboursé dépasse le montant payé")
        }
        val transactionId = payment.providerTransactionId
            ?: throw BadRequestException("Ce paiement ne peut pas être remboursé automatiquement")

        val stripeRefund = stripeGateway.refund(transactionId, request.amount, request.reason)
        val refund = refundRepository.save(
            Refund(
                payment = payment,
                montant = amount,
                stripeRefundId = stripeRefund.id,
                statut = mapRefundStatus(stripeRefund.status),
                raison = request.reason
            ).apply { if (statut == RefundStatus.SUCCEEDED) completedAt = Instant.now() }
        )

        onRefunded(payment, fullyRefunded = amount.compareTo(payment.montant) == 0)

        eventPublisher.publishEvent(
            RefundCreatedEvent(
                refundId = refund.id!!,
                paymentId = payment.id!!,
                userId = payment.user.id!!,
                montant = refund.montant,
                raison = refund.raison
            )
        )

        return RefundResponse(
            id = refund.id!!,
            paymentId = payment.id!!,
            amount = refund.montant,
            status = refund.statut.name,
            reason = refund.raison,
            createdAt = refund.createdAt
        )
    }

    // ==========================================
    // Helpers
    // ==========================================

    private fun gatewayFor(currency: Currency): PaymentGateway =
        gateways.firstOrNull { it.supports(currency) }
            ?: throw PaymentUnavailableException("Le paiement en ligne n'est pas disponible pour la devise $currency")

    private fun supersede(payment: Payment) {
        gateways.firstOrNull { it.provider == payment.provider }?.let { gateway ->
            runCatching { gateway.cancelCheckout(payment) }
                .onFailure { logger.warn(it) { "Could not cancel checkout for payment ${payment.id}" } }
        }
        payment.statut = PaymentStatus.CANCELLED
        paymentRepository.save(payment)
        publishCancelled(payment)
    }

    private fun refundDuplicate(payment: Payment) {
        val transactionId = payment.providerTransactionId
        if (transactionId == null) {
            payment.markAsFailed("Paiement en double", "DUPLICATE")
            paymentRepository.save(payment)
            return
        }
        val stripeRefund = stripeGateway.refund(transactionId, null, "Paiement en double")
        refundRepository.save(
            Refund(
                payment = payment,
                montant = payment.montant,
                stripeRefundId = stripeRefund.id,
                statut = mapRefundStatus(stripeRefund.status),
                raison = "Paiement en double"
            )
        )
        payment.failureReason = "Paiement en double, remboursé automatiquement"
        payment.failureCode = "DUPLICATE"
        onRefunded(payment, fullyRefunded = true)
    }

    private fun publishCancelled(payment: Payment) {
        eventPublisher.publishEvent(
            PaymentCancelledEvent(
                paymentId = payment.id!!,
                userId = payment.user.id!!,
                daretId = payment.daret.id!!,
                roundId = payment.round.id!!
            )
        )
    }

    private fun mapRefundStatus(status: String?): RefundStatus = when (status) {
        "succeeded" -> RefundStatus.SUCCEEDED
        "failed" -> RefundStatus.FAILED
        "canceled" -> RefundStatus.CANCELLED
        else -> RefundStatus.PENDING
    }

    fun toPaymentResponse(payment: Payment): PaymentResponse = PaymentResponse(
        id = payment.id!!,
        daretId = payment.daret.id!!,
        roundId = payment.round.id!!,
        roundNumero = payment.round.numero,
        userId = payment.user.id!!,
        userName = payment.user.fullName(),
        amount = payment.montant,
        currency = payment.devise,
        status = payment.statut,
        provider = payment.provider,
        method = payment.methode,
        failureReason = payment.failureReason,
        paidAt = payment.paidAt,
        createdAt = payment.createdAt
    )

    private companion object {
        val REFUND_STATUSES = setOf(PaymentStatus.REFUNDED, PaymentStatus.PARTIALLY_REFUNDED)
    }
}
