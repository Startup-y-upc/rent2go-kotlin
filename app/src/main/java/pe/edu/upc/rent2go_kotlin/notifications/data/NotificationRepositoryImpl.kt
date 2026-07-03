package pe.edu.upc.rent2go_kotlin.notifications.data

import pe.edu.upc.rent2go_kotlin.notifications.domain.AppNotification
import pe.edu.upc.rent2go_kotlin.notifications.domain.NotificationPage
import pe.edu.upc.rent2go_kotlin.notifications.domain.NotificationRepository

class NotificationRepositoryImpl(
    private val api: NotificationApi
) : NotificationRepository {

    override suspend fun getNotifications(userId: Int, page: Int, size: Int): NotificationPage {
        val response = api.getNotifications(userId = userId, page = page, size = size)
        return NotificationPage(
            notifications = response.content.map { it.toDomain() },
            page = response.page,
            totalPages = response.totalPages,
            totalElements = response.totalElements
        )
    }

    override suspend fun markAsRead(id: Int, userId: Int): AppNotification {
        return api.markAsRead(id = id, userId = userId).toDomain()
    }
}
