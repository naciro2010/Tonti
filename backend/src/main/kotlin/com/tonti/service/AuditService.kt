package com.tonti.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.tonti.entity.AuditLog
import com.tonti.repository.AuditLogRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository,
    private val objectMapper: ObjectMapper
) {

    fun log(
        userId: UUID?,
        action: String,
        entity: String,
        entityId: String? = null,
        oldData: Map<String, Any?>? = null,
        newData: Map<String, Any?>? = null
    ): AuditLog {
        val auditLog = AuditLog(
            userId = userId,
            action = action,
            entity = entity,
            entityId = entityId,
            oldData = oldData?.let { objectMapper.writeValueAsString(it) },
            newData = newData?.let { objectMapper.writeValueAsString(it) }
        )

        val saved = auditLogRepository.save(auditLog)
        logger.debug { "Audit: $action on $entity($entityId) by user $userId" }
        return saved
    }
}
