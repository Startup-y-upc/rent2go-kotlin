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
    private const val KEY_REMEMBER_ME = "remember_me"

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
            profileImageUrl = getPrefs().getString(KEY_PROFILE_IMAGE_URL, null)
        )
    }

    fun clearSession() {
        getPrefs().edit().clear().apply()
    }

    fun isUserLoggedIn(): Boolean {
        return getToken() != null
    }
}