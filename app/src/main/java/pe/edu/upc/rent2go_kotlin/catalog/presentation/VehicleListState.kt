package pe.edu.upc.rent2go_kotlin.catalog.presentation

import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle

data class VehicleListState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val hasMorePages: Boolean = true
)
