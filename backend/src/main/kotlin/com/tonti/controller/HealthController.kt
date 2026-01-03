package com.tonti.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Health checks")
class HealthController {

    data class HealthResponse(
        val status: String = "UP",
        val timestamp: Instant = Instant.now(),
        val version: String = "1.0.0",
        val name: String = "Tonti Backend"
    )

    @GetMapping("/health")
    @Operation(summary = "Health check")
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(HealthResponse())
    }

    @GetMapping
    @Operation(summary = "API Info")
    fun info(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "name" to "Tonti Backend API",
                "version" to "1.0.0",
                "description" to "Backend de paiement avec Stripe, Apple Pay et Google Pay",
                "documentation" to "/swagger-ui.html"
            )
        )
    }
}
