package com.tonti.service.payment

import com.tonti.config.AppProperties
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * URLs publiques du tunnel de paiement.
 *
 * Le PSP redirige le navigateur vers [returnUrl] (succès comme abandon) ; ce point d'entrée
 * renvoie ensuite vers l'application web ou mobile, qui interroge l'API pour connaître le
 * statut réel (seul le webhook PSP fait foi).
 */
@Component
class CheckoutLinkService(
    private val appProperties: AppProperties
) {
    private val apiBaseUrl get() = appProperties.urls.apiBaseUrl.trimEnd('/')
    private val webBaseUrl get() = appProperties.urls.webBaseUrl.trimEnd('/')

    fun returnUrl(paymentId: UUID): String = "$apiBaseUrl/api/payments/return/$paymentId"

    fun webResultUrl(paymentId: UUID): String = "$webBaseUrl/paiement/resultat?paymentId=$paymentId"

    fun appResultUrl(paymentId: UUID): String =
        "${appProperties.urls.appScheme}://paiement/resultat?paymentId=$paymentId"
}
