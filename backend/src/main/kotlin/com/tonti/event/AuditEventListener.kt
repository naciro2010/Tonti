package com.tonti.event

import com.tonti.service.AuditService
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class AuditEventListener(
    private val auditService: AuditService
) {

    // ==========================================
    // User Events
    // ==========================================

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onUserRegistered(event: UserRegisteredEvent) {
        auditService.log(
            userId = event.userId,
            action = "USER_REGISTERED",
            entity = "User",
            entityId = event.userId.toString(),
            newData = """{"email": "${event.email}", "firstName": "${event.firstName}", "lastName": "${event.lastName}"}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onUserProfileUpdated(event: UserProfileUpdatedEvent) {
        auditService.log(
            userId = event.userId,
            action = "PROFILE_UPDATED",
            entity = "User",
            entityId = event.userId.toString(),
            newData = """{"firstName": "${event.firstName}", "lastName": "${event.lastName}"}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onUserLoggedIn(event: UserLoggedInEvent) {
        auditService.log(
            userId = event.userId,
            action = "USER_LOGIN",
            entity = "User",
            entityId = event.userId.toString()
        )
    }

    // ==========================================
    // Daret Events
    // ==========================================

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onDaretCreated(event: DaretCreatedEvent) {
        auditService.log(
            userId = event.createurId,
            action = "DARET_CREATED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"nom": "${event.nom}", "montant": ${event.montantMensuel}, "devise": "${event.devise}", "taille": ${event.taille}}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onDaretUpdated(event: DaretUpdatedEvent) {
        auditService.log(
            userId = event.updatedByUserId,
            action = "DARET_UPDATED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"nom": "${event.nom}"}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onDaretStarted(event: DaretStartedEvent) {
        auditService.log(
            userId = event.startedByUserId,
            action = "DARET_STARTED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"membresCount": ${event.membresCount}, "dateDebut": "${event.dateDebut}", "dateFin": "${event.dateFin}"}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onDaretCompleted(event: DaretCompletedEvent) {
        auditService.log(
            userId = null,
            action = "DARET_COMPLETED",
            entity = "Daret",
            entityId = event.daretId.toString()
        )
    }

    // ==========================================
    // Member Events
    // ==========================================

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onMemberJoined(event: MemberJoinedEvent) {
        auditService.log(
            userId = event.userId,
            action = "MEMBER_JOINED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"userId": "${event.userId}", "userName": "${event.userName}"}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onMemberLeft(event: MemberLeftEvent) {
        auditService.log(
            userId = event.userId,
            action = "MEMBER_LEFT",
            entity = "Daret",
            entityId = event.daretId.toString()
        )
    }

    // ==========================================
    // Round Events
    // ==========================================

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onRoundClosed(event: RoundClosedEvent) {
        auditService.log(
            userId = null,
            action = "ROUND_CLOSED",
            entity = "Round",
            entityId = event.roundId.toString(),
            newData = """{"daretId": "${event.daretId}", "numero": ${event.roundNumero}, "receveur": "${event.receveurName}", "montantTotal": ${event.montantTotal}}"""
        )
    }

    // ==========================================
    // Payment Events
    // ==========================================

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentCreated(event: PaymentCreatedEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_CREATED",
            entity = "Payment",
            entityId = event.paymentId.toString(),
            newData = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}", "montant": ${event.montant}, "devise": "${event.devise}"}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentSucceeded(event: PaymentSucceededEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_SUCCEEDED",
            entity = "Payment",
            entityId = event.paymentId.toString(),
            newData = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}", "montant": ${event.montant}}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentFailed(event: PaymentFailedEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_FAILED",
            entity = "Payment",
            entityId = event.paymentId.toString(),
            newData = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}", "error": "${event.errorMessage ?: ""}"}"""
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentCancelled(event: PaymentCancelledEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_CANCELLED",
            entity = "Payment",
            entityId = event.paymentId.toString()
        )
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onRefundCreated(event: RefundCreatedEvent) {
        auditService.log(
            userId = event.userId,
            action = "REFUND_CREATED",
            entity = "Refund",
            entityId = event.refundId.toString(),
            newData = """{"paymentId": "${event.paymentId}", "montant": ${event.montant}, "raison": "${event.raison ?: ""}"}"""
        )
    }
}
