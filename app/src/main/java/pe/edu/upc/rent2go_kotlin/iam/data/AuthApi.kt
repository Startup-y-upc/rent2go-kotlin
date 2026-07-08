package pe.edu.upc.rent2go_kotlin.iam.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/v1/auth/me")
    suspend fun getMe(): Response<MeResponse>

    // US09 — edit own profile (name/phone/photo). Mirrors backend's
    // PATCH /api/v1/auth/me (multipart, all fields optional) in UserController.
    @Multipart
    @PATCH("api/v1/auth/me")
    suspend fun updateProfile(
        @Part("fullName") fullName: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part profileImage: MultipartBody.Part?
    ): Response<MeResponse>

    // Resend endpoint mirrors /auth/me's Bearer-token resolution — the
    // interceptor already attaches the Authorization header for authenticated
    // calls, so no explicit userId/email parameter is needed here.
    @POST("api/v1/auth/verify/resend")
    suspend fun resendVerification(): Response<Unit>

    // Fix 2 — submits the token the user received by email/pasted into the
    // profile screen's verification dialog. Backend returns 200 on success,
    // 400 (IllegalArgumentException -> GlobalExceptionHandler) on an
    // invalid/expired token.
    @POST("api/v1/auth/verify")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<Unit>

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
