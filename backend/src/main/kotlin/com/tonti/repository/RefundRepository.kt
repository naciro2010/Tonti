package com.tonti.repository

import com.tonti.entity.Refund
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefundRepository : JpaRepository<Refund, UUID> {

    fun findByStripeRefundId(stripeRefundId: String): Refund?

    @Query("SELECT r FROM Refund r WHERE r.payment.id = :paymentId ORDER BY r.createdAt DESC")
    fun findByPaymentId(paymentId: UUID): List<Refund>
}
