package pe.edu.upc.rent2go_kotlin.community.domain

data class UserReputation(
    val completedTrips: Int,
    val averageRating: Double,
    val acceptanceRate: Double
)
