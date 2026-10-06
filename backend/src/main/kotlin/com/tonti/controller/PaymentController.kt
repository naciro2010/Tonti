package com.tonti.controller

import com.tonti.dto.ApiResponse
import com.tonti.dto.PagedResponse
import com.tonti.dto.payment.CheckoutRequest
import com.tonti.dto.payment.CheckoutResponse
import com.tonti.dto.payment.CreateRefundRequest
import com.tonti.dto.payment.PaymentConfigResponse
import com.tonti.dto.payment.PaymentResponse
import com.tonti.dto.payment.RefundResponse
import com.tonti.security.UserPrincipal
import com.tonti.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Cotisations et remboursements")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/checkout")
    @Operation(summary = "Démarrer le paiement de sa cotisation (renvoie l'URL de la page de paiement hébergée)")
    fun checkout(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CheckoutRequest
    ): ResponseEntity<ApiResponse<CheckoutResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(paymentService.checkout(request, userPrincipal.user)))

    @GetMapping("/config")
    @Operation(summary = "Devises payables en ligne")
    fun config(): ResponseEntity<ApiResponse<PaymentConfigResponse>> =
        ResponseEntity.ok(ApiResponse.success(paymentService.paymentConfig()))

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un paiement")
    fun getPayment(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PaymentResponse>> =
        ResponseEntity.ok(ApiResponse.success(paymentService.getPayment(id, userPrincipal.user)))

    @GetMapping
    @Operation(summary = "Lister mes paiements")
    fun getMyPayments(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> {
        val page = paymentService.getUserPayments(userPrincipal.user.id!!, pageable)
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)))
    }

    @GetMapping("/daret/{daretId}/round/{roundId}")
    @Operation(summary = "Lister les paiements d'un round")
    fun getRoundPayments(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable daretId: UUID,
        @PathVariable roundId: UUID
    ): ResponseEntity<ApiResponse<List<PaymentResponse>>> =
        ResponseEntity.ok(ApiResponse.success(paymentService.getRoundPayments(daretId, roundId, userPrincipal.user.id!!)))

    @DeleteMapping("/{id}")
    @Operation(summary = "Abandonner un paiement en attente")
    fun cancelPayment(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PaymentResponse>> =
        ResponseEntity.ok(ApiResponse.success(paymentService.cancelPayment(id, userPrincipal.user), "Paiement annulé"))

    @PostMapping("/refunds")
    @Operation(summary = "Rembourser un paiement (administrateur du Daret)")
    fun createRefund(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateRefundRequest
    ): ResponseEntity<ApiResponse<RefundResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(paymentService.createRefund(request, userPrincipal.user), "Remboursement créé"))
}
