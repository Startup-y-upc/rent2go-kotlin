package pe.edu.upc.rent2go_kotlin.catalog.data

import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponse(
    val content: List<VehicleDto>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

@Serializable
data class VehicleDto(
    val id: Int,
    val ownerId: Int,
    val licensePlate: String,
    val make: String,
    val model: String,
    val year: Int,
    val vin: String,
    val status: String,
    val dailyPrice: Double,
    val categoryName: String,
    val location: String,
    val description: String,
    val seats: Int,
    val transmission: String,
    val fuelType: String,
    val features: List<String> = emptyList(),
    val primaryImageUrl: String? = null,
    val primaryImagePath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String,
    val updatedAt: String
)
