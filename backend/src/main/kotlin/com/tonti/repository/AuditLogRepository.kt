package com.tonti.repository

import com.tonti.entity.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, UUID> {

    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    fun findByUserId(userId: UUID, pageable: Pageable): Page<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.entity = :entity AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    fun findByEntityAndEntityId(entity: String, entityId: String, pageable: Pageable): Page<AuditLog>
}
