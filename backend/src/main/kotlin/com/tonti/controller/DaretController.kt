package com.tonti.controller

import com.tonti.dto.ApiResponse
import com.tonti.dto.PagedResponse
import com.tonti.dto.daret.CreateDaretRequest
import com.tonti.dto.daret.DaretDetailResponse
import com.tonti.dto.daret.DaretResponse
import com.tonti.dto.daret.JoinDaretRequest
import com.tonti.dto.daret.MembreResponse
import com.tonti.dto.daret.RoundResponse
import com.tonti.dto.daret.StartDaretRequest
import com.tonti.dto.daret.UpdateDaretRequest
import com.tonti.security.UserPrincipal
import com.tonti.service.DaretApiService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/darets")
@Tag(name = "Darets", description = "Gestion des Darets (tontines)")
class DaretController(
    private val darets: DaretApiService
) {

    @PostMapping
    @Operation(summary = "Créer un nouveau Daret")
    fun createDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: CreateDaretRequest
    ): ResponseEntity<ApiResponse<DaretResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(darets.create(request, userPrincipal.user), "Daret créé avec succès"))

    @GetMapping
    @Operation(summary = "Lister mes Darets")
    fun getMyDarets(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<DaretResponse>>> =
        ResponseEntity.ok(ApiResponse.success(darets.listMine(userPrincipal.user.id!!)))

    @GetMapping("/public")
    @Operation(summary = "Lister les Darets publics en recrutement")
    fun getPublicDarets(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PagedResponse<DaretResponse>>> =
        ResponseEntity.ok(ApiResponse.success(PagedResponse.of(darets.listPublic(pageable))))

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un Daret")
    fun getDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<DaretDetailResponse>> =
        ResponseEntity.ok(ApiResponse.success(darets.getDetail(id, userPrincipal.user.id!!)))

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un Daret")
    fun updateDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateDaretRequest
    ): ResponseEntity<ApiResponse<DaretResponse>> =
        ResponseEntity.ok(ApiResponse.success(darets.update(id, request, userPrincipal.user.id!!)))

    @PostMapping("/join")
    @Operation(summary = "Rejoindre un Daret via code d'invitation")
    fun joinDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Valid @RequestBody request: JoinDaretRequest
    ): ResponseEntity<ApiResponse<MembreResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(darets.join(request.codeInvitation, userPrincipal.user), "Vous avez rejoint le Daret"))

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Quitter un Daret")
    fun leaveDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        darets.leave(id, userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Vous avez quitté le Daret"))
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Démarrer un Daret (tirage ou ordre choisi des bénéficiaires)")
    fun startDaret(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: StartDaretRequest
    ): ResponseEntity<ApiResponse<DaretResponse>> =
        ResponseEntity.ok(ApiResponse.success(darets.start(id, request, userPrincipal.user.id!!), "Daret démarré"))

    @PostMapping("/{id}/rounds/{roundId}/close")
    @Operation(summary = "Clôturer un round")
    fun closeRound(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID,
        @PathVariable roundId: UUID
    ): ResponseEntity<ApiResponse<RoundResponse>> =
        ResponseEntity.ok(ApiResponse.success(darets.closeRound(id, roundId, userPrincipal.user.id!!), "Round clôturé"))

    @GetMapping("/code/{code}")
    @Operation(summary = "Aperçu d'un Daret par code d'invitation")
    fun getDaretByCode(
        @PathVariable code: String
    ): ResponseEntity<ApiResponse<DaretResponse>> =
        ResponseEntity.ok(ApiResponse.success(darets.getByCode(code)))
}
