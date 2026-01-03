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

enum class PaymentMethodType {
    CARD,
    APPLE_PAY,
    GOOGLE_PAY,
    SEPA_DEBIT,
    BANK_TRANSFER
}

enum class WalletType {
    APPLE_PAY,
    GOOGLE_PAY,
    SAMSUNG_PAY,
    LINK
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
