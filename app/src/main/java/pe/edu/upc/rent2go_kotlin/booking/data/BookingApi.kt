package pe.edu.upc.rent2go_kotlin.booking.data

import retrofit2.http.*

interface BookingApi {
    @POST("api/v1/reservations")
    suspend fun createReservation(@Body request: CreateBookingRequest): BookingDto

    @GET("api/v1/reservations")
    suspend fun getReservationsByRenter(
        @Query("renterId") renterId: Int,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): BookingResponse
}
