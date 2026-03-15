package com.tonti.repository

import com.tonti.entity.Round
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RoundRepository : JpaRepository<Round, UUID> {

    @Query("SELECT r FROM Round r WHERE r.daret.id = :daretId ORDER BY r.numero")
    fun findByDaretId(daretId: UUID): List<Round>

    @Query("SELECT r FROM Round r WHERE r.daret.id = :daretId AND r.estClos = false")
    fun findCurrentRound(daretId: UUID): Round?

    @Query("SELECT r FROM Round r WHERE r.daret.id = :daretId AND r.numero = :numero")
    fun findByDaretIdAndNumero(daretId: UUID, numero: Int): Round?

    @Query("SELECT MAX(r.numero) FROM Round r WHERE r.daret.id = :daretId")
    fun findMaxNumero(daretId: UUID): Int?
}
