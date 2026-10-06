package com.tonti.service.payment

import com.tonti.entity.Currency
import com.tonti.entity.Payment
import com.tonti.entity.PaymentProvider
import com.tonti.entity.User

/**
 * Prestataire de paiement exposant une page de paiement hébergée (redirection).
 *
 * La saisie de la carte et l'authentification 3-D Secure se font exclusivement chez le PSP :
 * Tonti ne voit jamais de numéro de carte (périmètre PCI-DSS SAQ-A).
 */
interface PaymentGateway {
    val provider: PaymentProvider

    /** Indique si ce PSP est configuré et peut encaisser dans la devise donnée. */
    fun supports(currency: Currency): Boolean

    /** Prépare la session de paiement chez le PSP pour un paiement déjà persisté. */
    fun createCheckout(payment: Payment, customer: User, locale: String): CheckoutSession

    /** Invalide la session de paiement d'une tentative abandonnée (best effort). */
    fun cancelCheckout(payment: Payment)
}

data class CheckoutSession(
    /** URL à ouvrir dans le navigateur (web) ou le navigateur in-app (iOS / Android). */
    val redirectUrl: String,
    /** Référence de commande côté PSP, persistée dans [Payment.providerOrderId]. */
    val providerOrderId: String
)
