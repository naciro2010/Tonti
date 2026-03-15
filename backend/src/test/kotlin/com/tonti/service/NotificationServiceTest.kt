package com.tonti.service

import com.tonti.entity.Notification
import com.tonti.entity.NotificationType
import com.tonti.entity.User
import com.tonti.exception.NotFoundException
import com.tonti.repository.NotificationRepository
import com.tonti.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class NotificationServiceTest {

    @MockK private lateinit var notificationRepository: NotificationRepository
    @MockK private lateinit var userRepository: UserRepository

    private lateinit var notificationService: NotificationService
    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        notificationService = NotificationService(notificationRepository, userRepository)
        testUser = User(
            email = "test@example.com",
            passwordHash = "hash",
            firstName = "Test",
            lastName = "User"
        ).apply { id = UUID.randomUUID() }
    }

    @Test
    fun `createNotification should save and return notification`() {
        val notification = Notification(
            user = testUser,
            type = NotificationType.PAYMENT_RECEIVED,
            title = "Paiement recu",
            message = "Vous avez recu un paiement"
        ).apply { id = UUID.randomUUID() }

        every { userRepository.findById(testUser.id!!) } returns Optional.of(testUser)
        every { notificationRepository.save(any()) } returns notification

        val result = notificationService.createNotification(
            userId = testUser.id!!,
            type = NotificationType.PAYMENT_RECEIVED,
            title = "Paiement recu",
            message = "Vous avez recu un paiement"
        )

        assertNotNull(result.id)
        assertEquals("Paiement recu", result.title)
        verify { notificationRepository.save(any()) }
    }

    @Test
    fun `createNotification should throw when user not found`() {
        val userId = UUID.randomUUID()
        every { userRepository.findById(userId) } returns Optional.empty()

        assertThrows<NotFoundException> {
            notificationService.createNotification(
                userId = userId,
                type = NotificationType.SYSTEM,
                title = "Test",
                message = "Test"
            )
        }
    }

    @Test
    fun `countUnread should return count from repository`() {
        every { notificationRepository.countUnreadByUserId(testUser.id!!) } returns 5L

        val count = notificationService.countUnread(testUser.id!!)

        assertEquals(5L, count)
    }

    @Test
    fun `markAsRead should update notification`() {
        val notification = Notification(
            user = testUser,
            type = NotificationType.MEMBER_JOINED,
            title = "Nouveau membre",
            message = "Un membre a rejoint"
        ).apply { id = UUID.randomUUID() }

        every { notificationRepository.findById(notification.id!!) } returns Optional.of(notification)
        every { notificationRepository.save(any()) } answers { firstArg() }

        val result = notificationService.markAsRead(notification.id!!, testUser.id!!)

        assertTrue(result.isRead)
        verify { notificationRepository.save(any()) }
    }

    @Test
    fun `markAsRead should throw when notification belongs to another user`() {
        val otherId = UUID.randomUUID()
        val notification = Notification(
            user = testUser,
            type = NotificationType.SYSTEM,
            title = "Test",
            message = "Test"
        ).apply { id = UUID.randomUUID() }

        every { notificationRepository.findById(notification.id!!) } returns Optional.of(notification)

        assertThrows<NotFoundException> {
            notificationService.markAsRead(notification.id!!, otherId)
        }
    }

    @Test
    fun `markAllAsRead should delegate to repository`() {
        every { notificationRepository.markAllAsReadByUserId(testUser.id!!) } just runs

        notificationService.markAllAsRead(testUser.id!!)

        verify { notificationRepository.markAllAsReadByUserId(testUser.id!!) }
    }
}
