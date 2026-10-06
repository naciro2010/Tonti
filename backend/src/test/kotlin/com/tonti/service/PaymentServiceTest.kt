package com.tonti.service

import com.tonti.dto.payment.CheckoutRequest
import com.tonti.entity.CheckoutChannel
import com.tonti.entity.Currency
import com.tonti.entity.Daret
import com.tonti.entity.DaretStatus
import com.tonti.entity.Membre
import com.tonti.entity.MembreRole
import com.tonti.entity.Payment
import com.tonti.entity.PaymentProvider
import com.tonti.entity.PaymentStatus
import com.tonti.entity.Round
import com.tonti.entity.User
import com.tonti.event.PaymentCreatedEvent
import com.tonti.event.PaymentSucceededEvent
import com.tonti.exception.BadRequestException
import com.tonti.exception.ConflictException
import com.tonti.exception.PaymentUnavailableException
import com.tonti.repository.PaymentRepository
import com.tonti.repository.RefundRepository
import com.tonti.repository.RoundRepository
import com.tonti.service.payment.CheckoutSession
import com.tonti.service.payment.StripeCheckoutGateway
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
class PaymentServiceTest {

    @MockK private lateinit var stripeGateway: StripeCheckoutGateway
    @MockK private lateinit var paymentRepository: PaymentRepository
    @MockK(relaxed = true) private lateinit var refundRepository: RefundRepository
    @MockK private lateinit var roundRepository: RoundRepository
    @MockK private lateinit var daretService: DaretService
    @MockK(relaxed = true) private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var service: PaymentService

    private lateinit var payer: User
    private lateinit var receveurUser: User
    private lateinit var daret: Daret
    private lateinit var payerMembre: Membre
    private lateinit var receveur: Membre
    private lateinit var round: Round

    @BeforeEach
    fun setup() {
        every { stripeGateway.provider } returns PaymentProvider.STRIPE
        every { stripeGateway.supports(any()) } returns true
        service = PaymentService(
            listOf(stripeGateway), stripeGateway, paymentRepository, refundRepository,
            roundRepository, daretService, eventPublisher
        )

        payer = user("payer@example.com")
        receveurUser = user("receveur@example.com")
        daret = Daret(
            nom = "Daret Famille",
            devise = Currency.MAD,
            montantMensuel = BigDecimal("500.00"),
            taille = 2,
            codeInvitation = "FAM234",
            createur = receveurUser,
            etat = DaretStatus.ACTIVE
        ).apply { id = UUID.randomUUID() }
        payerMembre = Membre(user = payer, daret = daret, role = MembreRole.MEMBRE, position = 2)
            .apply { id = UUID.randomUUID() }
        receveur = Membre(user = receveurUser, daret = daret, role = MembreRole.CREATEUR, position = 1)
            .apply { id = UUID.randomUUID() }
        round = Round(
            daret = daret,
            numero = 1,
            receveur = receveur,
            dateDebut = Instant.now(),
            dateFin = Instant.now().plus(30, ChronoUnit.DAYS)
        ).apply { id = UUID.randomUUID() }

        every { daretService.findById(daret.id!!) } returns daret
        every { daretService.checkIsMember(daret.id!!, payer.id!!) } returns payerMembre
        every { roundRepository.findById(round.id!!) } returns Optional.of(round)
        every { paymentRepository.existsSucceededByUserIdAndRoundId(any(), any()) } returns false
        every { paymentRepository.findByUserIdAndRoundIdAndStatutIn(any(), any(), any()) } returns emptyList()
        every { paymentRepository.save(any()) } answers {
            firstArg<Payment>().apply { if (id == null) id = UUID.randomUUID() }
        }
    }

    private fun user(email: String) =
        User(email = email, passwordHash = "hash", firstName = "Prénom", lastName = "Nom").apply { id = UUID.randomUUID() }

    private fun request() = CheckoutRequest(daretId = daret.id!!, roundId = round.id!!, channel = CheckoutChannel.APP)

    @Nested
    inner class Checkout {

        @Test
        fun `uses the daret amount and currency, never a client-provided one`() {
            val saved = slot<Payment>()
            every { stripeGateway.createCheckout(capture(saved), payer, "fr") } returns
                CheckoutSession(redirectUrl = "https://checkout.stripe.com/c/pay/cs_test_1", providerOrderId = "cs_test_1")

            val response = service.checkout(request(), payer)

            assertEquals(BigDecimal("500.00"), response.amount)
            assertEquals(Currency.MAD, response.currency)
            assertEquals("https://checkout.stripe.com/c/pay/cs_test_1", response.redirectUrl)
            assertEquals("cs_test_1", saved.captured.providerOrderId)
            assertEquals(CheckoutChannel.APP, saved.captured.channel)
            assertEquals(PaymentStatus.PENDING, saved.captured.statut)
            verify { eventPublisher.publishEvent(any<PaymentCreatedEvent>()) }
        }

        @Test
        fun `rejects the beneficiary of the round`() {
            every { daretService.checkIsMember(daret.id!!, payer.id!!) } returns receveur
            assertThrows<BadRequestException> { service.checkout(request(), payer) }
        }

        @Test
        fun `rejects a member who already paid`() {
            every { paymentRepository.existsSucceededByUserIdAndRoundId(payer.id!!, round.id!!) } returns true
            assertThrows<ConflictException> { service.checkout(request(), payer) }
        }

        @Test
        fun `rejects a daret that has not started`() {
            daret.etat = DaretStatus.RECRUTEMENT
            assertThrows<BadRequestException> { service.checkout(request(), payer) }
        }

        @Test
        fun `rejects a closed round`() {
            round.close()
            assertThrows<BadRequestException> { service.checkout(request(), payer) }
        }

        @Test
        fun `fails cleanly when no PSP supports the currency`() {
            every { stripeGateway.supports(any()) } returns false
            assertThrows<PaymentUnavailableException> { service.checkout(request(), payer) }
        }

        @Test
        fun `cancels previous open attempts before starting a new one`() {
            val previous = Payment(user = payer, daret = daret, round = round, montant = daret.montantMensuel)
                .apply { id = UUID.randomUUID(); providerOrderId = "cs_old" }
            every { paymentRepository.findByUserIdAndRoundIdAndStatutIn(any(), any(), any()) } returns listOf(previous)
            every { stripeGateway.cancelCheckout(previous) } returns Unit
            every { stripeGateway.createCheckout(any(), any(), any()) } returns
                CheckoutSession(redirectUrl = "https://checkout.stripe.com/c/pay/cs_new", providerOrderId = "cs_new")

            service.checkout(request(), payer)

            assertEquals(PaymentStatus.CANCELLED, previous.statut)
            verify { stripeGateway.cancelCheckout(previous) }
        }
    }

    @Nested
    inner class Confirmation {

        private fun pendingPayment() =
            Payment(user = payer, daret = daret, round = round, montant = daret.montantMensuel)
                .apply { id = UUID.randomUUID(); providerOrderId = "cs_test" }

        @Test
        fun `marks the payment as succeeded and notifies`() {
            val payment = pendingPayment()

            service.onPaymentSucceeded(payment, "pi_123")

            assertEquals(PaymentStatus.SUCCEEDED, payment.statut)
            assertEquals("pi_123", payment.providerTransactionId)
            verify { eventPublisher.publishEvent(any<PaymentSucceededEvent>()) }
        }

        @Test
        fun `is idempotent`() {
            val payment = pendingPayment().apply { markAsSucceeded() }

            service.onPaymentSucceeded(payment, "pi_123")

            verify(exactly = 0) { eventPublisher.publishEvent(any<PaymentSucceededEvent>()) }
        }

        @Test
        fun `refunds a duplicate payment automatically`() {
            val payment = pendingPayment()
            every { paymentRepository.existsSucceededByUserIdAndRoundId(payer.id!!, round.id!!) } returns true
            every { stripeGateway.refund("pi_dup", null, any()) } returns
                mockk<com.stripe.model.Refund>(relaxed = true) {
                    every { id } returns "re_1"
                    every { status } returns "succeeded"
                }

            service.onPaymentSucceeded(payment, "pi_dup")

            assertEquals(PaymentStatus.REFUNDED, payment.statut)
            assertEquals("DUPLICATE", payment.failureCode)
            verify(exactly = 0) { eventPublisher.publishEvent(any<PaymentSucceededEvent>()) }
        }

        @Test
        fun `ignores a failure on an already settled payment`() {
            val payment = pendingPayment().apply { markAsSucceeded() }

            service.onPaymentFailed(payment, "declined", "card_declined")

            assertEquals(PaymentStatus.SUCCEEDED, payment.statut)
        }
    }
}
