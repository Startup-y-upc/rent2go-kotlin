package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.catalog.data.VehicleResponse
import pe.edu.upc.rent2go_kotlin.catalog.data.toDomain
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository

class VehicleListViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _state = mutableStateOf(VehicleListState())
    val state: State<VehicleListState> = _state

    private var currentVehicles = mutableListOf<Vehicle>()

    fun loadFirstPage() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = "")
            try {
                val response = repository.getVehicles(page = 0)
                currentVehicles = response.content.map { it.toDomain() }.toMutableList()
                _state.value = _state.value.copy(
                    vehicles = currentVehicles.toList(),
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
                val response = repository.getVehicles(page = nextPage)
                val newVehicles = response.content.map { it.toDomain() }
                currentVehicles.addAll(newVehicles)
                _state.value = _state.value.copy(
                    vehicles = currentVehicles.toList(),
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
}
