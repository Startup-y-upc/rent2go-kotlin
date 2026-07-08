package pe.edu.upc.rent2go_kotlin.iam.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.theme.TextGray

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onContinueClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Rent", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "2", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
            Text(text = "Go", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Crea tu cuenta\nen Rent2Go.",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Empezamos con tus datos básicos. Después elegirás cómo usar la app.",
            fontSize = 16.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        // Steps Indicator
        // Phase 9 (US77) — was a hardcoded fixed dark-navy Color(0xFF1B2336); now theme-derived
        // so it adapts if this screen's surrounding theme ever changes from its current dark look.
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StepItem("01", "Datos", true)
                StepItem("02", "Tipo cuenta", false)
                StepItem("03", "Validación", false)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        InputField(
            label = "Nombre completo",
            value = viewModel.registerFullName,
            onValueChange = { viewModel.registerFullName = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        InputField(
            label = "Correo electrónico",
            value = viewModel.registerEmail,
            onValueChange = { viewModel.registerEmail = it.lowercase().trim() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        InputField(
            label = "Teléfono",
            value = viewModel.registerPhone,
            onValueChange = { text ->
                val digits = text.filter { it.isDigit() }
                if (digits.length <= 9) {
                    viewModel.registerPhone = digits
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        InputField(
            label = "Contraseña",
            value = viewModel.registerPassword,
            onValueChange = { viewModel.registerPassword = it },
            isPassword = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        InputField(
            label = "Confirmar contraseña",
            value = viewModel.registerConfirmPassword,
            onValueChange = { viewModel.registerConfirmPassword = it },
            isPassword = true
        )
        
        if (viewModel.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = viewModel.errorMessage ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.clearError()
                val fullName = viewModel.registerFullName.trim()
                val email = viewModel.registerEmail.trim()
                val phone = viewModel.registerPhone.trim()
                val password = viewModel.registerPassword
                val confirmPassword = viewModel.registerConfirmPassword

                if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    viewModel.errorMessage = "Por favor, complete todos los campos de registro."
                    return@Button
                }
                if (phone.length != 9) {
                    viewModel.errorMessage = "El teléfono debe tener exactamente 9 dígitos."
                    return@Button
                }
                if (!email.contains("@") || !email.contains(".")) {
                    viewModel.errorMessage = "Por favor, ingrese un correo válido."
                    return@Button
                }
                if (password != confirmPassword) {
                    viewModel.errorMessage = "Las contraseñas no coinciden."
                    return@Button
                }
                if (password.length < 6) {
                    viewModel.errorMessage = "La contraseña debe tener al menos 6 caracteres."
                    return@Button
                }

                viewModel.clearError()
                onContinueClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Continuar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Ya tienes cuenta? ", color = Color.White, fontSize = 14.sp)
            TextButton(
                onClick = onLoginClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "Iniciar sesión", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StepItem(number: String, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = if (isActive) Color.White else TextGray, fontSize = 12.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
        Text(text = label, color = if (isActive) Color.White else TextGray, fontSize = 10.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit, isPassword: Boolean = false) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        Text(text = label, color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = Color.White)
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
