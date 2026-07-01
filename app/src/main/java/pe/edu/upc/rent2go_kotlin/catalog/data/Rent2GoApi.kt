package pe.edu.upc.rent2go_kotlin.catalog.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Rent2GoApi {
    @GET("api/v1/vehicles")
    suspend fun getVehicles(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("seats") seats: Int? = null,
        @Query("transmission") transmission: String? = null,
        @Query("fuelType") fuelType: String? = null
    ): VehicleResponse

    @GET("api/v1/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Int): VehicleDto
}
