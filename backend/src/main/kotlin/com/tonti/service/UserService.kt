package com.tonti.service

import com.tonti.dto.auth.*
import com.tonti.entity.User
import com.tonti.event.UserLoggedInEvent
import com.tonti.event.UserProfileUpdatedEvent
import com.tonti.event.UserRegisteredEvent
import com.tonti.exception.ConflictException
import com.tonti.exception.NotFoundException
import com.tonti.repository.UserRepository
import com.tonti.service.payment.StripeService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val stripeService: StripeService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createUser(request: RegisterRequest): User {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Un compte existe déjà avec cet email")
        }

        val user = User(
            email = request.email.lowercase().trim(),
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            phone = request.phone?.trim()
        )

        val savedUser = userRepository.save(user)

        // Créer le client Stripe en arrière-plan
        try {
            stripeService.getOrCreateCustomer(savedUser)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to create Stripe customer for user ${savedUser.id}, will retry later" }
        }

        logger.info { "Created user ${savedUser.id} with email ${savedUser.email}" }

        eventPublisher.publishEvent(UserRegisteredEvent(
            userId = savedUser.id!!,
            email = savedUser.email,
            firstName = savedUser.firstName,
            lastName = savedUser.lastName
        ))

        return savedUser
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email.lowercase().trim())
    }

    fun findById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("Utilisateur non trouvé") }
    }

    fun findByIdWithPaymentMethods(id: UUID): User? {
        return userRepository.findByIdWithPaymentMethods(id)
    }

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): User {
        val user = findById(userId)

        request.firstName?.let { user.firstName = it.trim() }
        request.lastName?.let { user.lastName = it.trim() }
        request.phone?.let { user.phone = it.trim() }

        // Mettre à jour Stripe si nécessaire
        user.stripeCustomerId?.let { customerId ->
            try {
                stripeService.updateCustomer(customerId, user)
            } catch (e: Exception) {
                logger.warn(e) { "Failed to update Stripe customer $customerId" }
            }
        }

        val saved = userRepository.save(user)

        eventPublisher.publishEvent(UserProfileUpdatedEvent(
            userId = saved.id!!,
            firstName = saved.firstName,
            lastName = saved.lastName
        ))

        return saved
    }

    @Transactional
    fun changePassword(userId: UUID, oldPassword: String, newPassword: String) {
        val user = findById(userId)

        if (!passwordEncoder.matches(oldPassword, user.passwordHash)) {
            throw ConflictException("Mot de passe actuel incorrect")
        }

        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        logger.info { "Password changed for user $userId" }
    }

    @Transactional
    fun updateLastLogin(user: User) {
        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        eventPublisher.publishEvent(UserLoggedInEvent(
            userId = user.id!!,
            email = user.email
        ))
    }

    fun toUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            phone = user.phone,
            avatarUrl = user.avatarUrl,
            isVerified = user.isVerified,
            createdAt = user.createdAt
        )
    }
}
