package pe.edu.upc.rent2go_kotlin.catalog.data

import kotlinx.serialization.Serializable

@Serializable
data class CarDto(
    val id: Int,
    val brand: String,
    val model: String,
    val type: String,
    val transmission: String,
    val fuel: String,
    val seats: Int,
    val price_per_day: Double,
    val rating: Double,
    val image_url: String,
    val description: String,
    val owner_name: String
)
