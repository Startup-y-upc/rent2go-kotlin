package pe.edu.upc.rent2go_kotlin.booking.data

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@Serializable
data class AddFavoriteRequest(val userId: Int, val vehicleId: Int)

@Serializable
data class FavoriteResponse(val id: Int, val userId: Int, val vehicleId: Int, val createdAt: String? = null)

@Serializable
data class PagedFavoritesResponse(
    val content: List<FavoriteResponse> = emptyList(),
    val page: Int = 1,
    val size: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0
)

/** FavoritesController exacto del backend — /api/v1/favorites. */
interface FavoritesApi {
    @POST("api/v1/favorites")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<FavoriteResponse>

    @DELETE("api/v1/favorites")
    suspend fun removeFavorite(@Query("userId") userId: Int, @Query("vehicleId") vehicleId: Int): Response<Unit>

    @GET("api/v1/favorites")
    suspend fun listFavorites(
        @Query("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): Response<PagedFavoritesResponse>
}
