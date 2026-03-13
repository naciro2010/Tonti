package com.tonti.controller

import com.tonti.config.AppProperties
import com.tonti.dto.ApiResponse
import com.tonti.dto.PagedResponse
import com.tonti.dto.payment.*
import com.tonti.security.UserPrincipal
import com.tonti.service.PaymentService
import com.tonti.service.payment.StripeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Gestion des paiements")
class PaymentController(
    private val paymentService: PaymentService,
    private val stripeService: StripeService,
    private val appProperties: AppProperties
) {

    // ==========================================
    // Payment Intents
    // ==========================================

    @PostMapping("/intent")
    @Operation(summary = "Créer une intention de paiement")
    fun createPaymentIntent(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreatePaymentIntentRequest
    ): ResponseEntity<ApiResponse<PaymentIntentResponse>> {
        val response = paymentService.createPayment(request, userPrincipal.user)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirmer un paiement")
    fun confirmPayment(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: ConfirmPaymentRequest
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        val response = paymentService.confirmPayment(request, userPrincipal.user)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un paiement")
    fun getPayment(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        val response = paymentService.getPayment(id, userPrincipal.user)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping
    @Operation(summary = "Lister mes paiements")
    fun getMyPayments(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> {
        val page = paymentService.getUserPayments(userPrincipal.user.id!!, pageable)
        val response = PagedResponse(
            content = page.content,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            isLast = page.isLast
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/daret/{daretId}/round/{roundId}")
    @Operation(summary = "Lister les paiements d'un round")
    fun getRoundPayments(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable daretId: UUID,
        @PathVariable roundId: UUID
    ): ResponseEntity<ApiResponse<List<PaymentResponse>>> {
        val payments = paymentService.getRoundPayments(daretId, roundId, userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(payments))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler un paiement")
    fun cancelPayment(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        val response = paymentService.cancelPayment(id, userPrincipal.user)
        return ResponseEntity.ok(ApiResponse.success(response, "Paiement annulé"))
    }

    // ==========================================
    // Payment Methods
    // ==========================================

    @PostMapping("/setup-intent")
    @Operation(summary = "Créer un intent pour ajouter une méthode de paiement")
    fun createSetupIntent(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<SetupIntentResponse>> {
        val response = paymentService.createSetupIntent(userPrincipal.user)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/methods")
    @Operation(summary = "Ajouter une méthode de paiement")
    fun attachPaymentMethod(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: AttachPaymentMethodRequest
    ): ResponseEntity<ApiResponse<PaymentMethodResponse>> {
        val response = paymentService.attachPaymentMethod(request.paymentMethodId, userPrincipal.user)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Méthode de paiement ajoutée"))
    }

    @GetMapping("/methods")
    @Operation(summary = "Lister mes méthodes de paiement")
    fun listPaymentMethods(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> {
        val methods = paymentService.listPaymentMethods(userPrincipal.user)
        return ResponseEntity.ok(ApiResponse.success(methods))
    }

    @DeleteMapping("/methods/{paymentMethodId}")
    @Operation(summary = "Supprimer une méthode de paiement")
    fun removePaymentMethod(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable paymentMethodId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        paymentService.removePaymentMethod(paymentMethodId, userPrincipal.user)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Méthode de paiement supprimée"))
    }

    @PutMapping("/methods/{paymentMethodId}/default")
    @Operation(summary = "Définir la méthode de paiement par défaut")
    fun setDefaultPaymentMethod(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable paymentMethodId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        paymentService.setDefaultPaymentMethod(paymentMethodId, userPrincipal.user)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Méthode de paiement par défaut définie"))
    }

    // ==========================================
    // Refunds
    // ==========================================

    @PostMapping("/refunds")
    @Operation(summary = "Créer un remboursement")
    fun createRefund(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateRefundRequest
    ): ResponseEntity<ApiResponse<RefundResponse>> {
        val response = paymentService.createRefund(request, userPrincipal.user)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Remboursement créé"))
    }

    // ==========================================
    // Wallet Configuration (Apple Pay / Google Pay)
    // ==========================================

    @GetMapping("/wallet-config")
    @Operation(summary = "Obtenir la configuration pour Apple Pay et Google Pay")
    fun getWalletConfig(): ResponseEntity<ApiResponse<WalletConfigResponse>> {
        val config = WalletConfigResponse(
            stripePublishableKey = appProperties.stripe.publishableKey,
            merchantId = "merchant.com.tonti.app",
            merchantName = "Tonti",
            countryCode = "MA",
            supportedNetworks = listOf("visa", "mastercard", "amex"),
            applePayEnabled = true,
            applePayMerchantId = "merchant.com.tonti.app",
            googlePayEnabled = true,
            googlePayEnvironment = if (appProperties.stripe.secretKey.startsWith("sk_live")) "PRODUCTION" else "TEST"
        )
        return ResponseEntity.ok(ApiResponse.success(config))
    }
}
