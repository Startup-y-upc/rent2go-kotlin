package pe.edu.upc.rent2go_kotlin.iam.domain

interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun getMe(): User
    suspend fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        accountType: String
    ): User
    suspend fun submitKyc(
        userId: Int,
        fullName: String,
        idNumber: String,
        dniFrontUrl: String,
        dniBackUrl: String,
        driverLicenseUrl: String
    ): Boolean
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): String
    suspend fun requestPasswordReset(email: String): Boolean
    suspend fun confirmPasswordReset(token: String, newPassword: String): Boolean
}

