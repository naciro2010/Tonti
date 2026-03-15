package com.tonti.event

import com.tonti.entity.NotificationType
import com.tonti.service.NotificationService
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class NotificationEventListener(
    private val notificationService: NotificationService
) {

    @AsyncEventHandler
    fun onUserRegistered(event: UserRegisteredEvent) {
        logger.info { "Handling UserRegisteredEvent for user ${event.userId}" }
        notificationService.createNotification(
            userId = event.userId,
            type = NotificationType.SYSTEM,
            title = "Bienvenue sur Tonti !",
            message = "Votre compte a été créé avec succès. Créez ou rejoignez un Daret pour commencer.",
            data = """{"userId": "${event.userId}"}"""
        )
    }

    @AsyncEventHandler
    fun onDaretCreated(event: DaretCreatedEvent) {
        logger.info { "Handling DaretCreatedEvent for daret ${event.daretId}" }
        notificationService.createNotification(
            userId = event.createurId,
            type = NotificationType.SYSTEM,
            title = "Daret créé",
            message = "Votre Daret \"${event.nom}\" a été créé. Code d'invitation : ${event.codeInvitation}",
            data = """{"daretId": "${event.daretId}", "code": "${event.codeInvitation}"}"""
        )
    }

    @AsyncEventHandler
    fun onDaretStarted(event: DaretStartedEvent) {
        logger.info { "Handling DaretStartedEvent for daret ${event.daretId}" }
        event.membreIds.forEach { membreUserId ->
            notificationService.createNotification(
                userId = membreUserId,
                type = NotificationType.ROUND_STARTED,
                title = "Daret démarré",
                message = "Le Daret \"${event.nom}\" a démarré avec ${event.membresCount} membres. Le premier round commence maintenant !",
                data = """{"daretId": "${event.daretId}"}"""
            )
        }
    }

    @AsyncEventHandler
    fun onDaretCompleted(event: DaretCompletedEvent) {
        logger.info { "Handling DaretCompletedEvent for daret ${event.daretId}" }
        event.membreIds.forEach { membreUserId ->
            notificationService.createNotification(
                userId = membreUserId,
                type = NotificationType.SYSTEM,
                title = "Daret terminé",
                message = "Le Daret \"${event.nom}\" est terminé. Merci pour votre participation !",
                data = """{"daretId": "${event.daretId}"}"""
            )
        }
    }

    @AsyncEventHandler
    fun onMemberJoined(event: MemberJoinedEvent) {
        logger.info { "Handling MemberJoinedEvent: user ${event.userId} joined daret ${event.daretId}" }
        notificationService.createNotification(
            userId = event.createurId,
            type = NotificationType.MEMBER_JOINED,
            title = "Nouveau membre",
            message = "${event.userName} a rejoint le Daret \"${event.daretNom}\" (${event.currentCount}/${event.taille})",
            data = """{"daretId": "${event.daretId}", "userId": "${event.userId}"}"""
        )
        notificationService.createNotification(
            userId = event.userId,
            type = NotificationType.DARET_INVITATION,
            title = "Daret rejoint",
            message = "Vous avez rejoint le Daret \"${event.daretNom}\"",
            data = """{"daretId": "${event.daretId}"}"""
        )
    }

    @AsyncEventHandler
    fun onMemberLeft(event: MemberLeftEvent) {
        logger.info { "Handling MemberLeftEvent: user ${event.userId} left daret ${event.daretId}" }
        notificationService.createNotification(
            userId = event.createurId,
            type = NotificationType.MEMBER_LEFT,
            title = "Membre parti",
            message = "${event.userName} a quitté le Daret \"${event.daretNom}\"",
            data = """{"daretId": "${event.daretId}", "userId": "${event.userId}"}"""
        )
    }

    @AsyncEventHandler
    fun onRoundClosed(event: RoundClosedEvent) {
        logger.info { "Handling RoundClosedEvent for round ${event.roundId}" }
        notificationService.createNotification(
            userId = event.receveurId,
            type = NotificationType.ROUND_ENDED,
            title = "Round clos - Vous recevez ${event.montantTotal} !",
            message = "Le round ${event.roundNumero} du Daret \"${event.daretNom}\" est clos. Vous recevez ${event.montantTotal}.",
            data = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}", "montant": ${event.montantTotal}}"""
        )
        event.membreIds.filter { it != event.receveurId }.forEach { membreUserId ->
            notificationService.createNotification(
                userId = membreUserId,
                type = NotificationType.ROUND_ENDED,
                title = "Round ${event.roundNumero} clos",
                message = "Le round ${event.roundNumero} du Daret \"${event.daretNom}\" est clos. ${event.receveurName} a reçu ${event.montantTotal}.",
                data = """{"daretId": "${event.daretId}", "roundId": "${event.roundId}"}"""
            )
        }
    }

    @AsyncEventHandler
    fun onPaymentSucceeded(event: PaymentSucceededEvent) {
        logger.info { "Handling PaymentSucceededEvent for payment ${event.paymentId}" }
        notificationService.createNotification(
            userId = event.userId,
            type = NotificationType.PAYMENT_RECEIVED,
            title = "Paiement confirmé",
            message = "Votre paiement de ${event.montant} ${event.devise} pour le round ${event.roundNumero} du Daret \"${event.daretNom}\" a été confirmé.",
            data = """{"paymentId": "${event.paymentId}", "daretId": "${event.daretId}", "roundId": "${event.roundId}"}"""
        )
        if (event.receveurId != event.userId) {
            notificationService.createNotification(
                userId = event.receveurId,
                type = NotificationType.PAYMENT_RECEIVED,
                title = "Paiement reçu",
                message = "${event.userName} a payé ${event.montant} ${event.devise} pour le round ${event.roundNumero}.",
                data = """{"paymentId": "${event.paymentId}", "daretId": "${event.daretId}", "roundId": "${event.roundId}"}"""
            )
        }
    }

    @AsyncEventHandler
    fun onPaymentFailed(event: PaymentFailedEvent) {
        logger.info { "Handling PaymentFailedEvent for payment ${event.paymentId}" }
        notificationService.createNotification(
            userId = event.userId,
            type = NotificationType.SYSTEM,
            title = "Paiement échoué",
            message = "Votre paiement a échoué. ${event.errorMessage ?: "Veuillez réessayer."}",
            data = """{"paymentId": "${event.paymentId}", "daretId": "${event.daretId}", "roundId": "${event.roundId}"}"""
        )
    }

    @AsyncEventHandler
    fun onRefundCreated(event: RefundCreatedEvent) {
        logger.info { "Handling RefundCreatedEvent for refund ${event.refundId}" }
        notificationService.createNotification(
            userId = event.userId,
            type = NotificationType.SYSTEM,
            title = "Remboursement initié",
            message = "Un remboursement de ${event.montant} a été initié pour votre paiement.",
            data = """{"refundId": "${event.refundId}", "paymentId": "${event.paymentId}"}"""
        )
    }
}
