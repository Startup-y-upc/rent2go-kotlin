package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import pe.edu.upc.rent2go_kotlin.common.SessionManager

class VehicleDetailViewModel(
    private val repository: VehicleRepository,
    private val bookingRepository: BookingRepository
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
        }
    }
}

data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String = "",
    val occupiedUntil: String? = null
)
