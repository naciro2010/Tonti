package com.tonti.controller

import com.tonti.dto.ApiResponse
import com.tonti.dto.ai.*
import com.tonti.security.UserPrincipal
import com.tonti.service.ai.AiAgentService
import com.tonti.service.ai.ArtifactGeneratorService
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/ai")
class AiController(
    private val aiAgentService: AiAgentService,
    private val artifactGeneratorService: ArtifactGeneratorService
) {

    @PostMapping("/chat")
    fun chat(
        @RequestBody request: ChatRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val user = principal.user
        logger.info { "AI chat request from user ${user.id}: ${request.message.take(100)}" }

        val response = aiAgentService.chat(request, user)

        return ResponseEntity.ok(
            ApiResponse(success = true, data = response)
        )
    }

    @PostMapping("/artifacts/pdf")
    fun generatePdf(
        @RequestBody data: Map<String, Any>,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ByteArray> {
        logger.info { "PDF generation request from user ${principal.user.id}" }

        val pdfBytes = artifactGeneratorService.generatePdf(data)
        val title = (data["title"] as? String ?: "rapport").replace(" ", "_").lowercase()

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${title}.pdf\"")
            .body(pdfBytes)
    }

    @PostMapping("/artifacts/xlsx")
    fun generateXlsx(
        @RequestBody data: Map<String, Any>,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ByteArray> {
        logger.info { "XLSX generation request from user ${principal.user.id}" }

        val xlsxBytes = artifactGeneratorService.generateXlsx(data)
        val title = (data["title"] as? String ?: "export").replace(" ", "_").lowercase()

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${title}.xlsx\"")
            .body(xlsxBytes)
    }
}
