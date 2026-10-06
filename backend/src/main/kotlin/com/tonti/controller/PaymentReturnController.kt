package com.tonti.controller

import com.tonti.entity.CheckoutChannel
import com.tonti.repository.PaymentRepository
import com.tonti.service.payment.CheckoutLinkService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.HtmlUtils
import java.net.URI
import java.util.UUID

/**
 * Point de retour navigateur après la page de paiement du PSP.
 *
 * - Web : redirection 303 vers la page de résultat de l'application web.
 * - Application mobile : page minimale qui rouvre l'application via son deep link
 *   (le navigateur in-app ne suit pas toujours une redirection HTTP vers un schéma custom).
 *
 * Ce point d'entrée ne modifie jamais le statut du paiement : seul le webhook PSP fait foi.
 */
@Hidden
@RestController
@RequestMapping("/api/payments/return")
class PaymentReturnController(
    private val paymentRepository: PaymentRepository,
    private val links: CheckoutLinkService
) {

    @RequestMapping("/{paymentId}", method = [RequestMethod.GET, RequestMethod.POST])
    fun handleReturn(@PathVariable paymentId: UUID): ResponseEntity<String> {
        val payment = paymentRepository.findById(paymentId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (payment.channel == CheckoutChannel.WEB) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(links.webResultUrl(paymentId)))
                .build()
        }

        val deepLink = HtmlUtils.htmlEscape(links.appResultUrl(paymentId))
        val html = """
            <!doctype html>
            <html lang="fr">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Tonti</title>
              <style>
                body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;
                  font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;background:#0B0F1A;color:#fff;text-align:center}
                a{display:inline-block;margin-top:24px;padding:14px 28px;border-radius:12px;background:#FFB300;color:#0B0F1A;
                  font-weight:600;text-decoration:none}
              </style>
            </head>
            <body>
              <main>
                <p>Paiement terminé. Retour à l'application…</p>
                <a href="$deepLink">Retourner dans Tonti</a>
              </main>
              <script>window.location.replace(document.querySelector('a').href);</script>
            </body>
            </html>
        """.trimIndent()

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .cacheControl(CacheControl.noStore())
            .header("Referrer-Policy", "no-referrer")
            .body(html)
    }
}
