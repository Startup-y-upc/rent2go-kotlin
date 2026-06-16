package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.booking.data.toDomain
import pe.edu.upc.rent2go_kotlin.booking.domain.Booking
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import pe.edu.upc.rent2go_kotlin.common.SessionManager

class BookingsViewModel(
    private val bookingRepository: BookingRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = mutableStateOf(BookingsState())
    val state: State<BookingsState> = _state

    fun loadBookings() {
        val renterId = SessionManager.getUserId()
        if (renterId == -1) {
            _state.value = BookingsState(error = "Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            _state.value = BookingsState(isLoading = true)
            try {
                // Fetch renter bookings (page 1)
                val response = bookingRepository.getBookingsByRenter(renterId = renterId, page = 1)
                val bookings = response.content.map { it.toDomain() }

                // Fetch corresponding vehicles concurrently
                val vehicleIds = bookings.map { it.vehicleId }.distinct()
                val vehiclesList = vehicleIds.map { vehicleId ->
                    async {
                        try {
                            vehicleId to vehicleRepository.getVehicleById(vehicleId)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                val vehiclesMap = vehiclesList.toMap()

                _state.value = BookingsState(
                    bookings = bookings,
                    vehicles = vehiclesMap,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = BookingsState(
                    isLoading = false,
                    error = e.message ?: "Error al cargar las reservas"
                )
            }
        }
    }
}

data class BookingsState(
    val bookings: List<Booking> = emptyList(),
    val vehicles: Map<Int, Vehicle> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String = ""
)
