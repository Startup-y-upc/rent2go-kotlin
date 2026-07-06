package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleFilters
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import pe.edu.upc.rent2go_kotlin.common.Counterparty
import pe.edu.upc.rent2go_kotlin.common.toDomain
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
            fuelType = filters.fuelType,
            centerLatitude = filters.centerLatitude,
            centerLongitude = filters.centerLongitude,
            radiusKm = filters.radiusKm
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

    // US76 closure (Sprint 5 fixes remaining scope): fail-open by design — a 404/network
    // error resolves to null rather than propagating, mirroring loadFavoriteState/
    // loadRatingAndReviews's existing non-fatal error handling in VehicleDetailViewModel.
    override suspend fun getVehicleOwnerSummary(id: Int): Counterparty? {
        return try {
            api.getVehicleOwnerSummary(id).toDomain(fallbackId = -1, fallbackLabel = "Propietario")
        } catch (e: Exception) {
            null
        }
    }
}
