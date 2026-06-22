package pe.edu.upc.rent2go_kotlin.iam.data

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import pe.edu.upc.rent2go_kotlin.iam.domain.AuthRepository
import pe.edu.upc.rent2go_kotlin.iam.domain.User

class AuthRepositoryImpl(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun login(email: String, password: String): User {
        try {
            val response = api.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Respuesta del servidor vacía")
                val user = User(
                    id = body.userId,
                    fullName = body.fullName,
                    email = body.email,
                    phone = body.phone,
                    role = body.accountType,
                    username = body.username,
                    profileImageUrl = body.profileImageUrl,
                    status = body.status,
                    emailVerified = body.emailVerified,
                    phoneVerified = body.phoneVerified,
                    twoFactorEnabled = body.twoFactorEnabled
                )
                pe.edu.upc.rent2go_kotlin.common.SessionManager.saveSession(body.token, user)
                return user
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Credenciales incorrectas"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error desconocido al iniciar sesión")
        }
    }

    override suspend fun getMe(): User {
        try {
            val response = api.getMe()
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Respuesta del servidor vacía")
                val user = User(
                    id = body.id,
                    fullName = body.fullName,
                    email = body.email,
                    phone = body.phone,
                    role = body.accountType,
                    username = body.username,
                    profileImageUrl = body.profileImageUrl,
                    status = body.status,
                    emailVerified = body.emailVerified,
                    phoneVerified = body.phoneVerified,
                    twoFactorEnabled = body.twoFactorEnabled
                )
                // Actualizar los datos en SessionManager (el token ya está guardado)
                pe.edu.upc.rent2go_kotlin.common.SessionManager.updateUser(user)
                return user
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Sesión expirada"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error al obtener los datos del usuario")
        }
    }

    override suspend fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        accountType: String
    ): User {
        try {
            val request = RegisterRequest(
                email = email,
                password = password,
                username = username,
                fullName = fullName,
                phone = phone,
                profileImageUrl = "", // URL vacía por defecto
                accountType = accountType
            )
            val response = api.register(request)
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Respuesta del servidor vacía")
                val user = User(
                    id = body.id,
                    fullName = body.fullName,
                    email = body.email,
                    phone = body.phone,
                    role = body.accountType,
                    username = body.username,
                    profileImageUrl = body.profileImageUrl,
                    status = body.status,
                    emailVerified = body.emailVerified,
                    phoneVerified = body.phoneVerified,
                    twoFactorEnabled = body.twoFactorEnabled
                )
                // Si el backend devuelve un token en el registro, guardar la sesión
                if (!body.token.isNullOrBlank()) {
                    pe.edu.upc.rent2go_kotlin.common.SessionManager.saveSession(body.token, user)
                }
                return user
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al registrar el usuario"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error desconocido al registrar")
        }
    }

    override suspend fun submitKyc(
        userId: Int,
        fullName: String,
        idNumber: String,
        dniFrontUrl: String,
        dniBackUrl: String,
        driverLicenseUrl: String
    ): Boolean {
        try {
            val request = SubmitKycRequest(
                userId = userId,
                fullName = fullName,
                idNumber = idNumber,
                dniFrontUrl = dniFrontUrl,
                dniBackUrl = dniBackUrl,
                driverLicenseUrl = driverLicenseUrl
            )
            val response = api.submitKyc(request)
            if (response.isSuccessful) {
                return true
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al enviar validación KYC"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error desconocido al enviar validación")
        }
    }

    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): String {
        try {
            val mimeType = when {
                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
                else -> "image/*"
            }
            val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = api.uploadImage(part)
            if (response.isSuccessful) {
                return response.body()?.imageUrl ?: throw Exception("URL de imagen no recibida")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al subir la imagen"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error desconocido al subir la imagen")
        }
    }

    override suspend fun requestPasswordReset(email: String): Boolean {
        try {
            val response = api.requestPasswordReset(PasswordResetRequest(email = email))
            if (response.isSuccessful) {
                return true
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al solicitar restablecimiento de contraseña"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error al solicitar código")
        }
    }

    override suspend fun confirmPasswordReset(token: String, newPassword: String): Boolean {
        try {
            val response = api.confirmPasswordReset(PasswordResetConfirm(token = token, newPassword = newPassword))
            if (response.isSuccessful) {
                return true
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al restablecer la contraseña"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error al restablecer la contraseña")
        }
    }
}
