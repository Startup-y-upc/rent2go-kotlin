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

    @GET("api/v1/reservations/{id}")
    suspend fun getReservationById(@Path("id") id: Int): BookingDto

    @POST("api/v1/reservations/{id}/cancel")
    suspend fun cancelReservation(
        @Path("id") id: Int,
        @Body request: CancelBookingRequest
    ): BookingDto

    // US15 (Renter, read-only) — consulta de disponibilidad antes de reservar.
    // Consumes the existing AvailabilityController; no owner-style block
    // creation is exposed here (that stays Flutter-only per platform split).
    @GET("api/v1/availability/vehicle/{vehicleId}/check")
    suspend fun checkAvailability(
        @Path("vehicleId") vehicleId: Int,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): AvailabilityCheckResponse
}
