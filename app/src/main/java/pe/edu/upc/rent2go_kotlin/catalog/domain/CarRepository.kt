package pe.edu.upc.rent2go_kotlin.catalog.domain

interface CarRepository {
    suspend fun getCars(): List<Car>
    suspend fun getCarById(id: Int): Car
}
