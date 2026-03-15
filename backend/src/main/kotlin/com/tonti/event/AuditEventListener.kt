package com.tonti.event

import com.tonti.service.AuditService
import org.springframework.stereotype.Component

@Component
class AuditEventListener(
    private val auditService: AuditService
) {

    @AsyncEventHandler
    fun onUserRegistered(event: UserRegisteredEvent) {
        auditService.log(
            userId = event.userId,
            action = "USER_REGISTERED",
            entity = "User",
            entityId = event.userId.toString(),
            newData = """{"email": "${event.email}", "firstName": "${event.firstName}", "lastName": "${event.lastName}"}"""
        )
    }

    @AsyncEventHandler
    fun onUserProfileUpdated(event: UserProfileUpdatedEvent) {
        auditService.log(
            userId = event.userId,
            action = "PROFILE_UPDATED",
            entity = "User",
            entityId = event.userId.toString(),
            newData = """{"firstName": "${event.firstName}", "lastName": "${event.lastName}"}"""
        )
    }

    @AsyncEventHandler
    fun onUserLoggedIn(event: UserLoggedInEvent) {
        auditService.log(
            userId = event.userId,
            action = "USER_LOGIN",
            entity = "User",
            entityId = event.userId.toString()
        )
    }

    @AsyncEventHandler
    fun onDaretCreated(event: DaretCreatedEvent) {
        auditService.log(
            userId = event.createurId,
            action = "DARET_CREATED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"nom": "${event.nom}", "montant": ${event.montantMensuel}, "devise": "${event.devise}", "taille": ${event.taille}}"""
        )
    }

    @AsyncEventHandler
    fun onDaretUpdated(event: DaretUpdatedEvent) {
        auditService.log(
            userId = event.updatedByUserId,
            action = "DARET_UPDATED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"nom": "${event.nom}"}"""
        )
    }

    @AsyncEventHandler
    fun onDaretStarted(event: DaretStartedEvent) {
        auditService.log(
            userId = event.startedByUserId,
            action = "DARET_STARTED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"membresCount": ${event.membresCount}, "dateDebut": "${event.dateDebut}", "dateFin": "${event.dateFin}"}"""
        )
    }

    @AsyncEventHandler
    fun onDaretCompleted(event: DaretCompletedEvent) {
        auditService.log(
            userId = null,
            action = "DARET_COMPLETED",
            entity = "Daret",
            entityId = event.daretId.toString()
        )
    }

    @AsyncEventHandler
    fun onMemberJoined(event: MemberJoinedEvent) {
        auditService.log(
            userId = event.userId,
            action = "MEMBER_JOINED",
            entity = "Daret",
            entityId = event.daretId.toString(),
            newData = """{"userId": "${event.userId}", "userName": "${event.userName}"}"""
        )
    }

    @AsyncEventHandler
    fun onMemberLeft(event: MemberLeftEvent) {
        auditService.log(
            userId = event.userId,
            action = "MEMBER_LEFT",
            entity = "Daret",
            entityId = event.daretId.toString()
        )
    }

    @AsyncEventHandler
    fun onRoundClosed(event: RoundClosedEvent) {
        auditService.log(
            userId = null,
            action = "ROUND_CLOSED",
            entity = "Round",
            entityId = event.roundId.toString(),
            newData = """{"daretId": "${event.daretId}", "numero": ${event.roundNumero}, "receveur": "${event.receveurName}", "montantTotal": ${event.montantTotal}}"""
        )
    }

    @AsyncEventHandler
    fun onPaymentCreated(event: PaymentCreatedEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_CREATED",
            entity = "Payment",
            entityId = event.paymentId.toString(),
            newData = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}", "montant": ${event.montant}, "devise": "${event.devise}"}"""
        )
    }

    @AsyncEventHandler
    fun onPaymentSucceeded(event: PaymentSucceededEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_SUCCEEDED",
            entity = "Payment",
            entityId = event.paymentId.toString(),
            newData = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}", "montant": ${event.montant}}"""
        )
    }

    @AsyncEventHandler
    fun onPaymentFailed(event: PaymentFailedEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_FAILED",
            entity = "Payment",
            entityId = event.paymentId.toString(),
            newData = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}", "error": "${event.errorMessage ?: ""}"}"""
        )
    }

    @AsyncEventHandler
    fun onPaymentCancelled(event: PaymentCancelledEvent) {
        auditService.log(
            userId = event.userId,
            action = "PAYMENT_CANCELLED",
            entity = "Payment",
            entityId = event.paymentId.toString()
        )
    }

    @AsyncEventHandler
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
