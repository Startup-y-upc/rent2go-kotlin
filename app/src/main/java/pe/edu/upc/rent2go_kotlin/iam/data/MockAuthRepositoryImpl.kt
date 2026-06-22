package pe.edu.upc.rent2go_kotlin.iam.data

import kotlinx.coroutines.delay
import pe.edu.upc.rent2go_kotlin.iam.domain.User
import pe.edu.upc.rent2go_kotlin.iam.domain.AuthRepository

class MockAuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): User {
        delay(1000) // Simulate network delay
        return User(1, "Usuario de Prueba", email, "999888777", "RENTER")
    }

    override suspend fun getMe(): User {
        delay(500) // Simulate network delay
        return User(1, "Usuario de Prueba", "usuario@example.com", "999888777", "RENTER")
    }

    override suspend fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        accountType: String
    ): User {
        delay(1000)
        return User(2, fullName, email, phone, accountType)
    }

    override suspend fun submitKyc(
        userId: Int,
        fullName: String,
        idNumber: String,
        dniFrontUrl: String,
        dniBackUrl: String,
        driverLicenseUrl: String
    ): Boolean {
        delay(1000)
        return true
    }

    override suspend fun requestPasswordReset(email: String): Boolean {
        delay(1000)
        return true
    }

    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): String {
        delay(500)
        return "https://rent2go-uploads.s3.amazonaws.com/mock_${fileName}"
    }

    override suspend fun confirmPasswordReset(token: String, newPassword: String): Boolean {
        delay(1000)
        return true
    }
}

