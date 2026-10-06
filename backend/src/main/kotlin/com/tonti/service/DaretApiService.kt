package com.tonti.service

import com.tonti.dto.daret.CreateDaretRequest
import com.tonti.dto.daret.DaretDetailResponse
import com.tonti.dto.daret.DaretResponse
import com.tonti.dto.daret.MembreResponse
import com.tonti.dto.daret.RoundResponse
import com.tonti.dto.daret.StartDaretRequest
import com.tonti.dto.daret.UpdateDaretRequest
import com.tonti.entity.MembreRole
import com.tonti.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Couche applicative de l'API Darets : orchestre [DaretService] et construit les DTOs
 * à l'intérieur de la transaction (les associations JPA sont chargées à la demande et
 * `open-in-view` est désactivé).
 */
@Service
@Transactional(readOnly = true)
class DaretApiService(
    private val daretService: DaretService
) {

    @Transactional
    fun create(request: CreateDaretRequest, user: User): DaretResponse =
        daretService.toDaretResponse(daretService.createDaret(request, user))

    fun listMine(userId: UUID): List<DaretResponse> =
        daretService.findByUser(userId).map { daretService.toDaretResponse(it) }

    fun listPublic(pageable: Pageable): Page<DaretResponse> =
        daretService.findPublicDarets(pageable).map { daretService.toDaretResponse(it) }

    fun getByCode(code: String): DaretResponse =
        daretService.toDaretResponse(daretService.findByCode(code))

    fun getDetail(id: UUID, userId: UUID): DaretDetailResponse {
        daretService.checkIsMember(id, userId)
        val daret = daretService.findByIdWithDetails(id)
        val createur = daret.membres.first { it.role == MembreRole.CREATEUR }

        return DaretDetailResponse(
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
            createur = daretService.toMembreResponse(createur),
            membres = daret.membres.filter { it.isActive }
                .sortedBy { it.position ?: Int.MAX_VALUE }
                .map { daretService.toMembreResponse(it) },
            rounds = daret.rounds.sortedBy { it.numero }.map { daretService.toRoundResponse(it) },
            createdAt = daret.createdAt
        )
    }

    @Transactional
    fun update(id: UUID, request: UpdateDaretRequest, userId: UUID): DaretResponse =
        daretService.toDaretResponse(daretService.updateDaret(id, request, userId))

    @Transactional
    fun join(code: String, user: User): MembreResponse =
        daretService.toMembreResponse(daretService.joinDaret(code, user))

    @Transactional
    fun leave(id: UUID, userId: UUID) = daretService.leaveDaret(id, userId)

    @Transactional
    fun start(id: UUID, request: StartDaretRequest, userId: UUID): DaretResponse =
        daretService.toDaretResponse(daretService.startDaret(id, request, userId))

    @Transactional
    fun closeRound(id: UUID, roundId: UUID, userId: UUID): RoundResponse =
        daretService.toRoundResponse(daretService.closeRound(id, roundId, userId))
}
