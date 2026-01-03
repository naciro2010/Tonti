package com.tonti.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_stripe_customer_id", columnList = "stripe_customer_id")
    ]
)
class User(
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "phone")
    var phone: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "stripe_customer_id", unique = true)
    var stripeCustomerId: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false,

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null
) : BaseEntity() {

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var sessions: MutableList<Session> = mutableListOf()

    @OneToMany(mappedBy = "createur", cascade = [CascadeType.ALL])
    var createdDarets: MutableList<Daret> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    var memberships: MutableList<Membre> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    var payments: MutableList<Payment> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var paymentMethods: MutableList<UserPaymentMethod> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var notifications: MutableList<Notification> = mutableListOf()

    fun fullName(): String = "$firstName $lastName"
}
