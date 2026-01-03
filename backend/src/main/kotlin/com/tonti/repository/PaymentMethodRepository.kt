package com.tonti.repository

import com.tonti.entity.UserPaymentMethod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentMethodRepository : JpaRepository<UserPaymentMethod, UUID> {

    @Query("SELECT pm FROM UserPaymentMethod pm WHERE pm.user.id = :userId AND pm.isActive = true ORDER BY pm.isDefault DESC, pm.createdAt DESC")
    fun findByUserId(userId: UUID): List<UserPaymentMethod>

    fun findByStripePaymentMethodId(stripePaymentMethodId: String): UserPaymentMethod?

    @Query("SELECT pm FROM UserPaymentMethod pm WHERE pm.user.id = :userId AND pm.isDefault = true")
    fun findDefaultByUserId(userId: UUID): UserPaymentMethod?

    @Modifying
    @Query("UPDATE UserPaymentMethod pm SET pm.isDefault = false WHERE pm.user.id = :userId")
    fun clearDefaultForUser(userId: UUID)

    @Modifying
    @Query("UPDATE UserPaymentMethod pm SET pm.isActive = false WHERE pm.stripePaymentMethodId = :stripePaymentMethodId")
    fun deactivateByStripeId(stripePaymentMethodId: String)
}
