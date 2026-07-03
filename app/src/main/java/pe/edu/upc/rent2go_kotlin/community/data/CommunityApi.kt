package pe.edu.upc.rent2go_kotlin.community.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CommunityApi {
    @GET("api/v1/community-trust/users/{userId}/reputation")
    suspend fun getUserReputation(@Path("userId") userId: Int): Response<UserReputationResponse>

    @GET("api/v1/community-trust/users/{userId}/conversations")
    suspend fun getConversations(@Path("userId") userId: Int): Response<List<ConversationResponse>>

    @GET("api/v1/community-trust/conversations/{conversationId}")
    suspend fun getConversation(@Path("conversationId") conversationId: Int): Response<ConversationResponse>

    @POST("api/v1/community-trust/conversations")
    suspend fun startConversation(@Body request: StartConversationRequest): Response<ConversationResponse>

    @GET("api/v1/community-trust/conversations/{conversationId}/messages")
    suspend fun getMessages(@Path("conversationId") conversationId: Int): Response<List<MessageResponse>>

    @POST("api/v1/community-trust/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: Int,
        @Body request: SendMessageRequest
    ): Response<MessageResponse>

    @GET("api/v1/community-trust/vehicles/{vehicleId}/rating")
    suspend fun getVehicleRating(@Path("vehicleId") vehicleId: Int): Response<VehicleRatingResponse>

    @GET("api/v1/community-trust/reviews/vehicle/{vehicleId}")
    suspend fun getVehicleReviews(@Path("vehicleId") vehicleId: Int): Response<List<ReviewResponse>>
}
