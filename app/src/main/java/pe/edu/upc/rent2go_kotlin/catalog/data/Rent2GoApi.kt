package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.common.CounterpartyDto
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
        @Query("fuelType") fuelType: String? = null,
        // TS19 — geo-radius search, same backend params Flutter uses (VehicleController.java).
        @Query("centerLatitude") centerLatitude: Double? = null,
        @Query("centerLongitude") centerLongitude: Double? = null,
        @Query("radiusKm") radiusKm: Double? = null
    ): VehicleResponse

    @GET("api/v1/vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Int): VehicleDto

    // US76 closure (Sprint 5 fixes remaining scope): pre-booking owner identity/verification
    // summary. Reuses CounterpartyDto (already shared with booking/community endpoints) since
    // the response shape is identical (CounterpartyResource.java on the backend).
    @GET("api/v1/vehicles/{id}/owner-summary")
    suspend fun getVehicleOwnerSummary(@Path("id") id: Int): CounterpartyDto
}
