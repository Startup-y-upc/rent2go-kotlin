package pe.edu.upc.rent2go_kotlin.notifications.domain

interface NotificationRepository {
    /**
     * Returns the [userId]'s notifications, most recent first, paged.
     */
    suspend fun getNotifications(userId: Int, page: Int = 0, size: Int = 20): NotificationPage

    /**
     * Marks notification [id] as read on behalf of [userId] and returns the
     * updated notification. Backend returns 403 if [userId] does not own it.
     */
    suspend fun markAsRead(id: Int, userId: Int): AppNotification
}

data class NotificationPage(
    val notifications: List<AppNotification>,
    val page: Int,
    val totalPages: Int,
    val totalElements: Int
)
