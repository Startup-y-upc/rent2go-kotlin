package pe.edu.upc.rent2go_kotlin.community.data

import kotlinx.coroutines.delay
import pe.edu.upc.rent2go_kotlin.community.domain.ChatMessage
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import pe.edu.upc.rent2go_kotlin.community.domain.Conversation
import pe.edu.upc.rent2go_kotlin.community.domain.ReviewCategory
import pe.edu.upc.rent2go_kotlin.community.domain.SubmittedReview
import pe.edu.upc.rent2go_kotlin.community.domain.TrustReport
import pe.edu.upc.rent2go_kotlin.community.domain.UserReputation
import pe.edu.upc.rent2go_kotlin.community.domain.VehicleRating
import pe.edu.upc.rent2go_kotlin.community.domain.VehicleReview

// K9 (docs/analysis/sprint2-mobile-mock-data-audit.md): confirmado como código
// muerto — DependencyProvider.kt solo instancia CommunityRepositoryImpl (real).
// Se mantiene compilable para no romper el build, pero se recomienda mover a
// test sources o eliminar en una futura limpieza (ver 03-backlog-ado.md).
class MockCommunityRepositoryImpl : CommunityRepository {
    override suspend fun getUserReputation(userId: Int): UserReputation {
        delay(500)
        return UserReputation(completedTrips = 18, averageRating = 4.92, acceptanceRate = 100.0)
    }

    override suspend fun getConversations(userId: Int): List<Conversation> {
        delay(300)
        return emptyList()
    }

    override suspend fun getConversation(conversationId: Int): Conversation? {
        delay(300)
        return null
    }

    override suspend fun startConversation(ownerId: Int, renterId: Int, vehicleId: Int, reservationId: Int?): Conversation {
        delay(300)
        return Conversation(
            id = 0, ownerId = ownerId, renterId = renterId, vehicleId = vehicleId,
            reservationId = reservationId, subject = null, status = "OPEN",
            lastMessageAt = null, lastMessagePreview = null
        )
    }

    override suspend fun getMessages(conversationId: Int): List<ChatMessage> {
        delay(300)
        return emptyList()
    }

    override suspend fun sendMessage(conversationId: Int, senderId: Int, content: String): ChatMessage {
        delay(300)
        return ChatMessage(id = 0, conversationId = conversationId, senderId = senderId, content = content, createdAt = null, readAt = null)
    }

    override suspend fun getVehicleRating(vehicleId: Int): VehicleRating? {
        delay(300)
        return null
    }

    override suspend fun getVehicleReviews(vehicleId: Int): List<VehicleReview> {
        delay(300)
        return emptyList()
    }

    override suspend fun openDispute(reservationId: Int, reporterId: Int, reason: String): TrustReport {
        delay(300)
        return TrustReport(id = 0, reservationId = reservationId, reporterId = reporterId, reason = reason, status = "OPEN", createdAt = null)
    }

    override suspend fun getUserDisputes(userId: Int): List<TrustReport> {
        delay(300)
        return emptyList()
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
        delay(300)
        return SubmittedReview(
            id = 0, reservationId = reservationId, vehicleId = vehicleId, reviewerId = reviewerId,
            reviewedUserId = reviewedUserId, category = category.apiValue, rating = rating,
            status = "PENDING", comment = comment, createdAt = null
        )
    }
}
