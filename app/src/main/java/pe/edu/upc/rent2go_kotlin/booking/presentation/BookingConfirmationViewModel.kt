package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.booking.data.CreateBookingRequest
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository
import pe.edu.upc.rent2go_kotlin.booking.domain.CoveragePlan
import pe.edu.upc.rent2go_kotlin.booking.domain.PaymentsRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BookingConfirmationViewModel(
    private val bookingRepository: BookingRepository,
    private val vehicleRepository: VehicleRepository,
    private val paymentsRepository: PaymentsRepository = pe.edu.upc.rent2go_kotlin.common.DependencyProvider.paymentsRepository
) : ViewModel() {

    // K8: planes reales del backend (BASIC/STANDARD/PREMIUM/NONE), ya no
    // ESSENTIAL/PLUS/PREMIUM inventados ni precios hardcodeados S/0/8/14.
    var coveragePlans by mutableStateOf<List<CoveragePlan>>(emptyList())
        private set
    var isLoadingCoveragePlans by mutableStateOf(false)
        private set

    var vehicle by mutableStateOf<Vehicle?>(null)
        private set

    var isLoadingVehicle by mutableStateOf(false)
        private set

    var errorVehicle by mutableStateOf<String?>(null)
        private set

    // Inputs
    var startDate by mutableStateOf(LocalDate.now())
    var endDate by mutableStateOf(LocalDate.now().plusDays(2))
    var coveragePlan by mutableStateOf("STANDARD") // BASIC, STANDARD, PREMIUM, NONE (códigos reales del backend)

    // Form Status
    var isSubmitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isSuccess by mutableStateOf(false)
        private set

    // US15 (Renter, read-only) — availability of the selected date range.
    // Populated by checkAvailability(); does not let the renter create/modify
    // blocks, only warns them before submitting a reservation request.
    var isCheckingAvailability by mutableStateOf(false)
        private set
    var isRangeAvailable by mutableStateOf(true)
        private set
    var blockedRanges by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set
    var availabilityError by mutableStateOf<String?>(null)
        private set

    // Price Calculations
    val rentalDays: Long
        get() = ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(1)

    val dailyPrice: Double
        get() = vehicle?.dailyPrice ?: 0.0

    val subtotal: Double
        get() = dailyPrice * rentalDays

    val coveragePricePerDay: Double
        get() = coveragePlans.firstOrNull { it.code == coveragePlan }?.dailyRateUsd ?: 0.0

    val coverageTotal: Double
        get() = coveragePricePerDay * rentalDays

    val serviceFee: Double
        get() = subtotal * 0.05 // 5% service fee

    val totalAmount: Double
        get() = subtotal + coverageTotal + serviceFee

    fun loadVehicle(vehicleId: Int) {
        viewModelScope.launch {
            isLoadingVehicle = true
            errorVehicle = null
            try {
                vehicle = vehicleRepository.getVehicleById(vehicleId)
                checkAvailability()
            } catch (e: Exception) {
                errorVehicle = e.message ?: "Error al obtener detalles del vehículo"
            } finally {
                isLoadingVehicle = false
            }
        }
        loadCoveragePlans()
    }

    private fun loadCoveragePlans() {
        viewModelScope.launch {
            isLoadingCoveragePlans = true
            try {
                coveragePlans = paymentsRepository.getCoveragePlans()
                // Selecciona STANDARD si existe, o el primer plan real devuelto por
                // el backend — nunca un código inventado como "PLUS"/"ESSENTIAL".
                if (coveragePlans.isNotEmpty() && coveragePlans.none { it.code == coveragePlan }) {
                    coveragePlan = coveragePlans.firstOrNull { it.code == "STANDARD" }?.code
                        ?: coveragePlans.first().code
                }
            } catch (e: Exception) {
                // Si falla, coveragePlans queda vacío y la UI debe mostrar un estado
                // de error explícito en vez de precios inventados.
            } finally {
                isLoadingCoveragePlans = false
            }
        }
    }

    /**
     * US15 (Renter, read-only) — re-checks availability whenever the renter
     * changes pickup/return dates, so they see occupied dates before paying.
     * Call this after mutating [startDate]/[endDate] from the UI.
     */
    fun checkAvailability() {
        val currentVehicle = vehicle ?: return
        viewModelScope.launch {
            isCheckingAvailability = true
            availabilityError = null
            try {
                val response = bookingRepository.checkAvailability(
                    vehicleId = currentVehicle.id,
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )
                isRangeAvailable = response.isAvailable
                blockedRanges = response.blockedRanges.map { it.startDate to it.endDate }
            } catch (e: Exception) {
                // Non-fatal: if the availability check fails (network, etc.),
                // do not block the reservation flow — only surface a soft warning.
                availabilityError = e.message ?: "No se pudo verificar la disponibilidad"
            } finally {
                isCheckingAvailability = false
            }
        }
    }

    fun confirmAndPayBooking(onSuccess: () -> Unit) {
        val currentVehicle = vehicle ?: return
        val renterId = SessionManager.getUserId()
        if (renterId == -1) {
            errorMessage = "Usuario no autenticado"
            return
        }
        if (!isRangeAvailable) {
            errorMessage = "El vehículo no está disponible en las fechas seleccionadas."
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null
            try {
                val request = CreateBookingRequest(
                    vehicleId = currentVehicle.id,
                    renterId = renterId,
                    ownerId = currentVehicle.ownerId,
                    startDate = startDate.toString(), // format yyyy-MM-dd
                    endDate = endDate.toString(),     // format yyyy-MM-dd
                    totalAmount = totalAmount,
                    pickupLocation = currentVehicle.location,
                    returnLocation = currentVehicle.location,
                    coveragePlan = coveragePlan,
                    pickupPhotos = emptyList(),
                    returnPhotos = emptyList()
                )
                bookingRepository.createBooking(request)
                isSuccess = true
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al procesar la reserva"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
