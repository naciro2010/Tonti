package com.tonti.exception

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

class NotFoundException(
    message: String = "Ressource non trouvée"
) : ApiException(HttpStatus.NOT_FOUND, message)

class BadRequestException(
    message: String = "Requête invalide"
) : ApiException(HttpStatus.BAD_REQUEST, message)

class UnauthorizedException(
    message: String = "Non authentifié"
) : ApiException(HttpStatus.UNAUTHORIZED, message)

class ForbiddenException(
    message: String = "Accès refusé"
) : ApiException(HttpStatus.FORBIDDEN, message)

class ConflictException(
    message: String = "Conflit"
) : ApiException(HttpStatus.CONFLICT, message)

class PaymentException(
    message: String,
    cause: Throwable? = null
) : ApiException(HttpStatus.BAD_REQUEST, message, cause)

class ValidationException(
    message: String,
    val errors: Map<String, String> = emptyMap()
) : ApiException(HttpStatus.UNPROCESSABLE_ENTITY, message)
