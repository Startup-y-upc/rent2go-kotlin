package pe.edu.upc.rent2go_kotlin.common

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.edu.upc.rent2go_kotlin.catalog.data.Rent2GoApi
import pe.edu.upc.rent2go_kotlin.catalog.data.VehicleRepositoryImpl
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleRepository
import pe.edu.upc.rent2go_kotlin.iam.data.AuthApi
import pe.edu.upc.rent2go_kotlin.iam.data.AuthRepositoryImpl
import pe.edu.upc.rent2go_kotlin.iam.domain.AuthRepository
import pe.edu.upc.rent2go_kotlin.community.data.CommunityApi
import pe.edu.upc.rent2go_kotlin.community.data.CommunityRepositoryImpl
import pe.edu.upc.rent2go_kotlin.community.domain.CommunityRepository
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import pe.edu.upc.rent2go_kotlin.booking.data.BookingApi
import pe.edu.upc.rent2go_kotlin.booking.data.BookingRepositoryImpl
import pe.edu.upc.rent2go_kotlin.booking.domain.BookingRepository

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
    private val bookingApi: BookingApi = retrofit.create(BookingApi::class.java)

    // Repositories
    val vehicleRepository: VehicleRepository = VehicleRepositoryImpl(api)
    val authRepository: AuthRepository = AuthRepositoryImpl(authApi)
    val communityRepository: CommunityRepository = CommunityRepositoryImpl(communityApi)
    val bookingRepository: BookingRepository = BookingRepositoryImpl(bookingApi)
}
