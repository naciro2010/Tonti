package com.tonti.entity

enum class Currency {
    MAD, EUR, USD
}

enum class DaretStatus {
    RECRUTEMENT,
    VERROUILLEE,
    ACTIVE,
    TERMINEE,
    ANNULEE
}

enum class Visibility {
    PRIVEE,
    NON_LISTEE,
    PUBLIQUE
}

enum class MembreRole {
    CREATEUR,
    ADMIN,
    MEMBRE
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    REQUIRES_ACTION,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

enum class PaymentType {
    CARD,
    APPLE_PAY,
    GOOGLE_PAY,
    BANK_TRANSFER,
    MOBILE_MONEY
}

/** Prestataire de paiement (PSP) ayant traité la transaction. */
enum class PaymentProvider {
    STRIPE
}

/** Surface depuis laquelle le paiement a été initié (pilote la redirection de retour). */
enum class CheckoutChannel {
    WEB,
    APP
}

enum class RefundStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}

enum class NotificationType {
    PAYMENT_RECEIVED,
    PAYMENT_DUE,
    PAYMENT_OVERDUE,
    ROUND_STARTED,
    ROUND_ENDED,
    DARET_INVITATION,
    MEMBER_JOINED,
    MEMBER_LEFT,
    SYSTEM
}
