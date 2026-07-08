package pe.edu.upc.rent2go_kotlin.notifications.domain

// US50/US51/US52 (Renter) — in-app-only notifications (no FCM/APNs push,
// per docs/spikes/SP05-push-notification-scope.md). Mirrors backend's
// NotificationResource exactly.
data class AppNotification(
    val id: Int,
    val userId: Int,
    val type: String,
    val message: String,
    val readAt: String?,
    val createdAt: String
) {
    val isRead: Boolean get() = readAt != null
}
