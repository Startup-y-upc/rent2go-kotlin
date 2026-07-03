package pe.edu.upc.rent2go_kotlin.community.data

import pe.edu.upc.rent2go_kotlin.community.domain.ChatMessage
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import pe.edu.upc.rent2go_kotlin.community.domain.Conversation
import pe.edu.upc.rent2go_kotlin.community.domain.ReviewCategory
import pe.edu.upc.rent2go_kotlin.community.domain.SubmittedReview
import pe.edu.upc.rent2go_kotlin.community.domain.TrustReport
import pe.edu.upc.rent2go_kotlin.community.domain.UserReputation
import pe.edu.upc.rent2go_kotlin.community.domain.VehicleRating
import pe.edu.upc.rent2go_kotlin.community.domain.VehicleReview

class CommunityRepositoryImpl(
    private val api: CommunityApi
) : CommunityRepository {
    override suspend fun getUserReputation(userId: Int): UserReputation {
        val response = api.getUserReputation(userId)
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Respuesta del servidor vacía")
            return UserReputation(
                completedTrips = body.completedTrips,
                averageRating = body.averageRating,
                acceptanceRate = body.acceptanceRate
            )
        }
        // K4: ya no se devuelve un 5.0/100.0 que aparenta ser un puntaje perfecto
        // real — se propaga el error para que la capa de presentación muestre un
        // estado de carga/error explícito en vez de datos fabricados.
        throw Exception("No se pudo obtener la reputación del usuario (HTTP ${response.code()})")
    }

    override suspend fun getConversations(userId: Int): List<Conversation> {
        val response = api.getConversations(userId)
        if (response.isSuccessful) {
            return (response.body() ?: emptyList()).map { it.toDomain() }
        }
        throw Exception("No se pudieron cargar las conversaciones (HTTP ${response.code()})")
    }

    override suspend fun getConversation(conversationId: Int): Conversation? {
        val response = api.getConversation(conversationId)
        if (response.isSuccessful) {
            return response.body()?.toDomain()
        }
        return null
    }

    override suspend fun startConversation(ownerId: Int, renterId: Int, vehicleId: Int, reservationId: Int?): Conversation {
        val response = api.startConversation(
            StartConversationRequest(ownerId = ownerId, renterId = renterId, vehicleId = vehicleId, reservationId = reservationId)
        )
        if (response.isSuccessful) {
            return (response.body() ?: throw Exception("Respuesta vacía al iniciar conversación")).toDomain()
        }
        throw Exception("No se pudo iniciar la conversación (HTTP ${response.code()})")
    }

    override suspend fun getMessages(conversationId: Int): List<ChatMessage> {
        val response = api.getMessages(conversationId)
        if (response.isSuccessful) {
            return (response.body() ?: emptyList()).map { it.toDomain() }
        }
        throw Exception("No se pudieron cargar los mensajes (HTTP ${response.code()})")
    }

    override suspend fun sendMessage(conversationId: Int, senderId: Int, content: String): ChatMessage {
        val response = api.sendMessage(conversationId, SendMessageRequest(senderId = senderId, content = content))
        if (response.isSuccessful) {
            return (response.body() ?: throw Exception("Respuesta vacía al enviar mensaje")).toDomain()
        }
        throw Exception("No se pudo enviar el mensaje (HTTP ${response.code()})")
    }

    override suspend fun getVehicleRating(vehicleId: Int): VehicleRating? {
        val response = api.getVehicleRating(vehicleId)
        if (response.isSuccessful) {
            val body = response.body() ?: return null
            return VehicleRating(vehicleId = body.vehicleId, average = body.average, count = body.count)
        }
        return null
    }

    override suspend fun getVehicleReviews(vehicleId: Int): List<VehicleReview> {
        val response = api.getVehicleReviews(vehicleId)
        if (response.isSuccessful) {
            return (response.body() ?: emptyList()).map {
                VehicleReview(id = it.id, reviewerId = it.reviewerId, rating = it.rating, comment = it.comment, createdAt = it.createdAt)
            }
        }
        return emptyList()
    }

    override suspend fun openDispute(reservationId: Int, reporterId: Int, reason: String): TrustReport {
        val response = api.openDispute(reservationId, OpenDisputeRequest(reporterId = reporterId, reason = reason))
        if (response.isSuccessful) {
            return (response.body() ?: throw Exception("Respuesta vacía al reportar la reserva")).toDomain()
        }
        throw Exception("No se pudo enviar el reporte (HTTP ${response.code()})")
    }

    override suspend fun getUserDisputes(userId: Int): List<TrustReport> {
        val response = api.getUserDisputes(userId)
        if (response.isSuccessful) {
            return (response.body() ?: emptyList()).map { it.toDomain() }
        }
        throw Exception("No se pudieron cargar tus reportes (HTTP ${response.code()})")
    }

    override suspend fun submitReview(
        reservationId: Int,
        vehicleId: Int,
        reviewerId: Int,
        reviewedUserId: Int?,
        category: ReviewCategory,
        rating: Int,
        comment: String?
    ): SubmittedReview {
        val response = api.submitReview(
            SubmitReviewRequest(
                reservationId = reservationId,
                vehicleId = vehicleId,
                reviewerId = reviewerId,
                reviewedUserId = reviewedUserId,
                category = category.apiValue,
                rating = rating,
                comment = comment
            )
        )
        if (response.isSuccessful) {
            return (response.body() ?: throw Exception("Respuesta vacía al enviar la reseña")).toDomain()
        }
        throw Exception("No se pudo enviar la reseña (HTTP ${response.code()})")
    }

    private fun TrustReportResponse.toDomain() = TrustReport(
        id = id, reservationId = reservationId, reporterId = reporterId,
        reason = reason, status = status, createdAt = createdAt
    )

    private fun ReviewResponse.toDomain() = SubmittedReview(
        id = id, reservationId = reservationId, vehicleId = vehicleId,
        reviewerId = reviewerId, reviewedUserId = reviewedUserId, category = category,
        rating = rating, status = status, comment = comment, createdAt = createdAt
    )

    private fun ConversationResponse.toDomain() = Conversation(
        id = id, ownerId = ownerId, renterId = renterId, vehicleId = vehicleId,
        reservationId = reservationId, subject = subject, status = status,
        lastMessageAt = lastMessageAt, lastMessagePreview = lastMessagePreview
    )

    private fun MessageResponse.toDomain() = ChatMessage(
        id = id, conversationId = conversationId, senderId = senderId,
        content = content, createdAt = createdAt, readAt = readAt
    )
}
