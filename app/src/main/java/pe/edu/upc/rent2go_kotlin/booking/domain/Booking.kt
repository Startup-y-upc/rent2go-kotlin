package pe.edu.upc.rent2go_kotlin.booking.domain

import pe.edu.upc.rent2go_kotlin.common.Counterparty

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
    val damageReport: String?,
    // TS18/US60 — always populated (falls back to "Usuario sin nombre registrado" +
    // the raw ID label if the backend hasn't sent the nested object yet).
    val renter: Counterparty,
    val owner: Counterparty
)
