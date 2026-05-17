package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.Resource
import pe.edu.upc.rent2go_kotlin.catalog.domain.GetCarsUseCase


class CarListViewModel(
    private val getCarsUseCase: GetCarsUseCase = DependencyProvider.getCarsUseCase
) : ViewModel() {

    private val _state = mutableStateOf(CarListState())
    val state: State<CarListState> = _state

    init {
        getCars()
    }

    private fun getCars() {
        getCarsUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = CarListState(cars = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = CarListState(error = result.message ?: "An unexpected error occurred")
                }
                is Resource.Loading -> {
                    _state.value = CarListState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}
