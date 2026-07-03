package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository
import pe.edu.upc.rent2go_kotlin.booking.domain.FavoritesRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import pe.edu.upc.rent2go_kotlin.community.domain.VehicleRating
import pe.edu.upc.rent2go_kotlin.community.domain.VehicleReview

class VehicleDetailViewModel(
    private val repository: VehicleRepository,
    private val bookingRepository: BookingRepository,
    private val favoritesRepository: FavoritesRepository = pe.edu.upc.rent2go_kotlin.common.DependencyProvider.favoritesRepository,
    private val communityRepository: CommunityRepository = pe.edu.upc.rent2go_kotlin.common.DependencyProvider.communityRepository
) : ViewModel() {

    private val _state = mutableStateOf(VehicleDetailState())
    val state: State<VehicleDetailState> = _state

    fun loadVehicle(id: Int) {
        viewModelScope.launch {
            _state.value = VehicleDetailState(isLoading = true)
            try {
                val vehicle = repository.getVehicleById(id)
                var occupiedUntilDate: String? = null
                val userId = SessionManager.getUserId()
                if (userId != -1) {
                    try {
                        val response = bookingRepository.getBookingsByRenter(renterId = userId, page = 1)
                        val activeBooking = response.content
                            .filter { it.vehicleId == id && (it.status == "PENDING" || it.status == "CONFIRMED" || it.status == "ACTIVE") }
                            .maxByOrNull { it.endDate }
                        if (activeBooking != null) {
                            occupiedUntilDate = activeBooking.endDate
                        }
                    } catch (e: Exception) {
                        // Non-fatal, fallback to no occupancy info
                    }
                }
                _state.value = VehicleDetailState(
                    vehicle = vehicle,
                    occupiedUntil = occupiedUntilDate
                )
            } catch (e: Exception) {
                _state.value = VehicleDetailState(
                    error = e.message ?: "Error al cargar el detalle del vehículo"
                )
            }
            loadFavoriteState(id)
            loadRatingAndReviews(id)
        }
    }

    private suspend fun loadFavoriteState(vehicleId: Int) {
        val userId = SessionManager.getUserId()
        if (userId == -1) return
        try {
            val fav = favoritesRepository.isFavorite(userId, vehicleId)
            _state.value = _state.value.copy(isFavorite = fav)
        } catch (e: Exception) {
            // No bloquea el detalle si falla la consulta de favoritos.
        }
    }

    private suspend fun loadRatingAndReviews(vehicleId: Int) {
        try {
            val rating = communityRepository.getVehicleRating(vehicleId)
            val reviews = communityRepository.getVehicleReviews(vehicleId)
            _state.value = _state.value.copy(rating = rating, reviews = reviews)
        } catch (e: Exception) {
            // Sección de reseñas queda vacía si falla, no bloquea el resto del detalle.
        }
    }

    fun toggleFavorite(vehicleId: Int) {
        val userId = SessionManager.getUserId()
        if (userId == -1) return
        val current = _state.value.isFavorite
        // Actualización optimista + reconciliación real (K5): el heart refleja
        // de inmediato la intención del usuario, pero la fuente de verdad es
        // siempre el backend (POST/DELETE /api/v1/favorites).
        _state.value = _state.value.copy(isFavorite = !current)
        viewModelScope.launch {
            try {
                if (current) {
                    favoritesRepository.removeFavorite(userId, vehicleId)
                } else {
                    favoritesRepository.addFavorite(userId, vehicleId)
                }
            } catch (e: Exception) {
                // Revierte si la llamada real falla.
                _state.value = _state.value.copy(isFavorite = current)
            }
        }
    }
}

data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String = "",
    val occupiedUntil: String? = null,
    val isFavorite: Boolean = false,
    val rating: VehicleRating? = null,
    val reviews: List<VehicleReview> = emptyList()
)
