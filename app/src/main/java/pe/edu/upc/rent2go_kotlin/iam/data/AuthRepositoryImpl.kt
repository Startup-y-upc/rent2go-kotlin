package pe.edu.upc.rent2go_kotlin.iam.data

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
                // Aquí guardaríamos el token en un gestor local si fuera necesario.
                // body.token contiene el token JWT recibido.
                return User(
                    id = body.userId,
                    fullName = body.fullName,
                    email = body.email,
                    phone = body.phone,
                    role = body.accountType
                )
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Credenciales incorrectas"
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Error desconocido al iniciar sesión")
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
                return User(
                    id = body.id,
                    fullName = body.fullName,
                    email = body.email,
                    phone = body.phone,
                    role = body.accountType
                )
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
