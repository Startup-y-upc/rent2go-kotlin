package pe.edu.upc.rent2go_kotlin.common

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.edu.upc.rent2go_kotlin.catalog.data.Rent2GoApi
import pe.edu.upc.rent2go_kotlin.catalog.data.MockCarRepositoryImpl
import pe.edu.upc.rent2go_kotlin.catalog.domain.CarRepository
import pe.edu.upc.rent2go_kotlin.catalog.domain.GetCarsUseCase
import pe.edu.upc.rent2go_kotlin.iam.data.AuthApi
import pe.edu.upc.rent2go_kotlin.iam.data.AuthRepositoryImpl
import pe.edu.upc.rent2go_kotlin.iam.domain.AuthRepository
import pe.edu.upc.rent2go_kotlin.community.data.CommunityApi
import pe.edu.upc.rent2go_kotlin.community.data.CommunityRepositoryImpl
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object DependencyProvider {
    private val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = SessionManager.getToken()
            if (token != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // API Services
    private val api: Rent2GoApi = retrofit.create(Rent2GoApi::class.java)
    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    private val communityApi: CommunityApi = retrofit.create(CommunityApi::class.java)

    // Repositories
    private val carRepository: CarRepository = MockCarRepositoryImpl()
    val authRepository: AuthRepository = AuthRepositoryImpl(authApi)
    val communityRepository: CommunityRepository = CommunityRepositoryImpl(communityApi)

    // Use Cases
    val getCarsUseCase: GetCarsUseCase = GetCarsUseCase(carRepository)
}
