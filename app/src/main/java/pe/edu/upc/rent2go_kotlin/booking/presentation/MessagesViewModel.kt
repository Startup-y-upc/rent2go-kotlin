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
import java.time.Instant
import java.time.format.DateTimeParseException

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
                // Entering the Messages screen means the user has now seen the
                // latest activity — record it so the nav-bar dot clears without
                // any extra call.
                SessionManager.markMessagesOpenedNow()
            } catch (e: Exception) {
                errorMessage = "No se pudieron cargar tus conversaciones"
                isLoading = false
            }
        }
    }
}

/**
 * Whether any conversation has activity newer than the last time the user
 * opened Messages locally. Replaces the previous N+1 unread-count logic (one
 * GET .../messages call per conversation just to count unread items client
 * side): conversations already carry lastMessageAt for free, so this only
 * needs a local timestamp comparison — no per-conversation message fetch, no
 * numeric count, just "is there something new".
 */
fun hasRecentMessageActivity(conversations: List<Conversation>): Boolean {
    val lastOpenedAt = SessionManager.getMessagesLastOpenedAt()
        ?: return conversations.any { !it.lastMessageAt.isNullOrEmpty() }
    return conversations.any { conversation ->
        val iso = conversation.lastMessageAt
        if (iso.isNullOrEmpty()) return@any false
        val messageAtMillis = try {
            Instant.parse(iso).toEpochMilli()
        } catch (e: DateTimeParseException) {
            return@any false
        }
        messageAtMillis > lastOpenedAt
    }
}
