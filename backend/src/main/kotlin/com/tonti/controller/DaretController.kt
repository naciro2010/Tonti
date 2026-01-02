package com.tonti.controller

import com.tonti.dto.ApiResponse
import com.tonti.dto.PagedResponse
import com.tonti.dto.daret.*
import com.tonti.security.UserPrincipal
import com.tonti.service.DaretService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/darets")
@Tag(name = "Darets", description = "Gestion des Darets (Tontines)")
class DaretController(
    private val daretService: DaretService
) {

    @PostMapping
    @Operation(summary = "Créer un nouveau Daret")
    fun createDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateDaretRequest
    ): ResponseEntity<ApiResponse<DaretResponse>> {
        val daret = daretService.createDaret(request, userPrincipal.user)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(daretService.toDaretResponse(daret), "Daret créé avec succès"))
    }

    @GetMapping
    @Operation(summary = "Lister mes Darets")
    fun getMyDarets(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<DaretResponse>>> {
        val darets = daretService.findByUser(userPrincipal.user.id!!)
            .map { daretService.toDaretResponse(it) }
        return ResponseEntity.ok(ApiResponse.success(darets))
    }

    @GetMapping("/public")
    @Operation(summary = "Lister les Darets publics")
    fun getPublicDarets(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PagedResponse<DaretResponse>>> {
        val page = daretService.findPublicDarets(pageable)
        val response = PagedResponse(
            content = page.content.map { daretService.toDaretResponse(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            isLast = page.isLast
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un Daret")
    fun getDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<DaretDetailResponse>> {
        val daret = daretService.findByIdWithDetails(id)
        daretService.checkIsMember(id, userPrincipal.user.id!!)

        val createur = daret.membres.find { it.role == com.tonti.entity.MembreRole.CREATEUR }!!
        val response = DaretDetailResponse(
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
            membres = daret.membres.filter { it.isActive }.map { daretService.toMembreResponse(it) },
            rounds = daret.rounds.map { daretService.toRoundResponse(it) },
            createdAt = daret.createdAt
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un Daret")
    fun updateDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateDaretRequest
    ): ResponseEntity<ApiResponse<DaretResponse>> {
        val daret = daretService.updateDaret(id, request, userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(daretService.toDaretResponse(daret)))
    }

    @PostMapping("/join")
    @Operation(summary = "Rejoindre un Daret via code d'invitation")
    fun joinDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: JoinDaretRequest
    ): ResponseEntity<ApiResponse<MembreResponse>> {
        val membre = daretService.joinDaret(request.codeInvitation, userPrincipal.user)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(daretService.toMembreResponse(membre), "Vous avez rejoint le Daret"))
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Quitter un Daret")
    fun leaveDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        daretService.leaveDaret(id, userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Vous avez quitté le Daret"))
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Démarrer un Daret")
    fun startDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: StartDaretRequest
    ): ResponseEntity<ApiResponse<DaretResponse>> {
        val daret = daretService.startDaret(id, request, userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(daretService.toDaretResponse(daret), "Daret démarré"))
    }

    @PostMapping("/{id}/rounds/{roundId}/close")
    @Operation(summary = "Clôturer un round")
    fun closeRound(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID,
        @PathVariable roundId: UUID
    ): ResponseEntity<ApiResponse<RoundResponse>> {
        val round = daretService.closeRound(id, roundId, userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(daretService.toRoundResponse(round), "Round clôturé"))
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Obtenir un Daret par code d'invitation")
    fun getDaretByCode(
        @PathVariable code: String
    ): ResponseEntity<ApiResponse<DaretResponse>> {
        val daret = daretService.findByCode(code)
        return ResponseEntity.ok(ApiResponse.success(daretService.toDaretResponse(daret)))
    }
}
