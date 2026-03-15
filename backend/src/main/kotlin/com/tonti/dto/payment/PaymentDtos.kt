package com.tonti.dto.payment

import com.tonti.entity.Currency
import com.tonti.entity.PaymentStatus
import com.tonti.entity.PaymentType
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ==========================================
// Payment Intent DTOs
// ==========================================

data class CreatePaymentIntentRequest(
    @field:NotNull
    @field:DecimalMin("0.50")
    val amount: BigDecimal,

    @field:NotNull
    val currency: Currency = Currency.MAD,

    @field:NotNull
    val daretId: UUID,

    @field:NotNull
    val roundId: UUID,

    val description: String? = null,

    val paymentMethodId: String? = null,

    val returnUrl: String? = null
)

data class PaymentIntentResponse(
    val paymentIntentId: String,
    val clientSecret: String,
    val status: String,
    val amount: BigDecimal,
    val currency: Currency
)

data class ConfirmPaymentRequest(
    @field:NotBlank
    val paymentIntentId: String,

    @field:NotBlank
    val paymentMethodId: String
)

// ==========================================
// Setup Intent DTOs
// ==========================================

data class SetupIntentResponse(
    val setupIntentId: String,
    val clientSecret: String,
    val status: String
)

// ==========================================
// Payment Method DTOs
// ==========================================

data class AttachPaymentMethodRequest(
    @field:NotBlank
    val paymentMethodId: String
)

data class PaymentMethodResponse(
    val id: UUID,
    val type: String,
    val brand: String?,
    val last4: String?,
    val expMonth: Int?,
    val expYear: Int?,
    val walletType: String?,
    val isDefault: Boolean
)

data class SetDefaultPaymentMethodRequest(
    @field:NotBlank
    val paymentMethodId: String
)

// ==========================================
// Refund DTOs
// ==========================================

data class CreateRefundRequest(
    @field:NotNull
    val paymentId: UUID,

    @field:DecimalMin("0.01")
    val amount: BigDecimal? = null, // null = full refund

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
// Payment DTOs
// ==========================================

data class PaymentResponse(
    val id: UUID,
    val daretId: UUID,
    val roundId: UUID,
    val amount: BigDecimal,
    val currency: Currency,
    val status: PaymentStatus,
    val method: PaymentType,
    val paidAt: Instant?,
    val createdAt: Instant
)

// ==========================================
// Apple Pay / Google Pay Configuration
// ==========================================

data class WalletConfigResponse(
    val stripePublishableKey: String,
    val merchantId: String,
    val merchantName: String = "Tonti",
    val countryCode: String = "MA",
    val supportedNetworks: List<String> = listOf("visa", "mastercard", "amex"),

    // Apple Pay specific
    val applePayEnabled: Boolean = true,
    val applePayMerchantId: String? = null,

    // Google Pay specific
    val googlePayEnabled: Boolean = true,
    val googlePayEnvironment: String = "TEST" // "TEST" ou "PRODUCTION"
)

data class RegisterDomainRequest(
    @field:NotBlank
    val domain: String
)
