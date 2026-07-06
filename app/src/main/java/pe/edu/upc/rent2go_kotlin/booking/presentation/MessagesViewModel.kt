package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import pe.edu.upc.rent2go_kotlin.community.domain.Conversation

/** K1: reemplaza mockChats por conversaciones reales (CommunityTrustController). */
class MessagesViewModel(
    private val repository: pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository = DependencyProvider.communityRepository
) : ViewModel() {

    var conversations by mutableStateOf<List<Conversation>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Phase 8 (item 6) — client-derived unread counts. The backend's ConversationResource
    // has no unreadCount field (verified — see CommunityDto.kt's ConversationResponse), so
    // this mirrors Flutter's existing client-side workaround: fetch each conversation's
    // messages and count where senderId != myUserId && readAt == null.
    var unreadCountsByConversation by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set

    fun loadConversations() {
        val userId = SessionManager.getUserId()
        if (userId == -1) {
            errorMessage = "No hay sesión activa"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val loaded = repository.getConversations(userId)
                conversations = loaded
                isLoading = false
                loadUnreadCounts(userId, loaded)
            } catch (e: Exception) {
                errorMessage = "No se pudieron cargar tus conversaciones"
                isLoading = false
            }
        }
    }

    private suspend fun loadUnreadCounts(userId: Int, conversationsToCheck: List<Conversation>) {
        val counts = mutableMapOf<Int, Int>()
        for (conversation in conversationsToCheck) {
            try {
                val messages = repository.getMessages(conversation.id)
                counts[conversation.id] = messages.count { it.senderId != userId && it.readAt == null }
            } catch (e: Exception) {
                // Non-fatal: leave this conversation's badge absent rather than blocking the list.
            }
        }
        unreadCountsByConversation = counts
    }
}
