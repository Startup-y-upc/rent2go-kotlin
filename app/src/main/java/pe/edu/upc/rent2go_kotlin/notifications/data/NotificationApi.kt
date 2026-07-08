package pe.edu.upc.rent2go_kotlin.notifications.data

import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("api/v1/notifications/users/{userId}")
    suspend fun getNotifications(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): NotificationPagedResponse

    @PATCH("api/v1/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Int,
        @Query("userId") userId: Int
    ): NotificationResponse
}
