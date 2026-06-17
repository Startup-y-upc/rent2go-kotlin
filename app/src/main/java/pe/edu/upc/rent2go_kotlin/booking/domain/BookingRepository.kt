package pe.edu.upc.rent2go_kotlin.booking.domain

import pe.edu.upc.rent2go_kotlin.booking.data.BookingResponse
import pe.edu.upc.rent2go_kotlin.booking.data.CreateBookingRequest

interface BookingRepository {
    suspend fun createBooking(request: CreateBookingRequest): Booking
    suspend fun getBookingsByRenter(renterId: Int, status: String? = null, page: Int = 1): BookingResponse
    suspend fun cancelBooking(bookingId: Int, renterId: Int, reason: String): Booking
}
