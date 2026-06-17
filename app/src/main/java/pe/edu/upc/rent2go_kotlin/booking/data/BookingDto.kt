package pe.edu.upc.rent2go_kotlin.booking.data

import kotlinx.serialization.Serializable
import pe.edu.upc.rent2go_kotlin.booking.domain.Booking

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
    val damageReport: String? = null
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
        damageReport = damageReport
    )
}
