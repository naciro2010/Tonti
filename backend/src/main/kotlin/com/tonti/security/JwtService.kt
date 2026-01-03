package com.tonti.security

import com.tonti.config.AppProperties
import com.tonti.dto.auth.TokenResponse
import com.tonti.entity.Session
import com.tonti.entity.User
import com.tonti.exception.UnauthorizedException
import com.tonti.repository.SessionRepository
import com.tonti.repository.UserRepository
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

private val logger = KotlinLogging.logger {}

data class JwtTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

@Service
class JwtService(
    private val appProperties: AppProperties,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(appProperties.jwt.secret.toByteArray())
    }

    @Transactional
    fun generateTokens(user: User): JwtTokens {
        val now = Instant.now()
        val accessExpiration = now.plusMillis(appProperties.jwt.expiration)
        val refreshExpiration = now.plusMillis(appProperties.jwt.refreshExpiration)

        val accessToken = createToken(user.id!!.toString(), accessExpiration, TokenType.ACCESS)
        val refreshToken = createToken(user.id!!.toString(), refreshExpiration, TokenType.REFRESH)

        // Sauvegarder la session
        val session = Session(
            user = user,
            token = accessToken,
            refreshToken = refreshToken,
            expiresAt = refreshExpiration
        )
        sessionRepository.save(session)

        return JwtTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = appProperties.jwt.expiration / 1000
        )
    }

    @Transactional
    fun refreshTokens(refreshToken: String): TokenResponse {
        val session = sessionRepository.findByRefreshToken(refreshToken)
            ?: throw UnauthorizedException("Refresh token invalide")

        if (session.isExpired()) {
            sessionRepository.delete(session)
            throw UnauthorizedException("Refresh token expiré")
        }

        // Valider le token JWT
        val claims = validateToken(refreshToken)
        if (claims["type"] != TokenType.REFRESH.name) {
            throw UnauthorizedException("Token invalide")
        }

        val user = session.user
        val now = Instant.now()
        val accessExpiration = now.plusMillis(appProperties.jwt.expiration)
        val refreshExpiration = now.plusMillis(appProperties.jwt.refreshExpiration)

        val newAccessToken = createToken(user.id!!.toString(), accessExpiration, TokenType.ACCESS)
        val newRefreshToken = createToken(user.id!!.toString(), refreshExpiration, TokenType.REFRESH)

        // Mettre à jour la session
        session.token = newAccessToken
        session.refreshToken = newRefreshToken
        session.expiresAt = refreshExpiration
        sessionRepository.save(session)

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = appProperties.jwt.expiration / 1000
        )
    }

    fun validateToken(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw UnauthorizedException("Token expiré")
        } catch (e: JwtException) {
            throw UnauthorizedException("Token invalide")
        }
    }

    fun getUserIdFromToken(token: String): UUID {
        val claims = validateToken(token)
        return UUID.fromString(claims.subject)
    }

    fun isValidAccessToken(token: String): Boolean {
        return try {
            val claims = validateToken(token)
            claims["type"] == TokenType.ACCESS.name &&
                sessionRepository.findByToken(token) != null
        } catch (e: Exception) {
            false
        }
    }

    @Transactional
    fun revokeAllUserSessions(userId: UUID) {
        sessionRepository.deleteAllByUserId(userId)
        logger.info { "Revoked all sessions for user $userId" }
    }

    @Transactional
    fun cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions()
        logger.info { "Cleaned up expired sessions" }
    }

    private fun createToken(subject: String, expiration: Instant, type: TokenType): String {
        return Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(expiration))
            .claim("type", type.name)
            .signWith(secretKey)
            .compact()
    }

    private enum class TokenType {
        ACCESS, REFRESH
    }
}
