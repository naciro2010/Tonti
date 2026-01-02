package com.tonti.controller

import com.tonti.dto.ApiResponse
import com.tonti.dto.auth.*
import com.tonti.security.JwtService
import com.tonti.security.UserPrincipal
import com.tonti.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API d'authentification")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userService: UserService
) {

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val user = userService.createUser(request)
        val tokens = jwtService.generateTokens(user)
        userService.updateLastLogin(user)

        val authResponse = AuthResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            expiresIn = tokens.expiresIn,
            user = userService.toUserResponse(user)
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(authResponse, "Inscription réussie"))
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val userPrincipal = authentication.principal as UserPrincipal
        val user = userPrincipal.user
        val tokens = jwtService.generateTokens(user)
        userService.updateLastLogin(user)

        val authResponse = AuthResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            expiresIn = tokens.expiresIn,
            user = userService.toUserResponse(user)
        )

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Connexion réussie"))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        val tokens = jwtService.refreshTokens(request.refreshToken)
        return ResponseEntity.ok(ApiResponse.success(tokens))
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion")
    fun logout(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit>> {
        jwtService.revokeAllUserSessions(userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Déconnexion réussie"))
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir le profil de l'utilisateur connecté")
    fun getCurrentUser(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.findById(userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(userService.toUserResponse(user)))
    }

    @PutMapping("/me")
    @Operation(summary = "Mettre à jour le profil")
    fun updateProfile(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.updateProfile(userPrincipal.user.id!!, request)
        return ResponseEntity.ok(ApiResponse.success(userService.toUserResponse(user)))
    }

    @PostMapping("/change-password")
    @Operation(summary = "Changer le mot de passe")
    fun changePassword(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.changePassword(userPrincipal.user.id!!, request.oldPassword, request.newPassword)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Mot de passe modifié avec succès"))
    }
}
