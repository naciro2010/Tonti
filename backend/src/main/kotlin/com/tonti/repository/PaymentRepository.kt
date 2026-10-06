package com.tonti.repository

import com.tonti.entity.Payment
import com.tonti.entity.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {

    fun findByStripePaymentIntentId(stripePaymentIntentId: String): Payment?

    fun findByProviderOrderId(providerOrderId: String): Payment?

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Payment>

    @Query("SELECT p FROM Payment p WHERE p.daret.id = :daretId ORDER BY p.createdAt DESC")
    fun findByDaretId(daretId: UUID): List<Payment>

    @Query("SELECT p FROM Payment p WHERE p.round.id = :roundId ORDER BY p.createdAt DESC")
    fun findByRoundId(roundId: UUID): List<Payment>

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.round.id = :roundId AND p.statut IN :statuts")
    fun findByUserIdAndRoundIdAndStatutIn(userId: UUID, roundId: UUID, statuts: Collection<PaymentStatus>): List<Payment>

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user.id = :userId AND p.round.id = :roundId AND p.statut = :statut")
    fun countByUserIdAndRoundIdAndStatut(userId: UUID, roundId: UUID, statut: PaymentStatus): Long

    fun existsSucceededByUserIdAndRoundId(userId: UUID, roundId: UUID): Boolean =
        countByUserIdAndRoundIdAndStatut(userId, roundId, PaymentStatus.SUCCEEDED) > 0

    @Query("SELECT p FROM Payment p WHERE p.statut = :statut")
    fun findByStatut(statut: PaymentStatus): List<Payment>

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.round.id = :roundId AND p.statut = :statut")
    fun countByRoundIdAndStatut(roundId: UUID, statut: PaymentStatus): Long
}
