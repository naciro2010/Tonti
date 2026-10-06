package com.tonti.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.tonti.config.AppProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Clock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Limitation de débit par adresse IP sur les endpoints sensibles à la force brute
 * (connexion, inscription, rafraîchissement de token, suppression de compte).
 *
 * Implémentation en mémoire (fenêtre fixe) : suffisante pour une instance unique. Avec
 * plusieurs instances, la remplacer par un stockage partagé (Redis / bucket4j).
 */
@Component
class AuthRateLimitFilter(
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
) : OncePerRequestFilter() {

    private data class Window(val startedAt: Instant, var count: Int)

    private val windows = ConcurrentHashMap<String, Window>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        val sensitive = request.method == "POST" && PROTECTED_POST_PATHS.any { path == it } ||
            request.method == "DELETE" && path == "/api/v1/auth/me"
        return !sensitive
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val limit = appProperties.rateLimit
        val now = Instant.now(clock)
        val key = "${request.remoteAddr}|${request.servletPath}"

        val window = windows.compute(key) { _, current ->
            if (current == null || current.startedAt.plus(limit.authWindow).isBefore(now)) Window(now, 1)
            else current.apply { count++ }
        }!!

        if (windows.size > MAX_TRACKED_KEYS) {
            windows.entries.removeIf { it.value.startedAt.plus(limit.authWindow).isBefore(now) }
        }

        if (window.count > limit.authRequests) {
            val retryAfter = window.startedAt.plus(limit.authWindow).epochSecond - now.epochSecond
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.setHeader("Retry-After", retryAfter.coerceAtLeast(1).toString())
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = Charsets.UTF_8.name()
            objectMapper.writeValue(
                response.writer,
                mapOf(
                    "status" to HttpStatus.TOO_MANY_REQUESTS.value(),
                    "error" to "Too Many Requests",
                    "message" to "Trop de tentatives, veuillez réessayer dans quelques instants"
                )
            )
            return
        }

        filterChain.doFilter(request, response)
    }

    private companion object {
        val PROTECTED_POST_PATHS = setOf("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh")
        const val MAX_TRACKED_KEYS = 10_000
    }
}
