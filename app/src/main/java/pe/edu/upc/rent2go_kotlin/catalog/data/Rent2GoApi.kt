package pe.edu.upc.rent2go_kotlin.catalog.data

import retrofit2.http.GET
import retrofit2.http.Path

interface Rent2GoApi {
    @GET("cars")
    suspend fun getCars(): List<CarDto>

    @GET("cars/{id}")
    suspend fun getCarById(@Path("id") id: Int): CarDto
}
