package com.tonti.event

import com.tonti.entity.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

// ==========================================
// Base Event
// ==========================================

abstract class DomainEvent(
    val eventId: UUID = UUID.randomUUID(),
    val occurredAt: Instant = Instant.now()
)

// ==========================================
// User Events
// ==========================================

data class UserRegisteredEvent(
    val userId: UUID,
    val email: String,
    val firstName: String,
    val lastName: String
) : DomainEvent()

data class UserProfileUpdatedEvent(
    val userId: UUID,
    val firstName: String,
    val lastName: String
) : DomainEvent()

data class UserLoggedInEvent(
    val userId: UUID,
    val email: String
) : DomainEvent()

// ==========================================
// Daret Events
// ==========================================

data class DaretCreatedEvent(
    val daretId: UUID,
    val nom: String,
    val createurId: UUID,
    val createurName: String,
    val codeInvitation: String,
    val montantMensuel: BigDecimal,
    val devise: Currency,
    val taille: Int
) : DomainEvent()

data class DaretUpdatedEvent(
    val daretId: UUID,
    val nom: String,
    val updatedByUserId: UUID
) : DomainEvent()

data class DaretStartedEvent(
    val daretId: UUID,
    val nom: String,
    val startedByUserId: UUID,
    val membresCount: Int,
    val dateDebut: Instant,
    val dateFin: Instant,
    val membreIds: List<UUID>
) : DomainEvent()

data class DaretCompletedEvent(
    val daretId: UUID,
    val nom: String,
    val membreIds: List<UUID>
) : DomainEvent()

// ==========================================
// Membre Events
// ==========================================

data class MemberJoinedEvent(
    val membreId: UUID,
    val userId: UUID,
    val userName: String,
    val daretId: UUID,
    val daretNom: String,
    val createurId: UUID,
    val currentCount: Int,
    val taille: Int
) : DomainEvent()

data class MemberLeftEvent(
    val userId: UUID,
    val userName: String,
    val daretId: UUID,
    val daretNom: String,
    val createurId: UUID
) : DomainEvent()

// ==========================================
// Round Events
// ==========================================

data class RoundClosedEvent(
    val roundId: UUID,
    val roundNumero: Int,
    val daretId: UUID,
    val daretNom: String,
    val receveurId: UUID,
    val receveurName: String,
    val montantTotal: BigDecimal,
    val membreIds: List<UUID>
) : DomainEvent()

// ==========================================
// Payment Events
// ==========================================

data class PaymentCreatedEvent(
    val paymentId: UUID,
    val userId: UUID,
    val daretId: UUID,
    val roundId: UUID,
    val montant: BigDecimal,
    val devise: Currency
) : DomainEvent()

data class PaymentSucceededEvent(
    val paymentId: UUID,
    val userId: UUID,
    val userName: String,
    val daretId: UUID,
    val daretNom: String,
    val roundId: UUID,
    val roundNumero: Int,
    val receveurId: UUID,
    val montant: BigDecimal,
    val devise: Currency
) : DomainEvent()

data class PaymentFailedEvent(
    val paymentId: UUID,
    val userId: UUID,
    val daretId: UUID,
    val roundId: UUID,
    val errorMessage: String?,
    val errorCode: String?
) : DomainEvent()

data class PaymentCancelledEvent(
    val paymentId: UUID,
    val userId: UUID,
    val daretId: UUID,
    val roundId: UUID
) : DomainEvent()

data class RefundCreatedEvent(
    val refundId: UUID,
    val paymentId: UUID,
    val userId: UUID,
    val montant: BigDecimal,
    val raison: String?
) : DomainEvent()
