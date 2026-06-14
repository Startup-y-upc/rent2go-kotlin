package pe.edu.upc.rent2go_kotlin.iam.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.iam.domain.AuthRepository
import pe.edu.upc.rent2go_kotlin.iam.domain.User

class AuthViewModel(
    private val repository: AuthRepository = DependencyProvider.authRepository
) : ViewModel() {

    // Login Form State
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // Sign Up Form State
    var registerFullName by mutableStateOf("")
    var registerEmail by mutableStateOf("")
    var registerPhone by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerUsername by mutableStateOf("")
    var selectedAccountType by mutableStateOf("RENTER") // OWNER or RENTER

    // KYC Form State (mock paths/URLs)
    var kycDniNumber by mutableStateOf("")
    var kycDniFrontUrl by mutableStateOf("https://rent2go-uploads.s3.amazonaws.com/dni_front.jpg")
    var kycDniBackUrl by mutableStateOf("https://rent2go-uploads.s3.amazonaws.com/dni_back.jpg")
    var kycLicenseUrl by mutableStateOf("https://rent2go-uploads.s3.amazonaws.com/license.jpg")

    // UI Status State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var currentUser by mutableStateOf<User?>(null)
    var isAuthSuccess by mutableStateOf(false)
    var isKycSuccess by mutableStateOf(false)

    init {
        currentUser = pe.edu.upc.rent2go_kotlin.common.SessionManager.getUser()
        if (currentUser != null) {
            isAuthSuccess = true
        }
    }

    // Password Reset Form State
    var passwordResetEmail by mutableStateOf("")
    var passwordResetToken by mutableStateOf("")
    var passwordResetNewPassword by mutableStateOf("")
    var passwordResetConfirmPassword by mutableStateOf("")
    var isResetCodeSent by mutableStateOf(false)

    fun clearError() {
        errorMessage = null
    }

    fun login(rememberMe: Boolean, onSuccess: () -> Unit) {
        if (loginEmail.isBlank() || loginPassword.isBlank()) {
            errorMessage = "Por favor, complete todos los campos."
            return
        }

        pe.edu.upc.rent2go_kotlin.common.SessionManager.setRememberMe(rememberMe)

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val user = repository.login(loginEmail.trim(), loginPassword)
                currentUser = user
                isAuthSuccess = true
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Credenciales incorrectas"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        if (registerFullName.isBlank() || registerEmail.isBlank() || registerPhone.isBlank() || registerPassword.isBlank()) {
            errorMessage = "Por favor, complete todos los campos de registro."
            return
        }

        // Si el username está vacío, usar el email como username
        if (registerUsername.isBlank()) {
            registerUsername = registerEmail.substringBefore("@")
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val user = repository.register(
                    username = registerUsername.trim(),
                    fullName = registerFullName.trim(),
                    email = registerEmail.trim(),
                    phone = registerPhone.trim(),
                    password = registerPassword,
                    accountType = selectedAccountType
                )
                currentUser = user
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al registrar cuenta"
            } finally {
                isLoading = false
            }
        }
    }

    fun submitKyc(onSuccess: () -> Unit) {
        val userId = currentUser?.id ?: 1 // Fallback a id 1 si no hay usuario registrado/logueado
        val name = currentUser?.fullName ?: registerFullName.ifBlank { "Usuario de Prueba" }

        // Si no se ha completado el número de DNI, poner un placeholder
        val idNum = kycDniNumber.ifBlank { "77777777" }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val success = repository.submitKyc(
                    userId = userId,
                    fullName = name,
                    idNumber = idNum,
                    dniFrontUrl = kycDniFrontUrl,
                    dniBackUrl = kycDniBackUrl,
                    driverLicenseUrl = kycLicenseUrl
                )
                if (success) {
                    isKycSuccess = true
                    onSuccess()
                } else {
                    errorMessage = "Error al validar la documentación KYC"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al enviar la validación"
            } finally {
                isLoading = false
            }
        }
    }

    fun requestPasswordReset(onSuccess: () -> Unit) {
        if (passwordResetEmail.isBlank()) {
            errorMessage = "Por favor, ingrese su correo electrónico."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val success = repository.requestPasswordReset(passwordResetEmail.trim())
                if (success) {
                    isResetCodeSent = true
                    onSuccess()
                } else {
                    errorMessage = "Error al solicitar el código de restablecimiento."
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al solicitar el código."
            } finally {
                isLoading = false
            }
        }
    }

    fun confirmPasswordReset(onSuccess: () -> Unit) {
        if (passwordResetToken.isBlank() || passwordResetNewPassword.isBlank() || passwordResetConfirmPassword.isBlank()) {
            errorMessage = "Por favor, complete todos los campos."
            return
        }

        if (passwordResetNewPassword != passwordResetConfirmPassword) {
            errorMessage = "Las contraseñas no coinciden."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val success = repository.confirmPasswordReset(
                    token = passwordResetToken.trim(),
                    newPassword = passwordResetNewPassword
                )
                if (success) {
                    onSuccess()
                } else {
                    errorMessage = "Error al restablecer la contraseña."
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al restablecer la contraseña."
            } finally {
                isLoading = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        pe.edu.upc.rent2go_kotlin.common.SessionManager.clearSession()
        currentUser = null
        isAuthSuccess = false
        loginEmail = ""
        loginPassword = ""
        onSuccess()
    }
}
