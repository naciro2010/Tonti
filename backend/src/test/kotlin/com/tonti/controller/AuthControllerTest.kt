package com.tonti.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tonti.dto.auth.LoginRequest
import com.tonti.dto.auth.RegisterRequest
import com.tonti.entity.User
import com.tonti.security.JwtService
import com.tonti.security.JwtTokens
import com.tonti.security.UserPrincipal
import com.tonti.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockUserService(): UserService = mockk(relaxed = true)

        @Bean
        @Primary
        fun mockJwtService(): JwtService = mockk(relaxed = true)

        @Bean
        @Primary
        fun mockAuthenticationManager(): AuthenticationManager = mockk(relaxed = true)
    }

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `register should return 201 with auth response`() {
        // Given
        val request = RegisterRequest(
            email = "newuser@example.com",
            password = "password123",
            firstName = "New",
            lastName = "User"
        )

        val user = User(
            email = request.email,
            passwordHash = "hash",
            firstName = request.firstName,
            lastName = request.lastName
        ).apply { id = UUID.randomUUID() }

        val tokens = JwtTokens(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresIn = 3600
        )

        every { userService.createUser(any()) } returns user
        every { jwtService.generateTokens(any()) } returns tokens
        every { userService.toUserResponse(any()) } returns mockk(relaxed = true)
        every { userService.updateLastLogin(any()) } returns Unit

        // When/Then
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { exists() }
        }
    }

    @Test
    fun `login should return 200 with auth response`() {
        // Given
        val request = LoginRequest(
            email = "user@example.com",
            password = "password123"
        )

        val user = User(
            email = request.email,
            passwordHash = "hash",
            firstName = "Test",
            lastName = "User"
        ).apply { id = UUID.randomUUID() }

        val userPrincipal = UserPrincipal(user)
        val authentication = UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities)

        val tokens = JwtTokens(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresIn = 3600
        )

        every { authenticationManager.authenticate(any()) } returns authentication
        every { jwtService.generateTokens(any()) } returns tokens
        every { userService.toUserResponse(any()) } returns mockk(relaxed = true)
        every { userService.updateLastLogin(any()) } returns Unit

        // When/Then
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }
    }

    @Test
    fun `register should return 400 for invalid email`() {
        // Given
        val request = mapOf(
            "email" to "invalid-email",
            "password" to "password123",
            "firstName" to "Test",
            "lastName" to "User"
        )

        // When/Then
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
