package com.tonti.repository

import com.tonti.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    fun findByEmail(email: String): User?

    fun findByStripeCustomerId(stripeCustomerId: String): User?

    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.email = :email")
    fun findActiveByEmail(email: String): User?

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.paymentMethods WHERE u.id = :id")
    fun findByIdWithPaymentMethods(id: UUID): User?
}
