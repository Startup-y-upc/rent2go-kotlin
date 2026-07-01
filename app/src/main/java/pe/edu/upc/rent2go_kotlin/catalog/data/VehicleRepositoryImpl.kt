package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleFilters
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import java.util.concurrent.ConcurrentHashMap

class VehicleRepositoryImpl(
    private val api: Rent2GoApi
) : VehicleRepository {

    private val vehicleCache = ConcurrentHashMap<Int, Vehicle>()

    override suspend fun getVehicles(page: Int, size: Int, filters: VehicleFilters): VehicleResponse {
        val response = api.getVehicles(
            page = page,
            size = size,
            minPrice = filters.minPrice,
            maxPrice = filters.maxPrice,
            seats = filters.seats,
            transmission = filters.transmission,
            fuelType = filters.fuelType
        )
        response.content.forEach { dto ->
            vehicleCache[dto.id] = dto.toDomain()
        }
        return response
    }

    override suspend fun getVehicleById(id: Int): Vehicle {
        vehicleCache[id]?.let { return it }
        val dto = api.getVehicleById(id)
        val vehicle = dto.toDomain()
        vehicleCache[id] = vehicle
        return vehicle
    }
}
