package pe.edu.upc.rent2go_kotlin.catalog.presentation

import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleFilters

data class VehicleListState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val hasMorePages: Boolean = false,
    // US26/US27 — free-text search (client-side, matches make/model/location)
    // and structured filters (server-side, forwarded as query params).
    val searchQuery: String = "",
    val filters: VehicleFilters = VehicleFilters()
)
