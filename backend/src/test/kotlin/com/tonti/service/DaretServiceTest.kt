package com.tonti.service

import com.tonti.dto.daret.CreateDaretRequest
import com.tonti.dto.daret.StartDaretRequest
import com.tonti.dto.daret.UpdateDaretRequest
import com.tonti.entity.Currency
import com.tonti.entity.Daret
import com.tonti.entity.DaretStatus
import com.tonti.entity.Membre
import com.tonti.entity.MembreRole
import com.tonti.entity.Round
import com.tonti.entity.User
import com.tonti.entity.Visibility
import com.tonti.event.DaretCreatedEvent
import com.tonti.event.DaretUpdatedEvent
import com.tonti.event.MemberJoinedEvent
import com.tonti.event.MemberLeftEvent
import com.tonti.exception.BadRequestException
import com.tonti.exception.ConflictException
import com.tonti.exception.ForbiddenException
import com.tonti.exception.NotFoundException
import com.tonti.repository.DaretRepository
import com.tonti.repository.MembreRepository
import com.tonti.repository.PaymentRepository
import com.tonti.repository.RoundRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class DaretServiceTest {

    @MockK private lateinit var daretRepository: DaretRepository
    @MockK private lateinit var membreRepository: MembreRepository
    @MockK private lateinit var roundRepository: RoundRepository
    @MockK private lateinit var paymentRepository: PaymentRepository
    @MockK private lateinit var userService: UserService
    @MockK private lateinit var eventPublisher: ApplicationEventPublisher

    private lateinit var daretService: DaretService

    private lateinit var testUser: User
    private lateinit var testDaret: Daret

    @BeforeEach
    fun setup() {
        daretService = DaretService(
            daretRepository, membreRepository, roundRepository,
            paymentRepository, userService, eventPublisher
        )

        testUser = User(
            email = "creator@example.com",
            passwordHash = "hash",
            firstName = "Test",
            lastName = "Creator"
        ).apply { id = UUID.randomUUID() }

        testDaret = Daret(
            nom = "Test Daret",
            devise = Currency.MAD,
            montantMensuel = BigDecimal("500"),
            taille = 5,
            codeInvitation = "ABC123",
            createur = testUser
        ).apply { id = UUID.randomUUID() }
    }

    @Nested
    inner class CreateDaret {
        @Test
        fun `should create daret and add creator as first member`() {
            val request = CreateDaretRequest(
                nom = "Nouvelle Daret",
                description = "Une description",
                devise = Currency.MAD,
                montantMensuel = BigDecimal("500"),
                taille = 5,
                visibilite = Visibility.PRIVEE,
                delaiGraceJours = 3
            )

            every { daretRepository.existsByCodeInvitation(any()) } returns false
            every { daretRepository.save(any()) } answers { firstArg<Daret>().apply { id = UUID.randomUUID() } }
            every { membreRepository.save(any()) } answers { firstArg<Membre>().apply { id = UUID.randomUUID() } }
            every { eventPublisher.publishEvent(any<DaretCreatedEvent>()) } just runs

            val result = daretService.createDaret(request, testUser)

            assertNotNull(result.id)
            assertEquals("Nouvelle Daret", result.nom)
            assertEquals(Currency.MAD, result.devise)
            assertEquals(DaretStatus.RECRUTEMENT, result.etat)

            verify { daretRepository.save(any()) }
            verify { membreRepository.save(match { it.role == MembreRole.CREATEUR && it.position == 1 }) }
            verify { eventPublisher.publishEvent(any<DaretCreatedEvent>()) }
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `should return daret when found`() {
            every { daretRepository.findById(testDaret.id!!) } returns Optional.of(testDaret)

            val result = daretService.findById(testDaret.id!!)
            assertEquals(testDaret.nom, result.nom)
        }

        @Test
        fun `should throw NotFoundException when not found`() {
            val id = UUID.randomUUID()
            every { daretRepository.findById(id) } returns Optional.empty()

            assertThrows<NotFoundException> { daretService.findById(id) }
        }
    }

    @Nested
    inner class JoinDaret {
        @Test
        fun `should add member to daret in recruitment`() {
            val joiner = User(
                email = "joiner@example.com",
                passwordHash = "hash",
                firstName = "New",
                lastName = "Member"
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.RECRUTEMENT

            every { daretRepository.findByCodeInvitation("ABC123") } returns testDaret
            every { membreRepository.existsByUserIdAndDaretId(joiner.id!!, testDaret.id!!) } returns false
            every { membreRepository.countActiveByDaretId(testDaret.id!!) } returns 2L
            every { membreRepository.save(any()) } answers { firstArg<Membre>().apply { id = UUID.randomUUID() } }
            every { eventPublisher.publishEvent(any<MemberJoinedEvent>()) } just runs

            val result = daretService.joinDaret("ABC123", joiner)

            assertEquals(MembreRole.MEMBRE, result.role)
            assertEquals(3, result.position)
            verify { membreRepository.save(any()) }
            verify { eventPublisher.publishEvent(any<MemberJoinedEvent>()) }
        }

        @Test
        fun `should throw when daret is not recruiting`() {
            val joiner = User(
                email = "j@example.com", passwordHash = "h",
                firstName = "J", lastName = "D"
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.ACTIVE
            every { daretRepository.findByCodeInvitation("ABC123") } returns testDaret

            assertThrows<BadRequestException> { daretService.joinDaret("ABC123", joiner) }
        }

        @Test
        fun `should throw when user already member`() {
            val joiner = User(
                email = "j@example.com", passwordHash = "h",
                firstName = "J", lastName = "D"
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.RECRUTEMENT
            every { daretRepository.findByCodeInvitation("ABC123") } returns testDaret
            every { membreRepository.existsByUserIdAndDaretId(joiner.id!!, testDaret.id!!) } returns true

            assertThrows<ConflictException> { daretService.joinDaret("ABC123", joiner) }
        }

        @Test
        fun `should throw when daret is full`() {
            val joiner = User(
                email = "j@example.com", passwordHash = "h",
                firstName = "J", lastName = "D"
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.RECRUTEMENT
            every { daretRepository.findByCodeInvitation("ABC123") } returns testDaret
            every { membreRepository.existsByUserIdAndDaretId(joiner.id!!, testDaret.id!!) } returns false
            every { membreRepository.countActiveByDaretId(testDaret.id!!) } returns 5L // taille = 5

            assertThrows<BadRequestException> { daretService.joinDaret("ABC123", joiner) }
        }
    }

    @Nested
    inner class LeaveDaret {
        @Test
        fun `should allow member to leave during recruitment`() {
            val membre = Membre(
                user = testUser,
                daret = testDaret,
                role = MembreRole.MEMBRE,
                position = 2
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.RECRUTEMENT

            every { membreRepository.findByUserIdAndDaretId(testUser.id!!, testDaret.id!!) } returns membre
            every { membreRepository.save(any()) } answers { firstArg() }
            every { membreRepository.findActiveByDaretId(testDaret.id!!) } returns emptyList()
            every { eventPublisher.publishEvent(any<MemberLeftEvent>()) } just runs

            daretService.leaveDaret(testDaret.id!!, testUser.id!!)

            assertFalse(membre.isActive)
            assertNotNull(membre.leftAt)
            verify { eventPublisher.publishEvent(any<MemberLeftEvent>()) }
        }

        @Test
        fun `should prevent creator from leaving`() {
            val membre = Membre(
                user = testUser,
                daret = testDaret,
                role = MembreRole.CREATEUR,
                position = 1
            ).apply { id = UUID.randomUUID() }

            every { membreRepository.findByUserIdAndDaretId(testUser.id!!, testDaret.id!!) } returns membre

            assertThrows<BadRequestException> { daretService.leaveDaret(testDaret.id!!, testUser.id!!) }
        }

        @Test
        fun `should prevent leaving an active daret`() {
            val membre = Membre(
                user = testUser,
                daret = testDaret,
                role = MembreRole.MEMBRE,
                position = 2
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.ACTIVE

            every { membreRepository.findByUserIdAndDaretId(testUser.id!!, testDaret.id!!) } returns membre

            assertThrows<BadRequestException> { daretService.leaveDaret(testDaret.id!!, testUser.id!!) }
        }
    }

    @Nested
    inner class UpdateDaret {
        @Test
        fun `should update daret during recruitment`() {
            val request = UpdateDaretRequest(nom = "Updated Name")

            val adminMembre = Membre(
                user = testUser, daret = testDaret,
                role = MembreRole.CREATEUR, position = 1
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.RECRUTEMENT

            every { daretRepository.findById(testDaret.id!!) } returns Optional.of(testDaret)
            every { membreRepository.findByUserIdAndDaretId(testUser.id!!, testDaret.id!!) } returns adminMembre
            every { daretRepository.save(any()) } answers { firstArg() }
            every { eventPublisher.publishEvent(any<DaretUpdatedEvent>()) } just runs

            val result = daretService.updateDaret(testDaret.id!!, request, testUser.id!!)

            assertEquals("Updated Name", result.nom)
            verify { daretRepository.save(any()) }
        }

        @Test
        fun `should reject update when daret is active`() {
            val request = UpdateDaretRequest(nom = "Updated")

            val adminMembre = Membre(
                user = testUser, daret = testDaret,
                role = MembreRole.CREATEUR, position = 1
            ).apply { id = UUID.randomUUID() }

            testDaret.etat = DaretStatus.ACTIVE

            every { daretRepository.findById(testDaret.id!!) } returns Optional.of(testDaret)
            every { membreRepository.findByUserIdAndDaretId(testUser.id!!, testDaret.id!!) } returns adminMembre

            assertThrows<BadRequestException> { daretService.updateDaret(testDaret.id!!, request, testUser.id!!) }
        }

        @Test
        fun `should reject update from non-admin`() {
            val request = UpdateDaretRequest(nom = "Updated")

            every { daretRepository.findById(testDaret.id!!) } returns Optional.of(testDaret)
            every { membreRepository.findByUserIdAndDaretId(testUser.id!!, testDaret.id!!) } returns null

            assertThrows<ForbiddenException> { daretService.updateDaret(testDaret.id!!, request, testUser.id!!) }
        }
    }

    @Nested
    inner class CloseRound {
        @Test
        fun `should throw when round is already closed`() {
            val receveurMembre = Membre(
                user = testUser, daret = testDaret,
                role = MembreRole.CREATEUR, position = 1
            ).apply { id = UUID.randomUUID() }

            val round = Round(
                daret = testDaret, numero = 1,
                receveur = receveurMembre,
                dateDebut = Instant.now(), dateFin = Instant.now()
            ).apply {
                id = UUID.randomUUID()
                estClos = true
            }

            every { daretRepository.findById(testDaret.id!!) } returns Optional.of(testDaret)
            every { membreRepository.findByUserIdAndDaretId(testUser.id!!, testDaret.id!!) } returns receveurMembre
            every { roundRepository.findById(round.id!!) } returns Optional.of(round)

            assertThrows<BadRequestException> { daretService.closeRound(testDaret.id!!, round.id!!, testUser.id!!) }
        }
    }

    @Nested
    inner class FindByCode {
        @Test
        fun `should find daret by invitation code`() {
            every { daretRepository.findByCodeInvitation("ABC123") } returns testDaret

            val result = daretService.findByCode("abc123")
            assertEquals(testDaret.nom, result.nom)
        }

        @Test
        fun `should throw when code not found`() {
            every { daretRepository.findByCodeInvitation("INVALID") } returns null

            assertThrows<NotFoundException> { daretService.findByCode("invalid") }
        }
    }
}
