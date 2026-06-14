package pe.edu.upc.rent2go_kotlin.catalog.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Rent2GoApi {
    @GET("api/v1/vehicles")
    suspend fun getVehicles(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): VehicleResponse

    @GET("api/v1/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Int): VehicleDto
}
