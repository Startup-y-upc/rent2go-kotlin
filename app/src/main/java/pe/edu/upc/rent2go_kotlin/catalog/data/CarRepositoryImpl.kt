package pe.edu.upc.rent2go_kotlin.catalog.data

import pe.edu.upc.rent2go_kotlin.catalog.domain.Car
import pe.edu.upc.rent2go_kotlin.catalog.domain.CarRepository

class CarRepositoryImpl(
    private val api: Rent2GoApi
) : CarRepository {
    override suspend fun getCars(): List<Car> {
        return api.getCars().map { it.toCar() }
    }

    override suspend fun getCarById(id: Int): Car {
        return api.getCarById(id).toCar()
    }
}
