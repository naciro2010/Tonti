package com.tonti.dto.auth

import jakarta.validation.constraints.*
import java.time.Instant
import java.util.*

// ==========================================
// Registration
// ==========================================

data class RegisterRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "Email invalide")
    val email: String,

    @field:NotBlank(message = "Le mot de passe est requis")
    @field:Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    val password: String,

    @field:NotBlank(message = "Le prénom est requis")
    val firstName: String,

    @field:NotBlank(message = "Le nom est requis")
    val lastName: String,

    val phone: String? = null
)

// ==========================================
// Login
// ==========================================

data class LoginRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "Email invalide")
    val email: String,

    @field:NotBlank(message = "Le mot de passe est requis")
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserResponse
)

// ==========================================
// Token Refresh
// ==========================================

data class RefreshTokenRequest(
    @field:NotBlank(message = "Le refresh token est requis")
    val refreshToken: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

// ==========================================
// User
// ==========================================

data class UserResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val avatarUrl: String?,
    val isVerified: Boolean,
    val createdAt: Instant
)

data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "L'ancien mot de passe est requis")
    val oldPassword: String,

    @field:NotBlank(message = "Le nouveau mot de passe est requis")
    @field:Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    val newPassword: String
)
