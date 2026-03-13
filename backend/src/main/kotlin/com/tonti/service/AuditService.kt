package com.tonti.service

import com.tonti.entity.AuditLog
import com.tonti.repository.AuditLogRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository
) {

    fun log(
        userId: UUID?,
        action: String,
        entity: String,
        entityId: String? = null,
        oldData: String? = null,
        newData: String? = null
    ): AuditLog {
        val auditLog = AuditLog(
            userId = userId,
            action = action,
            entity = entity,
            entityId = entityId,
            oldData = oldData,
            newData = newData
        )

        val saved = auditLogRepository.save(auditLog)
        logger.debug { "Audit: $action on $entity($entityId) by user $userId" }
        return saved
    }
}
