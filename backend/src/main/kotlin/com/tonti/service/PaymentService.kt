package com.tonti.service

import com.tonti.dto.payment.*
import com.tonti.entity.*
import com.tonti.event.*
import com.tonti.exception.*
import com.tonti.repository.*
import com.tonti.service.payment.StripeService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class PaymentService(
    private val stripeService: StripeService,
    private val paymentRepository: PaymentRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val roundRepository: RoundRepository,
    private val daretService: DaretService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createPayment(request: CreatePaymentIntentRequest, user: User): PaymentIntentResponse {
        val daret = daretService.findById(request.daretId)
        val membre = daretService.checkIsMember(request.daretId, user.id!!)

        val round = roundRepository.findById(request.roundId)
            .orElseThrow { NotFoundException("Round non trouvé") }

        if (round.daret.id != daret.id) {
            throw BadRequestException("Ce round n'appartient pas à ce Daret")
        }

        if (round.estClos) {
            throw BadRequestException("Ce round est déjà clos")
        }

        // Vérifier que le membre n'est pas le receveur de ce round
        if (round.receveur.id == membre.id) {
            throw BadRequestException("Vous êtes le bénéficiaire de ce round et n'avez pas à payer")
        }

        // Vérifier qu'il n'y a pas déjà un paiement en cours ou réussi
        val existingPayment = paymentRepository.findByUserIdAndRoundId(user.id!!, round.id!!)
        if (existingPayment != null && existingPayment.statut in listOf(PaymentStatus.SUCCEEDED, PaymentStatus.PROCESSING)) {
            throw ConflictException("Vous avez déjà effectué un paiement pour ce round")
        }

        // Créer le PaymentIntent Stripe
        val paymentIntentResponse = stripeService.createPaymentIntent(request, user)

        // Créer l'enregistrement de paiement
        val payment = Payment(
            user = user,
            daret = daret,
            round = round,
            montant = request.amount,
            devise = request.currency,
            stripePaymentIntentId = paymentIntentResponse.paymentIntentId,
            statut = PaymentStatus.PENDING,
            methode = determinePaymentMethod(request.paymentMethodId, user)
        )
        paymentRepository.save(payment)

        logger.info { "Created payment ${payment.id} for user ${user.id} in round ${round.id}" }

        eventPublisher.publishEvent(PaymentCreatedEvent(
            paymentId = payment.id!!,
            userId = user.id!!,
            daretId = daret.id!!,
            roundId = round.id!!,
            montant = request.amount,
            devise = daret.devise
        ))

        return paymentIntentResponse
    }

    fun confirmPayment(request: ConfirmPaymentRequest, user: User): PaymentResponse {
        val payment = paymentRepository.findByStripePaymentIntentId(request.paymentIntentId)
            ?: throw NotFoundException("Paiement non trouvé")

        if (payment.user.id != user.id) {
            throw ForbiddenException("Ce paiement ne vous appartient pas")
        }

        val paymentIntent = stripeService.confirmPaymentIntent(request.paymentIntentId, request.paymentMethodId)

        payment.statut = when (paymentIntent.status) {
            "succeeded" -> PaymentStatus.SUCCEEDED
            "processing" -> PaymentStatus.PROCESSING
            "requires_action" -> PaymentStatus.REQUIRES_ACTION
            else -> PaymentStatus.PENDING
        }

        if (payment.statut == PaymentStatus.SUCCEEDED) {
            payment.markAsSucceeded()
        }

        paymentRepository.save(payment)

        if (payment.statut == PaymentStatus.SUCCEEDED) {
            eventPublisher.publishEvent(PaymentSucceededEvent(
                paymentId = payment.id!!,
                userId = payment.user.id!!,
                userName = payment.user.fullName(),
                daretId = payment.daret.id!!,
                daretNom = payment.daret.nom,
                roundId = payment.round.id!!,
                roundNumero = payment.round.numero,
                receveurId = payment.round.receveur.user.id!!,
                montant = payment.montant,
                devise = payment.daret.devise
            ))
        }

        return toPaymentResponse(payment)
    }

    fun getPayment(paymentId: UUID, user: User): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { NotFoundException("Paiement non trouvé") }

        // Vérifier les droits d'accès
        if (payment.user.id != user.id) {
            daretService.checkIsMember(payment.daret.id!!, user.id!!)
        }

        return toPaymentResponse(payment)
    }

    fun getUserPayments(userId: UUID, pageable: Pageable): Page<PaymentResponse> {
        return paymentRepository.findByUserId(userId, pageable)
            .map { toPaymentResponse(it) }
    }

    fun getRoundPayments(daretId: UUID, roundId: UUID, userId: UUID): List<PaymentResponse> {
        daretService.checkIsMember(daretId, userId)

        val round = roundRepository.findById(roundId)
            .orElseThrow { NotFoundException("Round non trouvé") }

        if (round.daret.id != daretId) {
            throw BadRequestException("Ce round n'appartient pas à ce Daret")
        }

        return paymentRepository.findByRoundId(roundId)
            .map { toPaymentResponse(it) }
    }

    @Transactional
    fun cancelPayment(paymentId: UUID, user: User): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { NotFoundException("Paiement non trouvé") }

        if (payment.user.id != user.id) {
            throw ForbiddenException("Ce paiement ne vous appartient pas")
        }

        if (payment.statut !in listOf(PaymentStatus.PENDING, PaymentStatus.REQUIRES_ACTION)) {
            throw BadRequestException("Ce paiement ne peut plus être annulé")
        }

        payment.stripePaymentIntentId?.let { stripeService.cancelPaymentIntent(it) }
        payment.statut = PaymentStatus.CANCELLED
        paymentRepository.save(payment)

        logger.info { "Cancelled payment ${payment.id}" }

        eventPublisher.publishEvent(PaymentCancelledEvent(
            paymentId = payment.id!!,
            userId = user.id!!,
            daretId = payment.daret.id!!,
            roundId = payment.round.id!!
        ))

        return toPaymentResponse(payment)
    }

    // ==========================================
    // Payment Methods
    // ==========================================

    fun createSetupIntent(user: User): SetupIntentResponse {
        return stripeService.createSetupIntent(user)
    }

    @Transactional
    fun attachPaymentMethod(paymentMethodId: String, user: User): PaymentMethodResponse {
        val userPaymentMethod = stripeService.attachPaymentMethod(paymentMethodId, user)
        return toPaymentMethodResponse(userPaymentMethod)
    }

    fun listPaymentMethods(user: User): List<PaymentMethodResponse> {
        return stripeService.listPaymentMethods(user)
            .map { toPaymentMethodResponse(it) }
    }

    @Transactional
    fun removePaymentMethod(paymentMethodId: String, user: User) {
        stripeService.detachPaymentMethod(paymentMethodId, user)
    }

    @Transactional
    fun setDefaultPaymentMethod(paymentMethodId: String, user: User) {
        stripeService.setDefaultPaymentMethod(paymentMethodId, user)
    }

    // ==========================================
    // Refunds
    // ==========================================

    @Transactional
    fun createRefund(request: CreateRefundRequest, user: User): RefundResponse {
        val payment = paymentRepository.findById(request.paymentId)
            .orElseThrow { NotFoundException("Paiement non trouvé") }

        // Seuls les admins du Daret peuvent créer des remboursements
        daretService.checkIsMember(payment.daret.id!!, user.id!!)

        val refund = stripeService.createRefund(request, user)

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

    private fun determinePaymentMethod(paymentMethodId: String?, user: User): PaymentType {
        if (paymentMethodId == null) return PaymentType.CARD

        return paymentMethodRepository.findByStripePaymentMethodId(paymentMethodId)?.let { pm ->
            when (pm.walletType) {
                WalletType.APPLE_PAY -> PaymentType.APPLE_PAY
                WalletType.GOOGLE_PAY -> PaymentType.GOOGLE_PAY
                else -> PaymentType.CARD
            }
        } ?: PaymentType.CARD
    }

    private fun toPaymentResponse(payment: Payment): PaymentResponse {
        return PaymentResponse(
            id = payment.id!!,
            daretId = payment.daret.id!!,
            roundId = payment.round.id!!,
            amount = payment.montant,
            currency = payment.devise,
            status = payment.statut,
            method = payment.methode,
            paidAt = payment.paidAt,
            createdAt = payment.createdAt
        )
    }

    private fun toPaymentMethodResponse(pm: UserPaymentMethod): PaymentMethodResponse {
        return PaymentMethodResponse(
            id = pm.id!!,
            type = pm.type.name,
            brand = pm.brand,
            last4 = pm.last4,
            expMonth = pm.expMonth,
            expYear = pm.expYear,
            walletType = pm.walletType?.name,
            isDefault = pm.isDefault
        )
    }
}
