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

    // KYC Form State
    var kycDniNumber by mutableStateOf("")
    var kycDniFrontUrl by mutableStateOf("")
    var kycDniBackUrl by mutableStateOf("")
    var kycLicenseUrl by mutableStateOf("")

    // Upload progress per image
    var isUploadingDniFront by mutableStateOf(false)
    var isUploadingDniBack by mutableStateOf(false)
    var isUploadingLicense by mutableStateOf(false)

    // UI Status State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var currentUser by mutableStateOf<User?>(null)
    var isAuthSuccess by mutableStateOf(false)
    var isKycSuccess by mutableStateOf(false)

    init {
        // Si hay un token guardado, obtener los datos frescos del usuario desde /api/v1/auth/me
        if (pe.edu.upc.rent2go_kotlin.common.SessionManager.getToken() != null) {
            viewModelScope.launch {
                try {
                    val user = repository.getMe()
                    currentUser = user
                    isAuthSuccess = true
                } catch (e: Exception) {
                    // Si falla (token expirado, sin conexión), usar datos locales como fallback
                    currentUser = pe.edu.upc.rent2go_kotlin.common.SessionManager.getUser()
                    if (currentUser != null) {
                        isAuthSuccess = true
                    } else {
                        pe.edu.upc.rent2go_kotlin.common.SessionManager.clearSession()
                    }
                }
            }
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

    fun clearRegistrationFields() {
        registerFullName = ""
        registerEmail = ""
        registerPhone = ""
        registerPassword = ""
        registerUsername = ""
        kycDniNumber = ""
        kycDniFrontUrl = ""
        kycDniBackUrl = ""
        kycLicenseUrl = ""
        isKycSuccess = false
        clearError()
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
                // Registrar el usuario en el backend
                repository.register(
                    username = registerUsername.trim(),
                    fullName = registerFullName.trim(),
                    email = registerEmail.trim(),
                    phone = registerPhone.trim(),
                    password = registerPassword,
                    accountType = selectedAccountType
                )
                // Auto-login automático para obtener el token JWT de sesión
                val loggedInUser = repository.login(registerEmail.trim(), registerPassword)
                currentUser = loggedInUser
                isAuthSuccess = true
                loginEmail = registerEmail.trim()
                loginPassword = registerPassword
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al registrar cuenta"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfileImage(url: String) {
        val user = currentUser ?: return
        val updatedUser = user.copy(profileImageUrl = url)
        currentUser = updatedUser
        pe.edu.upc.rent2go_kotlin.common.SessionManager.updateUser(updatedUser)
    }


    fun uploadImage(imageBytes: ByteArray, fileName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.uploadImage(imageBytes, fileName)
                onSuccess(url)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al subir la imagen"
            }
        }
    }

    fun submitKyc(onSuccess: () -> Unit) {
        val userId = currentUser?.id ?: 1
        val name = currentUser?.fullName ?: registerFullName.ifBlank { "Usuario" }
        val idNum = kycDniNumber.ifBlank { "00000000" }

        if (kycDniFrontUrl.isBlank() || kycDniBackUrl.isBlank() || kycLicenseUrl.isBlank()) {
            errorMessage = "Por favor, suba todas las imágenes requeridas."
            return
        }

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
                    // Refresh user data to get updated verification status
                    try {
                        val refreshedUser = repository.getMe()
                        currentUser = refreshedUser
                    } catch (_: Exception) { /* non-fatal */ }
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
        clearRegistrationFields()
        onSuccess()
    }
}
