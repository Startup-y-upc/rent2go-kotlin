package pe.edu.upc.rent2go_kotlin.booking.data

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

    override suspend fun createPaymentIntent(reservationId: Int, amountCents: Int, currency: String): PaymentIntentResult {
        val response = api.createPaymentIntent(
            CreateIntentRequest(reservationId = reservationId, amountCents = amountCents, currency = currency)
        )
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Respuesta vacía al iniciar el cobro")
            return PaymentIntentResult(clientSecret = body.clientSecret, id = body.id)
        }
        throw Exception("No se pudo iniciar el cobro (HTTP ${response.code()})")
    }
}
