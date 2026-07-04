package pe.edu.upc.rent2go_kotlin.booking.data

import kotlinx.serialization.Serializable
import pe.edu.upc.rent2go_kotlin.booking.domain.Booking
import pe.edu.upc.rent2go_kotlin.common.CounterpartyDto
import pe.edu.upc.rent2go_kotlin.common.toDomain

@Serializable
data class BookingResponse(
    val content: List<BookingDto>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

@Serializable
data class BookingDto(
    val id: Int,
    val reservationCode: String,
    val vehicleId: Int,
    val renterId: Int,
    val ownerId: Int,
    val startDate: String,
    val endDate: String,
    val totalAmount: Double,
    val status: String,
    val pickupConfirmedAt: String? = null,
    val returnConfirmedAt: String? = null,
    val pickupLocation: String,
    val returnLocation: String,
    val coveragePlan: String,
    val pickupPhotos: List<String>? = null,
    val returnPhotos: List<String>? = null,
    val damageReport: String? = null,
    // TS18/US60 — additive; absent on older cached responses (ignoreUnknownKeys handles new
    // unexpected fields, defaults here handle the reverse: an old response missing this field).
    val renter: CounterpartyDto? = null,
    val owner: CounterpartyDto? = null
)

@Serializable
data class CreateBookingRequest(
    val vehicleId: Int,
    val renterId: Int,
    val ownerId: Int,
    val startDate: String,
    val endDate: String,
    val totalAmount: Double,
    val pickupLocation: String,
    val returnLocation: String,
    val coveragePlan: String,
    val pickupPhotos: List<String>? = emptyList(),
    val returnPhotos: List<String>? = emptyList()
)

fun BookingDto.toDomain(): Booking {
    return Booking(
        id = id,
        reservationCode = reservationCode,
        vehicleId = vehicleId,
        renterId = renterId,
        ownerId = ownerId,
        startDate = startDate,
        endDate = endDate,
        totalAmount = totalAmount,
        status = status,
        pickupConfirmedAt = pickupConfirmedAt,
        returnConfirmedAt = returnConfirmedAt,
        pickupLocation = pickupLocation,
        returnLocation = returnLocation,
        coveragePlan = coveragePlan,
        pickupPhotos = pickupPhotos ?: emptyList(),
        returnPhotos = returnPhotos ?: emptyList(),
        damageReport = damageReport,
        renter = renter.toDomain(renterId, "Arrendatario #$renterId"),
        owner = owner.toDomain(ownerId, "Propietario #$ownerId")
    )
}

@Serializable
data class CancelBookingRequest(
    val requestedById: Int,
    val reason: String
)

// US15 (Renter, read-only) — mirrors AvailabilityController#checkAvailability response shape.
@Serializable
data class AvailabilityCheckResponse(
    val vehicleId: Int,
    val isAvailable: Boolean,
    val blockedRanges: List<BlockedRangeDto> = emptyList()
)

@Serializable
data class BlockedRangeDto(
    val startDate: String,
    val endDate: String
)

