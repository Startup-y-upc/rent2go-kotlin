package pe.edu.upc.rent2go_kotlin.iam.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/v1/auth/me")
    suspend fun getMe(): Response<MeResponse>

    @POST("api/v1/auth/kyc")
    suspend fun submitKyc(@Body request: SubmitKycRequest): Response<Unit>

    @POST("api/v1/auth/password/request")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<Unit>

    @POST("api/v1/auth/password/reset")
    suspend fun confirmPasswordReset(@Body request: PasswordResetConfirm): Response<Unit>

    @Multipart
    @POST("api/uploads/images")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ImageUploadResponse>
}
