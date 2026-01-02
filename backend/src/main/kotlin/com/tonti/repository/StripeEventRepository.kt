package com.tonti.repository

import com.tonti.entity.StripeEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StripeEventRepository : JpaRepository<StripeEvent, UUID> {

    fun findByStripeEventId(stripeEventId: String): StripeEvent?

    fun existsByStripeEventId(stripeEventId: String): Boolean

    @Query("SELECT se FROM StripeEvent se WHERE se.processed = false ORDER BY se.createdAt")
    fun findUnprocessedEvents(): List<StripeEvent>
}
