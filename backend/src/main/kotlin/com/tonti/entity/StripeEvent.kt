package com.tonti.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "stripe_events",
    indexes = [
        Index(name = "idx_stripe_events_stripe_event_id", columnList = "stripe_event_id"),
        Index(name = "idx_stripe_events_type", columnList = "type"),
        Index(name = "idx_stripe_events_processed", columnList = "processed")
    ]
)
class StripeEvent(
    @Column(name = "stripe_event_id", nullable = false, unique = true)
    var stripeEventId: String,

    @Column(nullable = false)
    var type: String,

    @Column(columnDefinition = "jsonb", nullable = false)
    var data: String,

    @Column(nullable = false)
    var processed: Boolean = false,

    @Column(name = "processed_at")
    var processedAt: Instant? = null,

    @Column
    var error: String? = null
) : BaseEntity()
