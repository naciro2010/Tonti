package com.tonti.service

import com.tonti.dto.auth.RegisterRequest
import com.tonti.entity.User
import com.tonti.exception.ConflictException
import com.tonti.entity.Currency
import com.tonti.entity.Daret
import com.tonti.entity.DaretStatus
import com.tonti.entity.Membre
import com.tonti.entity.MembreRole
import com.tonti.event.UserDeletedEvent
import com.tonti.exception.BadRequestException
import com.tonti.repository.MembreRepository
import com.tonti.repository.NotificationRepository
import com.tonti.repository.SessionRepository
import com.tonti.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK(relaxed = true)
    private lateinit var membreRepository: MembreRepository

    @MockK(relaxed = true)
    private lateinit var sessionRepository: SessionRepository

    @MockK(relaxed = true)
    private lateinit var notificationRepository: NotificationRepository

    @MockK(relaxed = true)
    private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userService = UserService(
            userRepository, membreRepository, sessionRepository, notificationRepository,
            passwordEncoder, eventPublisher
        )
    }

    @Test
    fun `createUser should create a new user successfully`() {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        val hashedPassword = "hashed_password"
        val savedUser = User(
            email = request.email.lowercase(),
            passwordHash = hashedPassword,
            firstName = request.firstName,
            lastName = request.lastName
        ).apply { id = UUID.randomUUID() }

        every { userRepository.existsByEmail(any()) } returns false
        every { passwordEncoder.encode(any()) } returns hashedPassword
        every { userRepository.save(any()) } returns savedUser

        // When
        val result = userService.createUser(request)

        // Then
        assertNotNull(result)
        assertEquals(request.email.lowercase(), result.email)
        assertEquals(request.firstName, result.firstName)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `createUser should throw ConflictException when email already exists`() {
        // Given
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "password123",
            firstName = "John",
            lastName = "Doe"
        )

        every { userRepository.existsByEmail(any()) } returns true

        // When/Then
        assertThrows<ConflictException> {
            userService.createUser(request)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `findByEmail should return user when found`() {
        // Given
        val email = "test@example.com"
        val user = User(
            email = email,
            passwordHash = "hash",
            firstName = "John",
            lastName = "Doe"
        )

        every { userRepository.findByEmail(email) } returns user

        // When
        val result = userService.findByEmail(email)

        // Then
        assertNotNull(result)
        assertEquals(email, result?.email)
    }

    @Test
    fun `findByEmail should return null when not found`() {
        // Given
        val email = "notfound@example.com"
        every { userRepository.findByEmail(email) } returns null

        // When
        val result = userService.findByEmail(email)

        // Then
        assertNull(result)
    }

    @Test
    fun `deleteAccount should anonymize the user and revoke sessions`() {
        val userId = UUID.randomUUID()
        val user = User(
            email = "leaving@example.com",
            passwordHash = "hash",
            firstName = "Leila",
            lastName = "Bennani",
            phone = "+212600000000"
        ).apply { id = userId }

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { passwordEncoder.matches("secret123", "hash") } returns true
        every { passwordEncoder.encode(any()) } returns "random-hash"
        every { membreRepository.findActiveByUserId(userId) } returns emptyList()
        every { userRepository.save(any()) } answers { firstArg() }

        userService.deleteAccount(userId, "secret123")

        assertFalse(user.isActive)
        assertNull(user.phone)
        assertTrue(user.email.endsWith("@deleted.tonti.invalid"))
        assertEquals("random-hash", user.passwordHash)
        verify { sessionRepository.deleteAllByUserId(userId) }
        verify { notificationRepository.deleteAllByUserId(userId) }
        verify { eventPublisher.publishEvent(any<UserDeletedEvent>()) }
    }

    @Test
    fun `deleteAccount should refuse while the user takes part in an active daret`() {
        val userId = UUID.randomUUID()
        val user = User(email = "a@b.c", passwordHash = "hash", firstName = "A", lastName = "B").apply { id = userId }
        val daret = Daret(
            nom = "Daret",
            devise = Currency.MAD,
            montantMensuel = java.math.BigDecimal("500"),
            taille = 3,
            codeInvitation = "ABC234",
            createur = user,
            etat = DaretStatus.ACTIVE
        )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { passwordEncoder.matches(any(), any()) } returns true
        every { membreRepository.findActiveByUserId(userId) } returns
            listOf(Membre(user = user, daret = daret, role = MembreRole.MEMBRE))

        assertThrows<ConflictException> { userService.deleteAccount(userId, "secret123") }
        assertTrue(user.isActive)
    }

    @Test
    fun `deleteAccount should reject a wrong password`() {
        val userId = UUID.randomUUID()
        val user = User(email = "a@b.c", passwordHash = "hash", firstName = "A", lastName = "B").apply { id = userId }

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { passwordEncoder.matches(any(), any()) } returns false

        assertThrows<BadRequestException> { userService.deleteAccount(userId, "wrong") }
    }
}
