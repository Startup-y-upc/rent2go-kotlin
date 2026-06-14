package pe.edu.upc.rent2go_kotlin.community.data

import kotlinx.serialization.Serializable

@Serializable
data class UserReputationResponse(
    val userId: Int,
    val approvedReviewCount: Int,
    val averageRating: Double,
    val trustScore: Int,
    val blocked: Boolean,
    val lastModerationReason: String? = null,
    val completedTrips: Int,
    val acceptanceRate: Double,
    val responseRate: Double
)
