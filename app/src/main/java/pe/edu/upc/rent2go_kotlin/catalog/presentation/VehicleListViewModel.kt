package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.catalog.data.toDomain
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleFilters
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository

/**
 * US25/US26/US27 — Explorar/buscar/filtrar catálogo (Renter, Kotlin-only).
 *
 * Search (US26) is applied client-side over the currently loaded pages,
 * matching make/model/location, because the backend's `location` query
 * param is an exact case-insensitive match (see
 * `VehicleQueryServiceImpl.findVehicles`), not a free-text search — a
 * substring match on the client gives the AC'd "coincide con marca, modelo
 * o ubicación" behavior without requiring a backend change.
 *
 * Filters (US27: price, seats, transmission, fuelType) are forwarded as
 * real query params to `GET /api/v1/vehicles`, which already supports them
 * server-side (confirmed in `VehicleController.searchAvailableVehicles`).
 */
class VehicleListViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _state = mutableStateOf(VehicleListState())
    val state: State<VehicleListState> = _state

    private var rawVehicles = mutableListOf<Vehicle>()

    fun loadFirstPage() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            try {
                val response = repository.getVehicles(page = 0, filters = _state.value.filters)
                rawVehicles = response.content.map { it.toDomain() }.toMutableList()
                _state.value = _state.value.copy(
                    vehicles = applySearch(rawVehicles, _state.value.searchQuery),
                    isLoading = false,
                    currentPage = response.page,
                    totalPages = response.totalPages,
                    hasMorePages = response.page < response.totalPages - 1
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar los vehículos"
                )
            }
        }
    }

    fun loadNextPage() {
        val currentState = _state.value
        if (!currentState.hasMorePages || currentState.isLoadingMore) return

        val nextPage = currentState.currentPage + 1
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMore = true)
            try {
                val response = repository.getVehicles(
                    page = nextPage,
                    filters = _state.value.filters
                )
                val newVehicles = response.content.map { it.toDomain() }
                rawVehicles.addAll(newVehicles)
                _state.value = _state.value.copy(
                    vehicles = applySearch(rawVehicles, _state.value.searchQuery),
                    isLoadingMore = false,
                    currentPage = response.page,
                    totalPages = response.totalPages,
                    hasMorePages = response.page < response.totalPages - 1
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Error al cargar más vehículos"
                )
            }
        }
    }

    /** US26 — updates the search box text and re-filters the already-loaded vehicles. */
    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            vehicles = applySearch(rawVehicles, query)
        )
    }

    /** US27 — applies structured filters and reloads from page 0 (server-side). */
    fun applyFilters(filters: VehicleFilters) {
        _state.value = _state.value.copy(filters = filters)
        loadFirstPage()
    }

    fun clearFilters() {
        _state.value = _state.value.copy(filters = VehicleFilters())
        loadFirstPage()
    }

    private fun applySearch(vehicles: List<Vehicle>, query: String): List<Vehicle> {
        if (query.isBlank()) return vehicles
        val needle = query.trim()
        return vehicles.filter { vehicle ->
            vehicle.make.contains(needle, ignoreCase = true) ||
                vehicle.model.contains(needle, ignoreCase = true) ||
                vehicle.location.contains(needle, ignoreCase = true)
        }
    }
}
