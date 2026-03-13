package com.tonti.service

import com.tonti.entity.Notification
import com.tonti.entity.NotificationType
import com.tonti.entity.User
import com.tonti.exception.NotFoundException
import com.tonti.repository.NotificationRepository
import com.tonti.repository.UserRepository
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {

    fun createNotification(
        userId: UUID,
        type: NotificationType,
        title: String,
        message: String,
        data: String? = null
    ): Notification {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("Utilisateur non trouvé") }

        val notification = Notification(
            user = user,
            type = type,
            title = title,
            message = message,
            data = data
        )

        val saved = notificationRepository.save(notification)
        logger.debug { "Created notification ${saved.id} for user $userId: $title" }
        return saved
    }

    fun getUserNotifications(userId: UUID, pageable: Pageable): Page<Notification> {
        return notificationRepository.findByUserId(userId, pageable)
    }

    fun getUnreadNotifications(userId: UUID): List<Notification> {
        return notificationRepository.findUnreadByUserId(userId)
    }

    fun countUnread(userId: UUID): Long {
        return notificationRepository.countUnreadByUserId(userId)
    }

    @Transactional
    fun markAsRead(notificationId: UUID, userId: UUID): Notification {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NotFoundException("Notification non trouvée") }

        if (notification.user.id != userId) {
            throw NotFoundException("Notification non trouvée")
        }

        notification.markAsRead()
        return notificationRepository.save(notification)
    }

    @Transactional
    fun markAllAsRead(userId: UUID) {
        notificationRepository.markAllAsReadByUserId(userId)
        logger.debug { "Marked all notifications as read for user $userId" }
    }
}
