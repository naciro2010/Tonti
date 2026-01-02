package com.tonti.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "refunds",
    indexes = [
        Index(name = "idx_refunds_payment_id", columnList = "payment_id"),
        Index(name = "idx_refunds_stripe_refund_id", columnList = "stripe_refund_id")
    ]
)
class Refund(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    var payment: Payment,

    @Column(nullable = false, precision = 12, scale = 2)
    var montant: BigDecimal,

    @Column(name = "stripe_refund_id", nullable = false, unique = true)
    var stripeRefundId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var statut: RefundStatus = RefundStatus.PENDING,

    @Column
    var raison: String? = null,

    @Column(name = "completed_at")
    var completedAt: Instant? = null
) : BaseEntity()
