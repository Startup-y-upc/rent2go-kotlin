package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import pe.edu.upc.rent2go_kotlin.community.domain.ReviewCategory

/**
 * US43 (Renter) — rating/review submission on a completed reservation.
 * Mirrors Flutter's Phase 6 rating screen against the same backend endpoint
 * (POST /api/v1/community-trust/reviews).
 */
class RatingViewModel(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _state = mutableStateOf(RatingState())
    val state: State<RatingState> = _state

    fun submitRating(
        reservationId: Int,
        vehicleId: Int,
        reviewedUserId: Int?,
        category: ReviewCategory,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit
    ) {
        val reviewerId = SessionManager.getUserId()
        if (reviewerId == -1) {
            _state.value = _state.value.copy(error = "Usuario no autenticado")
            return
        }
        if (rating !in 1..5) {
            _state.value = _state.value.copy(error = "Selecciona una calificación de 1 a 5")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            try {
                communityRepository.submitReview(
                    reservationId = reservationId,
                    vehicleId = vehicleId,
                    reviewerId = reviewerId,
                    reviewedUserId = reviewedUserId,
                    category = category,
                    rating = rating,
                    comment = comment.trim().ifBlank { null }
                )
                _state.value = _state.value.copy(isSubmitting = false, isSubmitted = true)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "No se pudo enviar la reseña"
                )
            }
        }
    }

    fun resetState() {
        _state.value = RatingState()
    }
}

data class RatingState(
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)
