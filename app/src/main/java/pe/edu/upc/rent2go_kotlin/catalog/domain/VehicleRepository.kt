package pe.edu.upc.rent2go_kotlin.catalog.domain

import pe.edu.upc.rent2go_kotlin.catalog.data.VehicleResponse

interface VehicleRepository {
    suspend fun getVehicles(page: Int = 0, size: Int = 20): VehicleResponse
    suspend fun getVehicleById(id: Int): Vehicle
}
