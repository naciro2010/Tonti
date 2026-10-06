package com.tonti.dto.auth

import jakarta.validation.constraints.*
import java.time.Instant
import java.util.UUID

// ==========================================
// Registration
// ==========================================

data class RegisterRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "Email invalide")
    val email: String,

    @field:NotBlank(message = "Le mot de passe est requis")
    @field:Size(min = 8, max = 128, message = "Le mot de passe doit contenir entre 8 et 128 caractères")
    val password: String,

    @field:NotBlank(message = "Le prénom est requis")
    @field:Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    val firstName: String,

    @field:NotBlank(message = "Le nom est requis")
    @field:Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    val lastName: String,

    @field:Pattern(regexp = PHONE_PATTERN, message = "Numéro de téléphone invalide")
    val phone: String? = null
)

/** Format international E.164 tolérant les espaces (ex : +212 6 12 34 56 78). */
const val PHONE_PATTERN = "^\\+?[0-9 ]{8,20}$"

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
    @field:Size(min = 1, max = 100, message = "Le prénom doit contenir entre 1 et 100 caractères")
    val firstName: String? = null,
    @field:Size(min = 1, max = 100, message = "Le nom doit contenir entre 1 et 100 caractères")
    val lastName: String? = null,
    @field:Pattern(regexp = "^$|$PHONE_PATTERN", message = "Numéro de téléphone invalide")
    val phone: String? = null
)

data class DeleteAccountRequest(
    @field:NotBlank(message = "Le mot de passe est requis pour confirmer la suppression")
    val password: String
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "L'ancien mot de passe est requis")
    val oldPassword: String,

    @field:NotBlank(message = "Le nouveau mot de passe est requis")
    @field:Size(min = 8, max = 128, message = "Le mot de passe doit contenir entre 8 et 128 caractères")
    val newPassword: String
)
