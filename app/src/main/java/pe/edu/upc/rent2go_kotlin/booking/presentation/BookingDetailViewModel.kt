package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.booking.domain.Booking
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository

class BookingDetailViewModel(
    private val bookingRepository: BookingRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    var booking: Booking? by mutableStateOf(null)
        private set

    var vehicle: Vehicle? by mutableStateOf(null)
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var error: String? by mutableStateOf(null)
        private set

    fun loadBookingDetail(bookingId: Int) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val bookingData = bookingRepository.getBookingById(bookingId)
                booking = bookingData

                // Resolve the associated vehicle
                try {
                    vehicle = vehicleRepository.getVehicleById(bookingData.vehicleId)
                } catch (e: Exception) {
                    // Vehicle not found is non-fatal; we still show the booking
                    vehicle = null
                }

                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                error = e.message ?: "Error al cargar el detalle de la reserva"
            }
        }
    }
}
