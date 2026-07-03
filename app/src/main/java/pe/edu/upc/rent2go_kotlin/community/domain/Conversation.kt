package pe.edu.upc.rent2go_kotlin.community.domain

data class Conversation(
    val id: Int,
    val ownerId: Int,
    val renterId: Int,
    val vehicleId: Int,
    val reservationId: Int?,
    val subject: String?,
    val status: String,
    val lastMessageAt: String?,
    val lastMessagePreview: String?
)

data class ChatMessage(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val content: String,
    val createdAt: String?,
    val readAt: String?
)

data class VehicleRating(
    val vehicleId: Int,
    val average: Double,
    val count: Int
)

data class VehicleReview(
    val id: Int,
    val reviewerId: Int,
    val rating: Int,
    val comment: String?,
    val createdAt: String?
)
