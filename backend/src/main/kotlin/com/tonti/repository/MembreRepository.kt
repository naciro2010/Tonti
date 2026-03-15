package com.tonti.repository

import com.tonti.entity.Membre
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MembreRepository : JpaRepository<Membre, UUID> {

    @Query("SELECT m FROM Membre m WHERE m.user.id = :userId AND m.daret.id = :daretId")
    fun findByUserIdAndDaretId(userId: UUID, daretId: UUID): Membre?

    @Query("SELECT m FROM Membre m WHERE m.daret.id = :daretId AND m.isActive = true ORDER BY m.position")
    fun findActiveByDaretId(daretId: UUID): List<Membre>

    fun existsByUserIdAndDaretId(userId: UUID, daretId: UUID): Boolean

    @Query("SELECT COUNT(m) FROM Membre m WHERE m.daret.id = :daretId AND m.isActive = true")
    fun countActiveByDaretId(daretId: UUID): Long
}
