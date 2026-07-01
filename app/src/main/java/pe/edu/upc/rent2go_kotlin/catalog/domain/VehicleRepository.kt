package pe.edu.upc.rent2go_kotlin.catalog.domain

import pe.edu.upc.rent2go_kotlin.catalog.data.VehicleResponse

/**
 * Filters accepted by [VehicleRepository.getVehicles], mirrored 1:1 from the
 * query params already supported by the backend's `GET /api/v1/vehicles`
 * (see `VehicleController.searchAvailableVehicles`). All fields are optional;
 * a null/blank field means "no filter on this criterion".
 */
data class VehicleFilters(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val seats: Int? = null,
    val transmission: String? = null,
    val fuelType: String? = null
) {
    val isEmpty: Boolean
        get() = minPrice == null && maxPrice == null && seats == null &&
            transmission == null && fuelType == null
}

interface VehicleRepository {
    suspend fun getVehicles(
        page: Int = 0,
        size: Int = 20,
        filters: VehicleFilters = VehicleFilters()
    ): VehicleResponse
    suspend fun getVehicleById(id: Int): Vehicle
}
