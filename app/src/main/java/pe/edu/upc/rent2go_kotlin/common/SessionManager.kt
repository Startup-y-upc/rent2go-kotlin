package pe.edu.upc.rent2go_kotlin.common

import android.content.Context
import android.content.SharedPreferences
import pe.edu.upc.rent2go_kotlin.iam.domain.User

object SessionManager {
    private const val PREFS_NAME = "rent2go_session_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_FULL_NAME = "user_full_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PHONE = "user_phone"
    private const val KEY_ROLE = "user_role"
    private const val KEY_USERNAME = "user_username"
    private const val KEY_PROFILE_IMAGE_URL = "user_profile_image_url"
    private const val KEY_STATUS = "user_status"
    private const val KEY_EMAIL_VERIFIED = "user_email_verified"
    private const val KEY_PHONE_VERIFIED = "user_phone_verified"
    private const val KEY_TWO_FACTOR_ENABLED = "user_two_factor_enabled"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_KYC_DNI_FRONT = "kyc_dni_front"
    private const val KEY_KYC_DNI_BACK = "kyc_dni_back"
    private const val KEY_KYC_LICENSE = "kyc_license"

    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Ya NO limpiamos la sesión al iniciar si remember_me es false.
            // En móvil, la sesión persiste por defecto hasta que el usuario cierra sesión manualmente.
            // El flag remember_me puede usarse para sesiones extendidas (ej. refresh tokens en el futuro).
        }
    }

    fun setRememberMe(rememberMe: Boolean) {
        getPrefs().edit().putBoolean(KEY_REMEMBER_ME, rememberMe).apply()
    }

    private fun getPrefs(): SharedPreferences {
        return sharedPreferences ?: throw IllegalStateException("SessionManager has not been initialized. Call initialize(context) in MainActivity.")
    }

    fun saveSession(token: String, user: User) {
        getPrefs().edit().apply {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, user.id)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_EMAIL, user.email)
            putString(KEY_PHONE, user.phone)
            putString(KEY_ROLE, user.role)
            user.username?.let { putString(KEY_USERNAME, it) }
            user.profileImageUrl?.let { putString(KEY_PROFILE_IMAGE_URL, it) }
            putString(KEY_STATUS, user.status)
            putBoolean(KEY_EMAIL_VERIFIED, user.emailVerified)
            putBoolean(KEY_PHONE_VERIFIED, user.phoneVerified)
            putBoolean(KEY_TWO_FACTOR_ENABLED, user.twoFactorEnabled)
            apply()
        }
    }

    fun updateUser(user: User) {
        getPrefs().edit().apply {
            putInt(KEY_USER_ID, user.id)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_EMAIL, user.email)
            putString(KEY_PHONE, user.phone)
            putString(KEY_ROLE, user.role)
            user.username?.let { putString(KEY_USERNAME, it) }
            user.profileImageUrl?.let { putString(KEY_PROFILE_IMAGE_URL, it) }
            putString(KEY_STATUS, user.status)
            putBoolean(KEY_EMAIL_VERIFIED, user.emailVerified)
            putBoolean(KEY_PHONE_VERIFIED, user.phoneVerified)
            putBoolean(KEY_TWO_FACTOR_ENABLED, user.twoFactorEnabled)
            apply()
        }
    }

    fun getToken(): String? {
        return getPrefs().getString(KEY_TOKEN, null)
    }

    fun getUserId(): Int {
        return getPrefs().getInt(KEY_USER_ID, -1)
    }

    fun getUser(): User? {
        val id = getPrefs().getInt(KEY_USER_ID, -1)
        if (id == -1) return null

        return User(
            id = id,
            fullName = getPrefs().getString(KEY_FULL_NAME, "") ?: "",
            email = getPrefs().getString(KEY_EMAIL, "") ?: "",
            phone = getPrefs().getString(KEY_PHONE, "") ?: "",
            role = getPrefs().getString(KEY_ROLE, "") ?: "",
            username = getPrefs().getString(KEY_USERNAME, null),
            profileImageUrl = getPrefs().getString(KEY_PROFILE_IMAGE_URL, null),
            status = getPrefs().getString(KEY_STATUS, "") ?: "",
            emailVerified = getPrefs().getBoolean(KEY_EMAIL_VERIFIED, false),
            phoneVerified = getPrefs().getBoolean(KEY_PHONE_VERIFIED, false),
            twoFactorEnabled = getPrefs().getBoolean(KEY_TWO_FACTOR_ENABLED, false)
        )
    }

    fun clearSession() {
        getPrefs().edit().clear().apply()
    }

    fun isUserLoggedIn(): Boolean {
        return getToken() != null
    }

    fun saveKycUrls(dniFront: String, dniBack: String, license: String) {
        getPrefs().edit().apply {
            putString(KEY_KYC_DNI_FRONT, dniFront)
            putString(KEY_KYC_DNI_BACK, dniBack)
            putString(KEY_KYC_LICENSE, license)
            apply()
        }
    }

    fun getKycDniFront(): String {
        return getPrefs().getString(KEY_KYC_DNI_FRONT, "") ?: ""
    }

    fun getKycDniBack(): String {
        return getPrefs().getString(KEY_KYC_DNI_BACK, "") ?: ""
    }

    fun getKycLicense(): String {
        return getPrefs().getString(KEY_KYC_LICENSE, "") ?: ""
    }
}