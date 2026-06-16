package pe.edu.upc.rent2go_kotlin.booking.domain

data class Booking(
    val id: Int,
    val reservationCode: String,
    val vehicleId: Int,
    val renterId: Int,
    val ownerId: Int,
    val startDate: String,
    val endDate: String,
    val totalAmount: Double,
    val status: String,
    val pickupConfirmedAt: String?,
    val returnConfirmedAt: String?,
    val pickupLocation: String,
    val returnLocation: String,
    val coveragePlan: String,
    val pickupPhotos: List<String>,
    val returnPhotos: List<String>,
    val damageReport: String?
)
