package com.tonti.service

import com.tonti.dto.daret.*
import com.tonti.entity.*
import com.tonti.event.*
import com.tonti.exception.*
import com.tonti.repository.*
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class DaretService(
    private val daretRepository: DaretRepository,
    private val membreRepository: MembreRepository,
    private val roundRepository: RoundRepository,
    private val paymentRepository: PaymentRepository,
    private val userService: UserService,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val random = SecureRandom()

    @Transactional
    fun createDaret(request: CreateDaretRequest, createur: User): Daret {
        val codeInvitation = generateUniqueCode()

        val daret = Daret(
            nom = request.nom.trim(),
            description = request.description?.trim(),
            devise = request.devise,
            montantMensuel = request.montantMensuel,
            taille = request.taille,
            visibilite = request.visibilite,
            codeInvitation = codeInvitation,
            delaiGraceJours = request.delaiGraceJours,
            createur = createur
        )

        val savedDaret = daretRepository.save(daret)

        // Ajouter le créateur comme premier membre
        val membre = Membre(
            user = createur,
            daret = savedDaret,
            role = MembreRole.CREATEUR,
            position = 1
        )
        membreRepository.save(membre)

        logger.info { "Created Daret ${savedDaret.id} by user ${createur.id}" }

        eventPublisher.publishEvent(DaretCreatedEvent(
            daretId = savedDaret.id!!,
            nom = savedDaret.nom,
            createurId = createur.id!!,
            createurName = createur.fullName(),
            codeInvitation = savedDaret.codeInvitation,
            montantMensuel = savedDaret.montantMensuel,
            devise = savedDaret.devise,
            taille = savedDaret.taille
        ))

        return savedDaret
    }

    fun findById(id: UUID): Daret {
        return daretRepository.findById(id)
            .orElseThrow { NotFoundException("Daret non trouvé") }
    }

    fun findByIdWithDetails(id: UUID): Daret {
        return daretRepository.findByIdWithDetails(id)
            ?: throw NotFoundException("Daret non trouvé")
    }

    fun findByCode(code: String): Daret {
        return daretRepository.findByCodeInvitation(code.uppercase())
            ?: throw NotFoundException("Daret non trouvé avec ce code")
    }

    fun findByUser(userId: UUID): List<Daret> {
        val created = daretRepository.findByCreateur(userId)
        val member = daretRepository.findByMemberId(userId)
        return (created + member).distinctBy { it.id }
    }

    fun findPublicDarets(pageable: Pageable): Page<Daret> {
        return daretRepository.findByVisibiliteAndEtat(
            Visibility.PUBLIQUE,
            DaretStatus.RECRUTEMENT,
            pageable
        )
    }

    @Transactional
    fun updateDaret(id: UUID, request: UpdateDaretRequest, userId: UUID): Daret {
        val daret = findById(id)

        checkIsAdmin(daret, userId)

        if (daret.etat != DaretStatus.RECRUTEMENT) {
            throw BadRequestException("Le Daret ne peut être modifié qu'en phase de recrutement")
        }

        request.nom?.let { daret.nom = it.trim() }
        request.description?.let { daret.description = it.trim() }
        request.visibilite?.let { daret.visibilite = it }
        request.delaiGraceJours?.let { daret.delaiGraceJours = it }

        val saved = daretRepository.save(daret)

        eventPublisher.publishEvent(DaretUpdatedEvent(
            daretId = saved.id!!,
            nom = saved.nom,
            updatedByUserId = userId
        ))

        return saved
    }

    @Transactional
    fun joinDaret(code: String, user: User): Membre {
        val daret = findByCode(code)

        if (daret.etat != DaretStatus.RECRUTEMENT) {
            throw BadRequestException("Ce Daret n'accepte plus de nouveaux membres")
        }

        if (membreRepository.existsByUserIdAndDaretId(user.id!!, daret.id!!)) {
            throw ConflictException("Vous êtes déjà membre de ce Daret")
        }

        val currentCount = membreRepository.countActiveByDaretId(daret.id!!).toInt()
        if (currentCount >= daret.taille) {
            throw BadRequestException("Ce Daret est complet")
        }

        val membre = Membre(
            user = user,
            daret = daret,
            role = MembreRole.MEMBRE,
            position = currentCount + 1
        )

        val savedMembre = membreRepository.save(membre)
        logger.info { "User ${user.id} joined Daret ${daret.id}" }

        eventPublisher.publishEvent(MemberJoinedEvent(
            membreId = savedMembre.id!!,
            userId = user.id!!,
            userName = user.fullName(),
            daretId = daret.id!!,
            daretNom = daret.nom,
            createurId = daret.createur.id!!,
            currentCount = currentCount + 1,
            taille = daret.taille
        ))

        return savedMembre
    }

    @Transactional
    fun leaveDaret(daretId: UUID, userId: UUID) {
        val membre = membreRepository.findByUserIdAndDaretId(userId, daretId)
            ?: throw NotFoundException("Vous n'êtes pas membre de ce Daret")

        val daret = membre.daret

        if (membre.role == MembreRole.CREATEUR) {
            throw BadRequestException("Le créateur ne peut pas quitter le Daret")
        }

        if (daret.etat == DaretStatus.ACTIVE) {
            throw BadRequestException("Vous ne pouvez pas quitter un Daret actif")
        }

        membre.isActive = false
        membre.leftAt = Instant.now()
        membreRepository.save(membre)

        // Réorganiser les positions
        val activeMembers = membreRepository.findActiveByDaretId(daretId)
        activeMembers.forEachIndexed { index, m ->
            m.position = index + 1
            membreRepository.save(m)
        }

        logger.info { "User $userId left Daret $daretId" }

        eventPublisher.publishEvent(MemberLeftEvent(
            userId = userId,
            userName = membre.user.fullName(),
            daretId = daretId,
            daretNom = daret.nom,
            createurId = daret.createur.id!!
        ))
    }

    @Transactional
    fun startDaret(id: UUID, request: StartDaretRequest, userId: UUID): Daret {
        val daret = findById(id)

        checkIsAdmin(daret, userId)

        if (daret.etat != DaretStatus.RECRUTEMENT) {
            throw BadRequestException("Le Daret a déjà démarré")
        }

        val membres = membreRepository.findActiveByDaretId(daret.id!!)
        if (membres.size < 2) {
            throw BadRequestException("Il faut au moins 2 membres pour démarrer")
        }

        // Verrouiller le Daret
        daret.etat = DaretStatus.VERROUILLEE
        daret.taille = membres.size

        // Définir le roster (ordre des bénéficiaires)
        val roster = request.roster?.let { orderIds ->
            membres.sortedBy { m -> orderIds.indexOf(m.user.id) }
        } ?: membres.shuffled(random)

        roster.forEachIndexed { index, membre ->
            membre.position = index + 1
            membreRepository.save(membre)
        }

        // Créer les rounds
        val dateDebut = request.dateDebut ?: Instant.now()
        daret.dateDebut = dateDebut

        roster.forEachIndexed { index, membre ->
            val roundStart = dateDebut.plus((index * 30).toLong(), ChronoUnit.DAYS)
            val roundEnd = roundStart.plus(30, ChronoUnit.DAYS)

            val round = Round(
                daret = daret,
                numero = index + 1,
                receveur = membre,
                dateDebut = roundStart,
                dateFin = roundEnd
            )
            roundRepository.save(round)
        }

        daret.dateFin = dateDebut.plus((roster.size * 30).toLong(), ChronoUnit.DAYS)
        daret.etat = DaretStatus.ACTIVE

        val savedDaret = daretRepository.save(daret)
        logger.info { "Started Daret ${daret.id} with ${roster.size} members" }

        eventPublisher.publishEvent(DaretStartedEvent(
            daretId = savedDaret.id!!,
            nom = savedDaret.nom,
            startedByUserId = userId,
            membresCount = roster.size,
            dateDebut = savedDaret.dateDebut!!,
            dateFin = savedDaret.dateFin!!,
            membreIds = roster.map { it.user.id!! }
        ))

        return savedDaret
    }

    @Transactional
    fun closeRound(daretId: UUID, roundId: UUID, userId: UUID): Round {
        val daret = findById(daretId)
        checkIsAdmin(daret, userId)

        val round = roundRepository.findById(roundId)
            .orElseThrow { NotFoundException("Round non trouvé") }

        if (round.daret.id != daret.id) {
            throw BadRequestException("Ce round n'appartient pas à ce Daret")
        }

        if (round.estClos) {
            throw BadRequestException("Ce round est déjà clos")
        }

        // Vérifier que tous les paiements sont effectués
        val paidCount = paymentRepository.countByRoundIdAndStatut(roundId, PaymentStatus.SUCCEEDED)
        val expectedCount = membreRepository.countActiveByDaretId(daretId) - 1 // -1 car le receveur ne paie pas

        if (paidCount < expectedCount) {
            throw BadRequestException("Tous les paiements n'ont pas été effectués")
        }

        round.close()
        round.montantTotal = daret.montantMensuel.multiply(BigDecimal(expectedCount))

        val savedRound = roundRepository.save(round)

        val activeMembreIds = membreRepository.findActiveByDaretId(daretId).map { it.user.id!! }

        eventPublisher.publishEvent(RoundClosedEvent(
            roundId = savedRound.id!!,
            roundNumero = savedRound.numero,
            daretId = daretId,
            daretNom = daret.nom,
            receveurId = round.receveur.user.id!!,
            receveurName = round.receveur.user.fullName(),
            montantTotal = savedRound.montantTotal,
            membreIds = activeMembreIds
        ))

        // Vérifier si le Daret est terminé
        val allRoundsClosed = roundRepository.findByDaretId(daretId).all { it.estClos }
        if (allRoundsClosed) {
            daret.etat = DaretStatus.TERMINEE
            daretRepository.save(daret)
            logger.info { "Daret $daretId completed" }

            eventPublisher.publishEvent(DaretCompletedEvent(
                daretId = daretId,
                nom = daret.nom,
                membreIds = activeMembreIds
            ))
        }

        logger.info { "Round $roundId closed for Daret $daretId" }

        return savedRound
    }

    // ==========================================
    // Helpers
    // ==========================================

    private fun generateUniqueCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        var code: String
        do {
            code = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
        } while (daretRepository.existsByCodeInvitation(code))
        return code
    }

    private fun checkIsAdmin(daret: Daret, userId: UUID) {
        val membre = membreRepository.findByUserIdAndDaretId(userId, daret.id!!)
        if (membre == null || !membre.isAdmin()) {
            throw ForbiddenException("Vous n'avez pas les droits d'administration sur ce Daret")
        }
    }

    fun checkIsMember(daretId: UUID, userId: UUID): Membre {
        return membreRepository.findByUserIdAndDaretId(userId, daretId)
            ?: throw ForbiddenException("Vous n'êtes pas membre de ce Daret")
    }

    // ==========================================
    // Mappers
    // ==========================================

    fun toDaretResponse(daret: Daret): DaretResponse {
        val currentRound = daret.currentRound()
        return DaretResponse(
            id = daret.id!!,
            nom = daret.nom,
            description = daret.description,
            devise = daret.devise,
            montantMensuel = daret.montantMensuel,
            taille = daret.taille,
            etat = daret.etat,
            visibilite = daret.visibilite,
            codeInvitation = daret.codeInvitation,
            delaiGraceJours = daret.delaiGraceJours,
            dateDebut = daret.dateDebut,
            dateFin = daret.dateFin,
            createurId = daret.createur.id!!,
            membresCount = daret.membres.count { it.isActive },
            currentRound = currentRound?.numero,
            createdAt = daret.createdAt
        )
    }

    fun toMembreResponse(membre: Membre): MembreResponse {
        return MembreResponse(
            id = membre.id!!,
            userId = membre.user.id!!,
            firstName = membre.user.firstName,
            lastName = membre.user.lastName,
            email = membre.user.email,
            role = membre.role,
            position = membre.position,
            isActive = membre.isActive,
            joinedAt = membre.joinedAt
        )
    }

    fun toRoundResponse(round: Round): RoundResponse {
        val payments = paymentRepository.findByRoundId(round.id!!)
        return RoundResponse(
            id = round.id!!,
            numero = round.numero,
            receveur = toMembreResponse(round.receveur),
            dateDebut = round.dateDebut,
            dateFin = round.dateFin,
            estClos = round.estClos,
            montantTotal = round.montantTotal,
            paymentsCount = payments.size,
            paidCount = payments.count { it.statut == PaymentStatus.SUCCEEDED }
        )
    }
}
