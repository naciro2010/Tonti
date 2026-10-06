package com.tonti.service

import com.tonti.dto.auth.*
import com.tonti.entity.User
import com.tonti.entity.DaretStatus
import com.tonti.entity.MembreRole
import com.tonti.event.UserDeletedEvent
import com.tonti.event.UserLoggedInEvent
import com.tonti.event.UserProfileUpdatedEvent
import com.tonti.event.UserRegisteredEvent
import com.tonti.exception.BadRequestException
import com.tonti.exception.ConflictException
import com.tonti.exception.NotFoundException
import com.tonti.repository.MembreRepository
import com.tonti.repository.NotificationRepository
import com.tonti.repository.SessionRepository
import com.tonti.repository.UserRepository
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val membreRepository: MembreRepository,
    private val sessionRepository: SessionRepository,
    private val notificationRepository: NotificationRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createUser(request: RegisterRequest): User {
        if (userRepository.existsByEmail(request.email.lowercase().trim())) {
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

        logger.info { "Created user ${savedUser.id}" }

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

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): User {
        val user = findById(userId)

        request.firstName?.let { user.firstName = it.trim() }
        request.lastName?.let { user.lastName = it.trim() }
        request.phone?.let { user.phone = it.trim().ifEmpty { null } }

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
            throw BadRequestException("Mot de passe actuel incorrect")
        }

        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)
        // Toutes les sessions existantes sont invalidées après un changement de mot de passe
        sessionRepository.deleteAllByUserId(userId)

        logger.info { "Password changed for user $userId" }
    }

    /**
     * Suppression du compte à la demande de l'utilisateur.
     *
     * Le compte est anonymisé plutôt que supprimé physiquement : les paiements doivent être
     * conservés (obligations comptables et lutte anti-blanchiment) mais ne sont plus rattachés
     * à une personne identifiable. Impossible tant que l'utilisateur participe à un Daret en cours,
     * afin de ne pas léser les autres membres.
     */
    @Transactional
    fun deleteAccount(userId: UUID, password: String) {
        val user = findById(userId)

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw BadRequestException("Mot de passe incorrect")
        }

        val memberships = membreRepository.findActiveByUserId(userId)
        if (memberships.any { it.daret.etat in ONGOING_STATUSES }) {
            throw ConflictException(
                "Vous participez à un Daret en cours. Vous pourrez supprimer votre compte une fois celui-ci terminé."
            )
        }

        val now = Instant.now()
        memberships.filter { it.daret.etat == DaretStatus.RECRUTEMENT }.forEach { membre ->
            if (membre.role == MembreRole.CREATEUR) {
                membre.daret.etat = DaretStatus.ANNULEE
            }
            membre.isActive = false
            membre.leftAt = now
        }

        sessionRepository.deleteAllByUserId(userId)
        notificationRepository.deleteAllByUserId(userId)

        user.anonymize(passwordEncoder.encode(randomSecret()))
        userRepository.save(user)

        logger.info { "Account $userId deleted (anonymized)" }
        eventPublisher.publishEvent(UserDeletedEvent(userId = userId))
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

    private fun randomSecret(): String {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return Base64.getEncoder().encodeToString(bytes)
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

    private companion object {
        val ONGOING_STATUSES = setOf(DaretStatus.VERROUILLEE, DaretStatus.ACTIVE)
    }
}
