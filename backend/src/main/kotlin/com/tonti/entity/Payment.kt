package com.tonti.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "idx_payments_user_id", columnList = "user_id"),
        Index(name = "idx_payments_daret_id", columnList = "daret_id"),
        Index(name = "idx_payments_round_id", columnList = "round_id"),
        Index(name = "idx_payments_stripe_payment_intent_id", columnList = "stripe_payment_intent_id"),
        Index(name = "idx_payments_statut", columnList = "statut")
    ]
)
class Payment(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daret_id", nullable = false)
    var daret: Daret,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    var round: Round,

    @Column(nullable = false, precision = 12, scale = 2)
    var montant: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var devise: Currency = Currency.MAD,

    @Column(name = "stripe_payment_intent_id", unique = true)
    var stripePaymentIntentId: String? = null,

    @Column(name = "stripe_charge_id")
    var stripeChargeId: String? = null,

    @Column(name = "stripe_transfer_id")
    var stripeTransferId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: PaymentProvider = PaymentProvider.STRIPE,

    /** Référence de commande chez le PSP (id de session Stripe Checkout). */
    @Column(name = "provider_order_id", unique = true)
    var providerOrderId: String? = null,

    /** Identifiant de transaction renvoyé par le PSP (PaymentIntent Stripe). */
    @Column(name = "provider_transaction_id")
    var providerTransactionId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var channel: CheckoutChannel = CheckoutChannel.WEB,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var statut: PaymentStatus = PaymentStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var methode: PaymentType = PaymentType.CARD,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    var metadata: String? = null,

    @Column(name = "paid_at")
    var paidAt: Instant? = null,

    @Column(name = "failed_at")
    var failedAt: Instant? = null,

    @Column(name = "refunded_at")
    var refundedAt: Instant? = null,

    @Column(name = "failure_reason")
    var failureReason: String? = null,

    @Column(name = "failure_code")
    var failureCode: String? = null
) : BaseEntity() {

    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL])
    var refunds: MutableList<Refund> = mutableListOf()

    fun isPending(): Boolean = statut == PaymentStatus.PENDING
    fun isSucceeded(): Boolean = statut == PaymentStatus.SUCCEEDED
    fun isFailed(): Boolean = statut == PaymentStatus.FAILED

    /** Un paiement est "ouvert" tant que le PSP peut encore le faire aboutir. */
    fun isOpen(): Boolean = statut in OPEN_STATUSES

    fun markAsSucceeded() {
        statut = PaymentStatus.SUCCEEDED
        paidAt = Instant.now()
        failureReason = null
        failureCode = null
    }

    fun markAsFailed(reason: String?, code: String?) {
        statut = PaymentStatus.FAILED
        failedAt = Instant.now()
        failureReason = reason?.take(500)
        failureCode = code?.take(50)
    }

    companion object {
        val OPEN_STATUSES = setOf(PaymentStatus.PENDING, PaymentStatus.PROCESSING, PaymentStatus.REQUIRES_ACTION)
    }
}
