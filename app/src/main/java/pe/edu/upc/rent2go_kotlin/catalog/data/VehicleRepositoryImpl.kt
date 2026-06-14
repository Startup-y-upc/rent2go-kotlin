package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository

class VehicleRepositoryImpl(
    private val api: Rent2GoApi
) : VehicleRepository {

    override suspend fun getVehicles(page: Int, size: Int): VehicleResponse {
        return api.getVehicles(page = page, size = size)
    }

    override suspend fun getVehicleById(id: Int): Vehicle {
        val dto = api.getVehicleById(id)
        return dto.toDomain()
    }
}
