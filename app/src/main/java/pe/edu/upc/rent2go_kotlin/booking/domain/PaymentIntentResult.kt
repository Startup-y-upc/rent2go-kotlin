package pe.edu.upc.rent2go_kotlin.booking.domain

/** US58/TS16 — resultado de crear un PaymentIntent en el backend, usado para confirmar el
 * cobro con Stripe PaymentSheet. */
data class PaymentIntentResult(
    val clientSecret: String,
    val id: String
)
