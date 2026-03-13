package com.tonti.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "audit_logs",
    indexes = [
        Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        Index(name = "idx_audit_logs_entity", columnList = "entity"),
        Index(name = "idx_audit_logs_action", columnList = "action")
    ]
)
class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    var id: UUID? = null,

    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(nullable = false)
    var action: String,

    @Column(nullable = false)
    var entity: String,

    @Column(name = "entity_id")
    var entityId: String? = null,

    @Column(name = "old_data", columnDefinition = "jsonb")
    var oldData: String? = null,

    @Column(name = "new_data", columnDefinition = "jsonb")
    var newData: String? = null,

    @Column(name = "ip_address")
    var ipAddress: String? = null,

    @Column(name = "user_agent")
    var userAgent: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)
