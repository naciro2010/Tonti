package com.tonti.service

import com.tonti.dto.auth.RegisterRequest
import com.tonti.entity.User
import com.tonti.exception.ConflictException
import com.tonti.repository.UserRepository
import com.tonti.service.payment.StripeService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var stripeService: StripeService

    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userService = UserService(userRepository, passwordEncoder, stripeService)
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
        every { stripeService.getOrCreateCustomer(any()) } returns "cus_test123"

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
}
