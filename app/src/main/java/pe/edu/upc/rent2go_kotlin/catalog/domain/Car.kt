package pe.edu.upc.rent2go_kotlin.catalog.domain

data class Car(
    val id: Int,
    val brand: String,
    val model: String,
    val type: String,
    val transmission: String,
    val fuel: String,
    val seats: Int,
    val pricePerDay: Double,
    val rating: Double,
    val imageUrl: String,
    val description: String,
    val ownerName: String
)
