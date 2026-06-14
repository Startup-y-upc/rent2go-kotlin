package pe.edu.upc.rent2go_kotlin.catalog.domain

data class Vehicle(
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
    val features: List<String>,
    val primaryImageUrl: String?,
    val primaryImagePath: String?,
    val createdAt: String,
    val updatedAt: String
)
