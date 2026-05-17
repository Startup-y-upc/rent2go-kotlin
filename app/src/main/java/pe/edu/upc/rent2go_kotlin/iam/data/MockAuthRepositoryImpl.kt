package pe.edu.upc.rent2go_kotlin.iam.data

import kotlinx.coroutines.delay
import pe.edu.upc.rent2go_kotlin.iam.domain.User
import pe.edu.upc.rent2go_kotlin.iam.domain.AuthRepository

class MockAuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): User {
        delay(1000) // Simulate network delay
        return User(1, "Usuario de Prueba", email, "999888777", "RENTER")
    }

    override suspend fun register(fullName: String, email: String, phone: String, password: String): User {
        delay(1000)
        return User(2, fullName, email, phone, "RENTER")
    }
}
