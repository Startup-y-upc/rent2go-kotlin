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

    /**
     * Bugfix (US58 follow-up): forces the backend to re-check [reservationId]'s PaymentIntent
     * against Stripe and apply the confirm/mark-paid transition if it already succeeded. Must be
     * called after a successful PaymentSheet confirmation and before re-reading the reservation,
     * because Stripe's webhook can lag behind the client-side confirmation and leave the
     * reservation observed as PENDING even though the charge succeeded. Failures are non-fatal —
     * the webhook remains the source of truth and will eventually apply the same transition.
     */
    suspend fun syncPayment(reservationId: Int)
}
