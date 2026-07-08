package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.booking.domain.Booking
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository
import pe.edu.upc.rent2go_kotlin.booking.domain.PaymentsRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import pe.edu.upc.rent2go_kotlin.common.SessionManager
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository

class BookingDetailViewModel(
    private val bookingRepository: BookingRepository,
    private val vehicleRepository: VehicleRepository,
    private val paymentsRepository: PaymentsRepository = pe.edu.upc.rent2go_kotlin.common.DependencyProvider.paymentsRepository,
    private val communityRepository: CommunityRepository = pe.edu.upc.rent2go_kotlin.common.DependencyProvider.communityRepository
) : ViewModel() {

    var booking: Booking? by mutableStateOf(null)
        private set

    var vehicle: Vehicle? by mutableStateOf(null)
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var error: String? by mutableStateOf(null)
        private set

    // Bugfix (reportado por Renter): la reserva creada con pago fallido/abandonado
    // quedaba sin forma de reintentar el cobro desde esta pantalla, pese a que el
    // mensaje de error de BookingConfirmationViewModel decía "revisa Mis reservas".
    // Reutiliza el mismo contrato PaymentSheetRequest/PaymentSheetOutcome que ya usa
    // BookingConfirmationViewModel, en vez de duplicar un tipo nuevo.
    var isProcessingPayment: Boolean by mutableStateOf(false)
        private set

    var paymentErrorMessage: String? by mutableStateOf(null)
        private set

    var paymentSheetRequest by mutableStateOf<PaymentSheetRequest?>(null)
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

    /**
     * Reintenta el pago de la reserva YA EXISTENTE (no crea una reserva nueva): crea un nuevo
     * PaymentIntent contra el mismo reservationId y pide al Composable presentar Stripe
     * PaymentSheet, igual que confirmAndPayBooking() en BookingConfirmationViewModel — solo que
     * aquí se omite el paso de creación de la reserva porque ya existe.
     */
    fun retryPayment() {
        val current = booking ?: return
        if (isProcessingPayment) return
        viewModelScope.launch {
            isProcessingPayment = true
            paymentErrorMessage = null
            try {
                val intent = paymentsRepository.createPaymentIntent(
                    reservationId = current.id,
                    amountCents = Math.round(current.totalAmount * 100).toInt()
                )
                if (intent.clientSecret.isBlank()) {
                    paymentErrorMessage = "El pago no pudo iniciarse. Intenta nuevamente."
                    isProcessingPayment = false
                    return@launch
                }
                // isProcessingPayment stays true while the sheet is up; cleared in
                // onPaymentSheetResult() once Stripe reports the outcome.
                paymentSheetRequest = PaymentSheetRequest.Ready(intent.clientSecret, current.reservationCode)
            } catch (e: Exception) {
                isProcessingPayment = false
                paymentErrorMessage = e.message ?: "No se pudo iniciar el cobro. Intenta nuevamente."
            }
        }
    }

    /** Called by the Composable once it has launched PaymentSheet for [paymentSheetRequest]. */
    fun onPaymentSheetLaunched() {
        paymentSheetRequest = null
    }

    /**
     * Mismos 3 desenlaces que US58: éxito -> refresca la reserva desde el backend para reflejar
     * el nuevo estado (CONFIRMED tras el webhook de Stripe); rechazo/error -> mensaje visible,
     * permite reintentar de nuevo; hoja cerrada sin completar -> sin cambio de estado.
     *
     * Bugfix (US58 follow-up): éxito ahora fuerza un sync del pago contra Stripe (POST
     * .../reservations/{id}/sync) ANTES de refrescar la reserva. El webhook de
     * `payment_intent.succeeded` es asíncrono y puede llegar después de que PaymentSheet ya
     * confirmó el cobro del lado del cliente — sin este sync, loadBookingDetail() podía leer la
     * reserva todavía en PENDING y mostrar el prompt de "pagar ahora" pese al cobro exitoso.
     */
    fun onPaymentSheetResult(result: PaymentSheetOutcome, bookingId: Int) {
        when (result) {
            is PaymentSheetOutcome.Completed -> {
                viewModelScope.launch {
                    paymentsRepository.syncPayment(bookingId)
                    isProcessingPayment = false
                    loadBookingDetail(bookingId)
                }
            }
            is PaymentSheetOutcome.Canceled -> {
                isProcessingPayment = false
                paymentErrorMessage = "Pago cancelado. La reserva sigue pendiente de pago."
            }
            is PaymentSheetOutcome.Failed -> {
                isProcessingPayment = false
                paymentErrorMessage = "El cobro falló: ${result.message} Puedes reintentarlo."
            }
        }
    }

    fun clearPaymentError() {
        paymentErrorMessage = null
    }

    var isOpeningChat: Boolean by mutableStateOf(false)
        private set

    var chatError: String? by mutableStateOf(null)
        private set

    /**
     * Phase 8 (item 5) — chat entry point from the booking detail screen. There is no
     * "get-or-create" endpoint on the backend, so this mirrors MessagesScreen's existing
     * flow: look up an existing conversation for this reservation first (getConversations
     * filtered by reservationId), and only call startConversation if none exists yet.
     */
    fun openChatForBooking(onReady: (conversationId: Int) -> Unit) {
        val current = booking ?: return
        if (isOpeningChat) return
        viewModelScope.launch {
            isOpeningChat = true
            chatError = null
            try {
                val userId = SessionManager.getUserId()
                val existing = communityRepository.getConversations(userId)
                    .firstOrNull { it.reservationId == current.id }
                val conversationId = existing?.id ?: communityRepository.startConversation(
                    ownerId = current.ownerId,
                    renterId = current.renterId,
                    vehicleId = current.vehicleId,
                    reservationId = current.id
                ).id
                isOpeningChat = false
                onReady(conversationId)
            } catch (e: Exception) {
                isOpeningChat = false
                chatError = e.message ?: "No se pudo abrir el chat. Intenta nuevamente."
            }
        }
    }
}
