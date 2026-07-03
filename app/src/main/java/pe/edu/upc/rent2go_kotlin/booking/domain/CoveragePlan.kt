package pe.edu.upc.rent2go_kotlin.booking.domain

data class CoveragePlan(
    val code: String,
    val name: String,
    val description: String,
    val dailyRateUsd: Double
)
