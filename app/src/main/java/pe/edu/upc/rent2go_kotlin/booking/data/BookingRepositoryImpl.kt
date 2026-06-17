package pe.edu.upc.rent2go_kotlin.booking.data

import pe.edu.upc.rent2go_kotlin.booking.domain.Booking
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository

class BookingRepositoryImpl(
    private val api: BookingApi
) : BookingRepository {

    override suspend fun createBooking(request: CreateBookingRequest): Booking {
        return api.createReservation(request).toDomain()
    }

    override suspend fun getBookingsByRenter(renterId: Int, status: String?, page: Int): BookingResponse {
        return api.getReservationsByRenter(renterId = renterId, status = status, page = page)
    }

    override suspend fun cancelBooking(bookingId: Int, renterId: Int, reason: String): Booking {
        return api.cancelReservation(bookingId, CancelBookingRequest(renterId, reason)).toDomain()
    }
}
