package pe.edu.upc.rent2go_kotlin.booking.domain

interface PaymentsRepository {
    suspend fun getCoveragePlans(): List<CoveragePlan>

    /**
     * US58/TS16 — creates a real Stripe PaymentIntent in the backend for [reservationId],
     * matching CreateIntentRequest/CreateIntentResponse exactly. The returned clientSecret is
     * used to present Stripe's Android PaymentSheet — no charge exists in Stripe until the
     * PaymentSheet confirms it.
     */
    suspend fun createPaymentIntent(reservationId: Int, amountCents: Int, currency: String = "usd"): PaymentIntentResult
}
