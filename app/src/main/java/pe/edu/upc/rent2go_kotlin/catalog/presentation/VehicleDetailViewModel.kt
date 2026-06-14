package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository

class VehicleDetailViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _state = mutableStateOf(VehicleDetailState())
    val state: State<VehicleDetailState> = _state

    fun loadVehicle(id: Int) {
        viewModelScope.launch {
            _state.value = VehicleDetailState(isLoading = true)
            try {
                val vehicle = repository.getVehicleById(id)
                _state.value = VehicleDetailState(vehicle = vehicle)
            } catch (e: Exception) {
                _state.value = VehicleDetailState(
                    error = e.message ?: "Error al cargar el detalle del vehículo"
                )
            }
        }
    }
}

data class VehicleDetailState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)
