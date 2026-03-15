package com.tonti.controller

import com.tonti.dto.ApiResponse
import com.tonti.dto.PagedResponse
import com.tonti.entity.NotificationType
import com.tonti.security.UserPrincipal
import com.tonti.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Gestion des notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    @Operation(summary = "Lister mes notifications")
    fun getNotifications(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> {
        val page = notificationService.getUserNotifications(userPrincipal.user.id!!, pageable)
        val response = PagedResponse(
            content = page.content.map { it.toResponse() },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            isLast = page.isLast
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/unread")
    @Operation(summary = "Lister les notifications non lues")
    fun getUnreadNotifications(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<NotificationResponse>>> {
        val notifications = notificationService.getUnreadNotifications(userPrincipal.user.id!!)
            .map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(notifications))
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Compter les notifications non lues")
    fun countUnread(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<UnreadCountResponse>> {
        val count = notificationService.countUnread(userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(UnreadCountResponse(count)))
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marquer une notification comme lue")
    fun markAsRead(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<NotificationResponse>> {
        val notification = notificationService.markAsRead(id, userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(notification.toResponse()))
    }

    @PutMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    fun markAllAsRead(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit>> {
        notificationService.markAllAsRead(userPrincipal.user.id!!)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Toutes les notifications ont été marquées comme lues"))
    }
}

// ==========================================
// DTOs
// ==========================================

data class NotificationResponse(
    val id: UUID,
    val type: NotificationType,
    val title: String,
    val message: String,
    val data: String?,
    val isRead: Boolean,
    val readAt: Instant?,
    val createdAt: Instant
)

data class UnreadCountResponse(
    val count: Long
)

private fun com.tonti.entity.Notification.toResponse() = NotificationResponse(
    id = id!!,
    type = type,
    title = title,
    message = message,
    data = data,
    isRead = isRead,
    readAt = readAt,
    createdAt = createdAt
)
