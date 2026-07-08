package pe.edu.upc.rent2go_kotlin.booking.domain

import pe.edu.upc.rent2go_kotlin.booking.data.AvailabilityCheckResponse
import pe.edu.upc.rent2go_kotlin.booking.data.BookingResponse
import pe.edu.upc.rent2go_kotlin.booking.data.CreateBookingRequest

interface BookingRepository {
    suspend fun createBooking(request: CreateBookingRequest): Booking
    suspend fun getBookingsByRenter(renterId: Int, status: String? = null, page: Int = 1): BookingResponse
    suspend fun getBookingById(id: Int): Booking
    suspend fun cancelBooking(bookingId: Int, renterId: Int, reason: String): Booking

    /**
     * US15 (Renter, read-only) — checks whether [vehicleId] is available for
     * [startDate]..[endDate] (yyyy-MM-dd) and returns the blocked date ranges
     * so the booking screen can warn the renter before submitting a request.
     * Does NOT expose any block-creation capability (Owner-only, Flutter side).
     */
    suspend fun checkAvailability(vehicleId: Int, startDate: String, endDate: String): AvailabilityCheckResponse
}
