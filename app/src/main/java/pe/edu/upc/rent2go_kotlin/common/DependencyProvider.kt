package pe.edu.upc.rent2go_kotlin.common

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import pe.edu.upc.rent2go_kotlin.catalog.data.Rent2GoApi
import pe.edu.upc.rent2go_kotlin.iam.data.MockAuthRepositoryImpl
import pe.edu.upc.rent2go_kotlin.catalog.data.MockCarRepositoryImpl
import pe.edu.upc.rent2go_kotlin.iam.domain.AuthRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.CarRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.GetCarsUseCase
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object DependencyProvider {
    private val json = Json { ignoreUnknownKeys = true }
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val api: Rent2GoApi = retrofit.create(Rent2GoApi::class.java)
    
    // Repositories
    private val carRepository: CarRepository = MockCarRepositoryImpl()
    val authRepository: AuthRepository = MockAuthRepositoryImpl()
    
    // Use Cases
    val getCarsUseCase: GetCarsUseCase = GetCarsUseCase(carRepository)
}
