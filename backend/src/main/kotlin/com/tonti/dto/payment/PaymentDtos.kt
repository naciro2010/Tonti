package com.tonti.dto.payment

import com.tonti.entity.CheckoutChannel
import com.tonti.entity.Currency
import com.tonti.entity.PaymentProvider
import com.tonti.entity.PaymentStatus
import com.tonti.entity.PaymentType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ==========================================
// Checkout
// ==========================================

/**
 * Démarre le paiement de la cotisation d'un round.
 * Le montant et la devise ne sont jamais fournis par le client : ils proviennent du Daret.
 */
data class CheckoutRequest(
    @field:NotNull
    val daretId: UUID,

    @field:NotNull
    val roundId: UUID,

    /** WEB : retour vers l'application web ; APP : retour via le deep link de l'application mobile. */
    val channel: CheckoutChannel = CheckoutChannel.WEB,

    @field:Pattern(regexp = "^(fr|ar|en)$", message = "Langue non supportée")
    val locale: String = "fr"
)

data class CheckoutResponse(
    val paymentId: UUID,
    val provider: PaymentProvider,
    val status: PaymentStatus,
    val amount: BigDecimal,
    val currency: Currency,
    /** Page de paiement hébergée par le PSP, à ouvrir dans le navigateur. */
    val redirectUrl: String
)

// ==========================================
// Payments
// ==========================================

data class PaymentResponse(
    val id: UUID,
    val daretId: UUID,
    val roundId: UUID,
    val roundNumero: Int,
    val userId: UUID,
    val userName: String,
    val amount: BigDecimal,
    val currency: Currency,
    val status: PaymentStatus,
    val provider: PaymentProvider,
    val method: PaymentType,
    val failureReason: String?,
    val paidAt: Instant?,
    val createdAt: Instant
)

// ==========================================
// Refunds
// ==========================================

data class CreateRefundRequest(
    @field:NotNull
    val paymentId: UUID,

    @field:DecimalMin("0.01")
    val amount: BigDecimal? = null, // null = remboursement total

    @field:Size(max = 500)
    val reason: String? = null
)

data class RefundResponse(
    val id: UUID,
    val paymentId: UUID,
    val amount: BigDecimal,
    val status: String,
    val reason: String?,
    val createdAt: Instant
)

// ==========================================
// Configuration publique
// ==========================================

data class PaymentConfigResponse(
    val onlinePaymentCurrencies: List<Currency>,
    val provider: PaymentProvider?
)
