package pe.edu.upc.rent2go_kotlin.booking.data

import pe.edu.upc.rent2go_kotlin.booking.domain.CoveragePlan
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
}
