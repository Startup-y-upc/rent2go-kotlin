package pe.edu.upc.rent2go_kotlin.booking.data

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

/** Item exacto devuelto por GET /api/v1/payments/coverage-plans:
 * códigos reales BASIC/STANDARD/PREMIUM/NONE a $5/$12/$20/día. */
@Serializable
data class CoveragePlanResponse(
    val code: String,
    val name: String,
    val description: String? = null,
    val dailyRateUSD: Double
)

interface PaymentsApi {
    @GET("api/v1/payments/coverage-plans")
    suspend fun getCoveragePlans(): Response<List<CoveragePlanResponse>>
}
