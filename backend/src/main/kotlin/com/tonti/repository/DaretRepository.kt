package com.tonti.repository

import com.tonti.entity.Daret
import com.tonti.entity.DaretStatus
import com.tonti.entity.Visibility
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DaretRepository : JpaRepository<Daret, UUID> {

    fun findByCodeInvitation(codeInvitation: String): Daret?

    @Query("SELECT d FROM Daret d WHERE d.createur.id = :userId ORDER BY d.createdAt DESC")
    fun findByCreateur(userId: UUID): List<Daret>

    @Query("SELECT d FROM Daret d WHERE d.visibilite = :visibilite AND d.etat = :etat ORDER BY d.createdAt DESC")
    fun findByVisibiliteAndEtat(visibilite: Visibility, etat: DaretStatus, pageable: Pageable): Page<Daret>

    @Query("""
        SELECT d FROM Daret d
        JOIN d.membres m
        WHERE m.user.id = :userId AND m.isActive = true
        ORDER BY d.createdAt DESC
    """)
    fun findByMemberId(userId: UUID): List<Daret>

    @Query("SELECT d FROM Daret d LEFT JOIN FETCH d.membres LEFT JOIN FETCH d.rounds WHERE d.id = :id")
    fun findByIdWithDetails(id: UUID): Daret?

    fun existsByCodeInvitation(codeInvitation: String): Boolean
}
