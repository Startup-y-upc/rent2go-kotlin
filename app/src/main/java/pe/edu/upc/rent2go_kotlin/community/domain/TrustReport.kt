package pe.edu.upc.rent2go_kotlin.community.domain

// US41 (Renter) — dispute/report on a reservation, mirroring backend's
// TrustReportResource (community-trust bounded context).
data class TrustReport(
    val id: Int,
    val reservationId: Int?,
    val reporterId: Int,
    val reason: String,
    val status: String,
    val createdAt: String?
)

// US43 (Renter) — submitted review/rating, mirroring backend's ReviewResource.
data class SubmittedReview(
    val id: Int,
    val reservationId: Int?,
    val vehicleId: Int,
    val reviewerId: Int,
    val reviewedUserId: Int?,
    val category: String?,
    val rating: Int,
    val status: String?,
    val comment: String?,
    val createdAt: String?
)

enum class ReviewCategory(val apiValue: String, val label: String) {
    VEHICLE("VEHICLE", "Vehículo"),
    RENTAL_EXPERIENCE("RENTAL_EXPERIENCE", "Experiencia de alquiler"),
    DRIVER("DRIVER", "Conductor"),
    COMMUNICATION("COMMUNICATION", "Comunicación")
}
