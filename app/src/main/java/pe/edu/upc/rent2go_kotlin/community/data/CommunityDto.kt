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

/** ConversationResource exacto del backend (CommunityTrustController). */
@Serializable
data class ConversationResponse(
    val id: Int,
    val ownerId: Int,
    val renterId: Int,
    val vehicleId: Int,
    val reservationId: Int? = null,
    val subject: String? = null,
    val status: String,
    val lastMessageAt: String? = null,
    val lastMessagePreview: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** MessageResource exacto del backend (CommunityTrustController). */
@Serializable
data class MessageResponse(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val content: String,
    val readAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isOnline: Boolean? = null,
    val lastSeenAt: String? = null,
    val unreadCount: Int? = null
)

@Serializable
data class StartConversationRequest(
    val ownerId: Int,
    val renterId: Int,
    val vehicleId: Int,
    val reservationId: Int? = null,
    val subject: String? = null
)

@Serializable
data class SendMessageRequest(
    val senderId: Int,
    val content: String
)

/** VehicleRatingResource exacto del backend: GET /vehicles/{id}/rating. */
@Serializable
data class VehicleRatingResponse(
    val vehicleId: Int,
    val average: Double,
    val count: Int
)

/** ReviewResource exacto del backend (campos usados por la sección de reseñas de K6). */
@Serializable
data class ReviewResponse(
    val id: Int,
    val reservationId: Int? = null,
    val vehicleId: Int,
    val reviewerId: Int,
    val reviewedUserId: Int? = null,
    val category: String? = null,
    val rating: Int,
    val status: String? = null,
    val comment: String? = null,
    val createdAt: String? = null
)
