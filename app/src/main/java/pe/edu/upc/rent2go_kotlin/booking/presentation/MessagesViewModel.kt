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
                conversations = repository.getConversations(userId)
            } catch (e: Exception) {
                errorMessage = "No se pudieron cargar tus conversaciones"
            } finally {
                isLoading = false
            }
        }
    }
}
