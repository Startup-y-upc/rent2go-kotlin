package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository

/**
 * US41 (Renter) — dispute/report submission on a reservation.
 * Mirrors Flutter's Phase 6 dispute screen against the same backend endpoint
 * (POST /api/v1/community-trust/reservations/{id}/disputes).
 */
class DisputeViewModel(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _state = mutableStateOf(DisputeState())
    val state: State<DisputeState> = _state

    fun submitDispute(reservationId: Int, reason: String, onSuccess: () -> Unit) {
        val reporterId = SessionManager.getUserId()
        if (reporterId == -1) {
            _state.value = _state.value.copy(error = "Usuario no autenticado")
            return
        }
        if (reason.isBlank()) {
            _state.value = _state.value.copy(error = "Describe el motivo del reporte")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            try {
                communityRepository.openDispute(reservationId, reporterId, reason.trim())
                _state.value = _state.value.copy(isSubmitting = false, isSubmitted = true)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "No se pudo enviar el reporte"
                )
            }
        }
    }

    fun resetState() {
        _state.value = DisputeState()
    }
}

data class DisputeState(
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)
