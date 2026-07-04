package pe.edu.upc.rent2go_kotlin.booking.data

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Item exacto devuelto por GET /api/v1/payments/coverage-plans:
 * códigos reales BASIC/STANDARD/PREMIUM/NONE a $5/$12/$20/día. */
@Serializable
data class CoveragePlanResponse(
    val code: String,
    val name: String,
    val description: String? = null,
    val dailyRateUSD: Double
)

/** Cuerpo exacto de POST /api/v1/payments/create-intent (CreateIntentRequest del backend).
 * currency NO tiene valor por defecto: kotlinx.serialization omite del JSON los campos
 * que quedan en su valor por defecto (salvo encodeDefaults=true en el Json compartido),
 * por lo que un default aquí desaparecería silenciosamente del body y el backend
 * (@NotBlank) lo rechazaría como null con "Currency is required" — bug reportado y
 * rastreado exactamente a este comportamiento. */
@Serializable
data class CreateIntentRequest(
    val reservationId: Int,
    val amountCents: Int,
    val currency: String
)

/** Respuesta exacta de POST /api/v1/payments/create-intent (CreateIntentResponse del backend):
 * clientSecret se usa para confirmar el cobro con el SDK de Stripe (PaymentSheet), US58/TS16. */
@Serializable
data class CreateIntentResponse(
    val clientSecret: String,
    val id: String
)

interface PaymentsApi {
    @GET("api/v1/payments/coverage-plans")
    suspend fun getCoveragePlans(): Response<List<CoveragePlanResponse>>

    @POST("api/v1/payments/create-intent")
    suspend fun createPaymentIntent(@Body request: CreateIntentRequest): Response<CreateIntentResponse>
}
