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

    /**
     * US09 — Editar perfil propio. All parameters optional; only non-null
     * ones are sent/changed, mirroring backend's PATCH /api/v1/auth/me.
     */
    suspend fun updateProfile(fullName: String?, phone: String?): User

    /**
     * Issues a new email verification token for the authenticated user and
     * re-sends the verification email (best-effort on the backend side).
     * Mirrors POST /api/v1/auth/verify/resend.
     */
    suspend fun resendVerificationEmail(): Boolean

    /**
     * Fix 2 — submits the userId + pasted token to POST /api/v1/auth/verify.
     * Returns true on success (200), false on 400 (invalid/expired token per
     * GlobalExceptionHandler's IllegalArgumentException mapping).
     */
    suspend fun verifyEmail(userId: Int, token: String): Boolean
}

