package pe.edu.upc.rent2go_kotlin.notifications.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import pe.edu.upc.rent2go_kotlin.notifications.domain.AppNotification
import pe.edu.upc.rent2go_kotlin.notifications.domain.NotificationRepository

/**
 * US50/US51/US52 (Renter) — in-app notification list + mark-as-read.
 * In-app-only per docs/spikes/SP05-push-notification-scope.md: no push SDK,
 * the list is (re)loaded on screen open (simple polling-on-open, no sockets).
 */
class NotificationsViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = mutableStateOf(NotificationsState())
    val state: State<NotificationsState> = _state

    fun loadNotifications() {
        val userId = SessionManager.getUserId()
        if (userId == -1) {
            _state.value = NotificationsState(error = "Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val page = notificationRepository.getNotifications(userId = userId, page = 0, size = 50)
                // Chronological order, most recent first (backend already sorts by
                // createdAt desc; sort defensively so the UI contract doesn't depend on it).
                val sorted = page.notifications.sortedByDescending { it.createdAt }
                _state.value = NotificationsState(notifications = sorted, isLoading = false)
            } catch (e: Exception) {
                _state.value = NotificationsState(
                    isLoading = false,
                    error = e.message ?: "No se pudieron cargar las notificaciones"
                )
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        val userId = SessionManager.getUserId()
        if (userId == -1) return

        // Optimistic UI update; reconciled with the server response on success,
        // reverted (via reload) on failure so the badge never lies to the user.
        val previous = _state.value.notifications
        val nowIso = java.time.Instant.now().toString()
        _state.value = _state.value.copy(
            notifications = previous.map {
                if (it.id == notificationId && !it.isRead) it.copy(readAt = nowIso) else it
            }
        )

        viewModelScope.launch {
            try {
                val updated = notificationRepository.markAsRead(id = notificationId, userId = userId)
                _state.value = _state.value.copy(
                    notifications = _state.value.notifications.map {
                        if (it.id == updated.id) updated else it
                    }
                )
            } catch (e: Exception) {
                // Revert optimistic change and surface the failure.
                _state.value = _state.value.copy(
                    notifications = previous,
                    error = e.message ?: "No se pudo marcar como leída"
                )
            }
        }
    }
}

data class NotificationsState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
