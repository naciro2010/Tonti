package com.tonti.dto.daret

import com.tonti.entity.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

// ==========================================
// Create Daret
// ==========================================

data class CreateDaretRequest(
    @field:NotBlank(message = "Le nom est requis")
    @field:Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    val nom: String,

    val description: String? = null,

    @field:NotNull(message = "La devise est requise")
    val devise: Currency = Currency.MAD,

    @field:NotNull(message = "Le montant mensuel est requis")
    @field:DecimalMin(value = "1", message = "Le montant doit être supérieur à 0")
    val montantMensuel: BigDecimal,

    @field:NotNull(message = "La taille est requise")
    @field:Min(value = 2, message = "Le Daret doit avoir au moins 2 membres")
    @field:Max(value = 50, message = "Le Daret ne peut pas avoir plus de 50 membres")
    val taille: Int,

    val visibilite: Visibility = Visibility.PRIVEE,

    @field:Min(value = 0, message = "Le délai de grâce ne peut pas être négatif")
    @field:Max(value = 30, message = "Le délai de grâce ne peut pas dépasser 30 jours")
    val delaiGraceJours: Int = 3
)

// ==========================================
// Update Daret
// ==========================================

data class UpdateDaretRequest(
    val nom: String? = null,
    val description: String? = null,
    val visibilite: Visibility? = null,
    val delaiGraceJours: Int? = null
)

// ==========================================
// Daret Response
// ==========================================

data class DaretResponse(
    val id: UUID,
    val nom: String,
    val description: String?,
    val devise: Currency,
    val montantMensuel: BigDecimal,
    val taille: Int,
    val etat: DaretStatus,
    val visibilite: Visibility,
    val codeInvitation: String,
    val delaiGraceJours: Int,
    val dateDebut: Instant?,
    val dateFin: Instant?,
    val createurId: UUID,
    val membresCount: Int,
    val currentRound: Int?,
    val createdAt: Instant
)

data class DaretDetailResponse(
    val id: UUID,
    val nom: String,
    val description: String?,
    val devise: Currency,
    val montantMensuel: BigDecimal,
    val taille: Int,
    val etat: DaretStatus,
    val visibilite: Visibility,
    val codeInvitation: String,
    val delaiGraceJours: Int,
    val dateDebut: Instant?,
    val dateFin: Instant?,
    val createur: MembreResponse,
    val membres: List<MembreResponse>,
    val rounds: List<RoundResponse>,
    val createdAt: Instant
)

// ==========================================
// Membre
// ==========================================

data class MembreResponse(
    val id: UUID,
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: MembreRole,
    val position: Int?,
    val isActive: Boolean,
    val joinedAt: Instant
)

data class JoinDaretRequest(
    @field:NotBlank(message = "Le code d'invitation est requis")
    val codeInvitation: String
)

data class UpdateMembrePositionRequest(
    @field:NotNull(message = "La nouvelle position est requise")
    @field:Min(value = 1, message = "La position doit être supérieure à 0")
    val position: Int
)

// ==========================================
// Round
// ==========================================

data class RoundResponse(
    val id: UUID,
    val numero: Int,
    val receveur: MembreResponse,
    val dateDebut: Instant,
    val dateFin: Instant,
    val estClos: Boolean,
    val montantTotal: BigDecimal,
    val paymentsCount: Int,
    val paidCount: Int
)

data class StartDaretRequest(
    val dateDebut: Instant? = null, // null = maintenant
    val roster: List<UUID>? = null  // null = ordre aléatoire
)
