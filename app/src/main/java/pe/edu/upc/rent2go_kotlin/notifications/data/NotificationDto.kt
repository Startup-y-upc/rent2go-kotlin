package pe.edu.upc.rent2go_kotlin.notifications.data

import kotlinx.serialization.Serializable
import pe.edu.upc.rent2go_kotlin.notifications.domain.AppNotification

/** NotificationResource exacto del backend (NotificationsController). */
@Serializable
data class NotificationResponse(
    val id: Int,
    val userId: Int,
    val type: String,
    val message: String,
    val readAt: String? = null,
    val createdAt: String
)

/** PagedResponse<NotificationResource> exacto del backend. */
@Serializable
data class NotificationPagedResponse(
    val content: List<NotificationResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

fun NotificationResponse.toDomain(): AppNotification = AppNotification(
    id = id,
    userId = userId,
    type = type,
    message = message,
    readAt = readAt,
    createdAt = createdAt
)
