package com.tonti.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "sessions",
    indexes = [
        Index(name = "idx_sessions_user_id", columnList = "user_id"),
        Index(name = "idx_sessions_token", columnList = "token"),
        Index(name = "idx_sessions_refresh_token", columnList = "refresh_token")
    ]
)
class Session(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, unique = true)
    var token: String,

    @Column(name = "refresh_token", nullable = false, unique = true)
    var refreshToken: String,

    @Column(name = "user_agent")
    var userAgent: String? = null,

    @Column(name = "ip_address")
    var ipAddress: String? = null,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant
) : BaseEntity() {

    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
}
