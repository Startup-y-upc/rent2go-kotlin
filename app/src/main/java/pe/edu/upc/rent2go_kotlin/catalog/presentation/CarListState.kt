package pe.edu.upc.rent2go_kotlin.catalog.presentation

import pe.edu.upc.rent2go_kotlin.catalog.domain.Car

data class CarListState(
    val isLoading: Boolean = false,
    val cars: List<Car> = emptyList(),
    val error: String = ""
)
