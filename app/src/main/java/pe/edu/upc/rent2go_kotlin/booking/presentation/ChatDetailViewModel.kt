package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.booking.domain.Booking
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import pe.edu.upc.rent2go_kotlin.community.domain.ChatMessage
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository

/** K2: reemplaza chatMessages en memoria por mensajes reales y persistidos. */
class ChatDetailViewModel(
    private val repository: CommunityRepository = DependencyProvider.communityRepository,
    private val bookingRepository: pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository = DependencyProvider.bookingRepository
) : ViewModel() {

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var sending by mutableStateOf(false)
        private set

    // Tarjeta de resumen de reserva: solo se puebla si la conversación tiene un
    // reservationId real (ConversationResource.reservationId es nullable); si es
    // null, se oculta la tarjeta en vez de mostrar datos fabricados.
    var reservationSummary by mutableStateOf<Booking?>(null)
        private set

    var conversationSubject by mutableStateOf<String?>(null)
        private set

    // TS18/US60 — real counterparty name for the chat header, replacing the hardcoded
    // "Conversación" fallback / free-text subject. Null until the conversation loads;
    // the Composable's own userName param covers that brief loading window.
    var counterpartyName by mutableStateOf<String?>(null)
        private set
    var counterpartyKycVerified by mutableStateOf(false)
        private set

    val currentUserId: Int get() = SessionManager.getUserId()

    fun loadMessages(conversationId: Int, reservationIdHint: Int?) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                messages = repository.getMessages(conversationId)
            } catch (e: Exception) {
                errorMessage = "No se pudieron cargar los mensajes"
            } finally {
                isLoading = false
            }
        }
        viewModelScope.launch {
            try {
                val conversation = repository.getConversation(conversationId)
                conversationSubject = conversation?.subject
                // TS18/US60 — the header should show who you're talking to (real name +
                // KYC status), not the free-text subject. currentUserId tells us whether
                // we are the owner or the renter in this conversation, so we can pick the
                // *other* party's counterparty object.
                if (conversation != null) {
                    val isCurrentUserOwner = currentUserId == conversation.ownerId
                    val counterparty = if (isCurrentUserOwner) conversation.renter else conversation.owner
                    counterpartyName = counterparty.fullName
                    counterpartyKycVerified = counterparty.kycVerified
                }
                // Si no se recibió un reservationId por navegación, se resuelve
                // consultando la conversación real (GET /conversations/{id}) —
                // su campo reservationId es la fuente de verdad, no un valor local.
                val reservationId = reservationIdHint ?: conversation?.reservationId
                reservationSummary = reservationId?.let { bookingRepository.getBookingById(it) }
            } catch (e: Exception) {
                reservationSummary = null
            }
        }
    }

    fun sendMessage(conversationId: Int, content: String) {
        if (content.isBlank()) return
        val senderId = currentUserId
        if (senderId == -1) return
        viewModelScope.launch {
            sending = true
            try {
                val sent = repository.sendMessage(conversationId, senderId, content)
                messages = messages + sent
            } catch (e: Exception) {
                errorMessage = "No se pudo enviar el mensaje"
            } finally {
                sending = false
            }
        }
    }
}
