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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onResetSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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
            text = "Recuperar\ncontraseña",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (!viewModel.isResetCodeSent) {
                "Ingresa tu correo para recibir un token de recuperación de contraseña."
            } else {
                "Ingresa el token de recuperación recibido y tu nueva contraseña."
            },
            fontSize = 16.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!viewModel.isResetCodeSent) {
            // STEP 1: REQUEST CODE
            Text(text = "Correo electrónico", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = viewModel.passwordResetEmail,
                onValueChange = { viewModel.passwordResetEmail = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1B2336),
                    unfocusedContainerColor = Color(0xFF1B2336),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
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

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    viewModel.requestPasswordReset {
                        // Código enviado con éxito, cambia el estado a ingresar el código
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Enviar código", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

        } else {
            // STEP 2: CONFIRM RESET
            Text(text = "Código de recuperación (Token)", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = viewModel.passwordResetToken,
                onValueChange = { viewModel.passwordResetToken = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1B2336),
                    unfocusedContainerColor = Color(0xFF1B2336),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Cambiar contraseña (Nueva)", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = viewModel.passwordResetNewPassword,
                onValueChange = { viewModel.passwordResetNewPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = Color.White)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1B2336),
                    unfocusedContainerColor = Color(0xFF1B2336),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Repetir contraseña", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = viewModel.passwordResetConfirmPassword,
                onValueChange = { viewModel.passwordResetConfirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = Color.White)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1B2336),
                    unfocusedContainerColor = Color(0xFF1B2336),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
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
                    viewModel.confirmPasswordReset {
                        // Restablecimiento exitoso, vuelve a login y limpia variables
                        viewModel.isResetCodeSent = false
                        viewModel.passwordResetEmail = ""
                        viewModel.passwordResetToken = ""
                        viewModel.passwordResetNewPassword = ""
                        viewModel.passwordResetConfirmPassword = ""
                        onResetSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Iniciar sesión", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Ya la recordaste? ", color = Color.White, fontSize = 14.sp)
            TextButton(
                onClick = {
                    viewModel.isResetCodeSent = false
                    viewModel.clearError()
                    onBackToLogin()
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "Iniciar sesión", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
