package pe.edu.upc.rent2go_kotlin.iam.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(fullName: String, email: String, phone: String, password: String): User
}
