package pe.edu.upc.rent2go_kotlin.booking.data

import android.util.Log
import pe.edu.upc.rent2go_kotlin.booking.domain.CoveragePlan
import pe.edu.upc.rent2go_kotlin.booking.domain.PaymentIntentResult
import pe.edu.upc.rent2go_kotlin.booking.domain.PaymentsRepository

class PaymentsRepositoryImpl(
    private val api: PaymentsApi
) : PaymentsRepository {
    override suspend fun getCoveragePlans(): List<CoveragePlan> {
        val response = api.getCoveragePlans()
        if (response.isSuccessful) {
            return (response.body() ?: emptyList()).map {
                CoveragePlan(code = it.code, name = it.name, description = it.description ?: "", dailyRateUsd = it.dailyRateUSD)
            }
        }
        throw Exception("No se pudieron cargar los planes de cobertura (HTTP ${response.code()})")
    }

    /**
     * Issue 1 fix (renter-reported HTTP 422 on create-intent): the backend's CreateIntentRequest
     * requires reservationId > 0, amountCents > 0, and a non-blank currency (@Positive/@NotBlank,
     * mapped to 422 "Unprocessable Entity" by GlobalExceptionHandler on any Bean Validation
     * failure). A reservationId or amountCents of 0 (e.g. a not-yet-created booking id, or a
     * $0.00 fare edge case) is the one realistic value that reproduces this 422 client-side —
     * guarded here BEFORE the network call so the failure is immediate and diagnosable instead
     * of a generic "HTTP 422" surfaced after a round-trip.
     *
     * Previously this method only threw "No se pudo iniciar el cobro (HTTP $code)" on failure,
     * discarding the response body entirely — any real validation error detail from the backend
     * (field name + message, per GlobalExceptionHandler's validationBody()) was silently lost,
     * making a genuine field-level bug impossible to diagnose from the client's error message.
     * Now the raw error body is read and appended so future 4xx/5xx failures are diagnosable.
     */
    override suspend fun createPaymentIntent(reservationId: Int, amountCents: Int, currency: String): PaymentIntentResult {
        require(reservationId > 0) { "ID de reserva inválido ($reservationId): no se puede iniciar el cobro." }
        require(amountCents > 0) { "Monto a cobrar inválido ($amountCents centavos): no se puede iniciar el cobro." }
        require(currency.isNotBlank()) { "Moneda inválida: no se puede iniciar el cobro." }

        val response = api.createPaymentIntent(
            CreateIntentRequest(reservationId = reservationId, amountCents = amountCents, currency = currency)
        )
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Respuesta vacía al iniciar el cobro")
            return PaymentIntentResult(clientSecret = body.clientSecret, id = body.id)
        }

        val errorDetail = response.errorBody()?.string()
        Log.e(
            "PaymentsRepository",
            "createPaymentIntent failed: HTTP ${response.code()} for reservationId=$reservationId, " +
                "amountCents=$amountCents, currency=$currency. Body: $errorDetail"
        )
        val detailSuffix = if (!errorDetail.isNullOrBlank()) ": $errorDetail" else ""
        throw Exception("No se pudo iniciar el cobro (HTTP ${response.code()})$detailSuffix")
    }

    /**
     * Bugfix (US58 follow-up): non-fatal by design. This is a defensive fallback for a webhook
     * race, not the source of truth — if it fails (network blip, Stripe API hiccup) the webhook
     * will still eventually confirm the reservation on its own, so callers must not surface this
     * as a user-facing payment error.
     */
    override suspend fun syncPayment(reservationId: Int) {
        try {
            val response = api.syncPayment(reservationId)
            if (!response.isSuccessful) {
                Log.w("PaymentsRepository", "syncPayment failed: HTTP ${response.code()} for reservationId=$reservationId")
            }
        } catch (e: Exception) {
            Log.w("PaymentsRepository", "syncPayment error for reservationId=$reservationId: ${e.message}")
        }
    }
}
