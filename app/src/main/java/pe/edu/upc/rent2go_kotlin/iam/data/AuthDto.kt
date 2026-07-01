package pe.edu.upc.rent2go_kotlin.iam.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: Int,
    val email: String,
    val username: String? = null,
    val fullName: String,
    val phone: String,
    val accountType: String,
    val status: String,
    val emailVerified: Boolean,
    val phoneVerified: Boolean,
    val twoFactorEnabled: Boolean,
    val profileImageUrl: String? = null
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val fullName: String,
    val phone: String,
    val profileImageUrl: String,
    val accountType: String
)

@Serializable
data class RegisterResponse(
    val id: Int,
    val email: String,
    val username: String? = null,
    @SerialName("full_name") val fullName: String,
    val phone: String,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    @SerialName("account_type") val accountType: String,
    val status: String,
    @SerialName("email_verified") val emailVerified: Boolean,
    @SerialName("phone_verified") val phoneVerified: Boolean,
    @SerialName("two_factor_enabled") val twoFactorEnabled: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val token: String? = null // token may be returned on register
)

@Serializable
data class MeResponse(
    val id: Int,
    val email: String,
    val username: String? = null,
    @SerialName("full_name") val fullName: String,
    val phone: String,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    @SerialName("account_type") val accountType: String,
    val status: String,
    @SerialName("email_verified") val emailVerified: Boolean,
    @SerialName("phone_verified") val phoneVerified: Boolean,
    @SerialName("two_factor_enabled") val twoFactorEnabled: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class SubmitKycRequest(
    val userId: Int,
    val fullName: String,
    val idNumber: String,
    val dniFrontUrl: String,
    val dniBackUrl: String,
    val driverLicenseUrl: String
)

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class PasswordResetConfirm(
    val token: String,
    val newPassword: String
)

@Serializable
data class ImageUploadResponse(
    val imageUrl: String,
    val imagePath: String? = null
)

