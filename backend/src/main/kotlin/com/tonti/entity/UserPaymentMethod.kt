package com.tonti.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "payment_methods",
    indexes = [
        Index(name = "idx_payment_methods_user_id", columnList = "user_id"),
        Index(name = "idx_payment_methods_stripe_id", columnList = "stripe_payment_method_id")
    ]
)
class UserPaymentMethod(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: PaymentMethodType,

    @Column(name = "stripe_payment_method_id", nullable = false, unique = true)
    var stripePaymentMethodId: String,

    @Column
    var brand: String? = null,

    @Column
    var last4: String? = null,

    @Column(name = "exp_month")
    var expMonth: Int? = null,

    @Column(name = "exp_year")
    var expYear: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type")
    var walletType: WalletType? = null,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
) : BaseEntity()
