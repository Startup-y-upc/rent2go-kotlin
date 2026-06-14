package pe.edu.upc.rent2go_kotlin.community.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CommunityApi {
    @GET("api/v1/community-trust/users/{userId}/reputation")
    suspend fun getUserReputation(@Path("userId") userId: Int): Response<UserReputationResponse>
}
