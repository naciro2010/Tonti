package com.tonti.repository

import com.tonti.entity.Session
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface SessionRepository : JpaRepository<Session, UUID> {

    fun findByToken(token: String): Session?

    fun findByRefreshToken(refreshToken: String): Session?

    @Query("SELECT s FROM Session s WHERE s.user.id = :userId AND s.expiresAt > :now")
    fun findActiveSessionsByUserId(userId: UUID, now: Instant = Instant.now()): List<Session>

    @Modifying
    @Query("DELETE FROM Session s WHERE s.user.id = :userId")
    fun deleteAllByUserId(userId: UUID)

    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    fun deleteExpiredSessions(now: Instant = Instant.now())
}
