package com.tonti.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_notifications_user_id", columnList = "user_id"),
        Index(name = "idx_notifications_is_read", columnList = "is_read")
    ]
)
class Notification(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: NotificationType,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var message: String,

    @Column(columnDefinition = "jsonb")
    var data: String? = null,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "read_at")
    var readAt: Instant? = null
) : BaseEntity() {

    fun markAsRead() {
        isRead = true
        readAt = Instant.now()
    }
}
