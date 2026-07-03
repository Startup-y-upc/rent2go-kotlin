package pe.edu.upc.rent2go_kotlin.booking.domain

interface PaymentsRepository {
    suspend fun getCoveragePlans(): List<CoveragePlan>
}
