package pe.edu.upc.rent2go_kotlin.community.domain

interface CommunityRepository {
    suspend fun getUserReputation(userId: Int): UserReputation

    suspend fun getConversations(userId: Int): List<Conversation>
    suspend fun getConversation(conversationId: Int): Conversation?
    suspend fun startConversation(ownerId: Int, renterId: Int, vehicleId: Int, reservationId: Int?): Conversation
    suspend fun getMessages(conversationId: Int): List<ChatMessage>
    suspend fun sendMessage(conversationId: Int, senderId: Int, content: String): ChatMessage

    suspend fun getVehicleRating(vehicleId: Int): VehicleRating?
    suspend fun getVehicleReviews(vehicleId: Int): List<VehicleReview>
}
