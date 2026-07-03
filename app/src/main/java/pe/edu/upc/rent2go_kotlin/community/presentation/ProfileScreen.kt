package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.rent2go_kotlin.common.ui.theme.DarkBlue
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.theme.TextGray

import pe.edu.upc.rent2go_kotlin.iam.presentation.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.automirrored.filled.Logout
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
    onLogoutClick: () -> Unit,
    onKycClick: () -> Unit,
    onTermsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val user = authViewModel.currentUser
    var isUploadingProfile by remember { mutableStateOf(false) }

    // Helper: read bytes from a content URI
    fun readBytes(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }

    fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else "profile.jpg"
            } else "profile.jpg"
        } ?: "profile.jpg"
    }

    val profileImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = readBytes(uri)
            if (bytes != null) {
                isUploadingProfile = true
                authViewModel.uploadImage(bytes, getFileName(uri)) { url ->
                    authViewModel.updateProfileImage(url)
                    isUploadingProfile = false
                }
            }
        }
    }

    LaunchedEffect(user) {
        if (user != null) {
            profileViewModel.loadUserReputation(user.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = DarkBlue,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        if (isUploadingProfile) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = PrimaryCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else if (!user?.profileImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName ?: "Usuario de Prueba",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = user?.email ?: "usuario@rent2go.com",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                        Text(
                            text = when (user?.role) {
                                "OWNER" -> "Propietario"
                                "RENTER" -> "Arrendatario"
                                else -> "Tipo de cuenta pendiente de selección"
                            },
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                    // US09 — edit own profile (name/phone)
                    IconButton(onClick = { authViewModel.startEditingProfile() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // K4: mientras carga o si falla, se muestra "—" en vez de un
                    // valor fabricado (antes 5.0/100.0 aparentaban ser reales).
                    if (profileViewModel.isLoading) {
                        StatItem("—", "Viajes")
                        StatItem("—", "Valoración")
                        StatItem("—", "Aceptación")
                    } else {
                        StatItem(profileViewModel.completedTrips?.toString() ?: "—", "Viajes")
                        StatItem(profileViewModel.averageRating?.let { String.format("%.2f", it) } ?: "—", "Valoración")
                        StatItem(profileViewModel.acceptanceRate?.let { "${it.toInt()}%" } ?: "—", "Aceptación")
                    }
                }
                if (profileViewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se pudo cargar tu reputación",
                        fontSize = 11.sp,
                        color = Color(0xFFFF8A80)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Verification Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ) {
                // K3: refleja el campo real kyc_verified del backend, ya no un
                // heurístico local basado en status/URLs de documentos subidos.
                val kycSubmitted = user?.kycVerified == true
                val emailOk = user?.emailVerified == true
                val phoneOk = user?.phoneVerified == true
                val profileUploaded = !user?.profileImageUrl.isNullOrBlank()
                val verifiedCount = listOf(kycSubmitted, emailOk, phoneOk, profileUploaded).count { it }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Confianza y verificación",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$verifiedCount / 4",
                                color = PrimaryCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Lightweight refresh affordance — re-fetches /auth/me so the
                            // verification badges reflect the real backend state after e.g.
                            // clicking the emailed verification link. No pull-to-refresh
                            // mechanism previously existed on this screen (Phase 0 finding);
                            // this is the minimal, dependency-free equivalent of Flutter's
                            // existing RefreshIndicator on the same screen.
                            IconButton(
                                onClick = { authViewModel.refreshCurrentUser() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Actualizar estado de verificación",
                                    tint = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    VerificationItem(
                        label = "Identidad y documentos (KYC)",
                        isVerified = kycSubmitted,
                        onVerifyClick = onKycClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.05f))
                    VerificationItem(
                        label = "Email verificado",
                        isVerified = emailOk,
                        actionLabel = if (authViewModel.isResendingVerification) "Enviando..." else "Reenviar correo",
                        onVerifyClick = if (!emailOk && !authViewModel.isResendingVerification) {
                            { authViewModel.resendVerificationEmail() }
                        } else null
                    )
                    if (authViewModel.resendVerificationMessage != null) {
                        Text(
                            text = authViewModel.resendVerificationMessage ?: "",
                            fontSize = 11.sp,
                            color = PrimaryCyan,
                            modifier = Modifier.padding(start = 36.dp, bottom = 4.dp)
                        )
                    }
                    if (authViewModel.resendVerificationError != null) {
                        Text(
                            text = authViewModel.resendVerificationError ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFFFF4D4D),
                            modifier = Modifier.padding(start = 36.dp, bottom = 4.dp)
                        )
                    }
                    // Fix 2 — paste-code verification: the user copies the token/code
                    // received by email and pastes it here to call POST /auth/verify
                    // directly, as an alternative to a clickable magic link.
                    if (!emailOk) {
                        Text(
                            text = "Ingresar código",
                            fontSize = 12.sp,
                            color = PrimaryCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 36.dp, bottom = 4.dp)
                                .clickable { authViewModel.startEnteringVerificationCode() }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.05f))
                    VerificationItem("Teléfono verificado", phoneOk)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.05f))
                    VerificationItem(
                        label = "Foto de perfil",
                        isVerified = profileUploaded,
                        onVerifyClick = if (!profileUploaded && !isUploadingProfile) {
                            { profileImagePicker.launch("image/*") }
                        } else null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Términos y Condiciones — entry point for TS15/US57, reads the
            // bundled asset (assets/legal/terms-and-conditions.md) via TermsScreen.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTermsClick() },
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Términos y Condiciones",
                        color = Color.Black.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cerrar Sesión Button
            Button(
                onClick = {
                    authViewModel.logout {
                        onLogoutClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }

    // US09 — Editar perfil propio (nombre, teléfono)
    if (authViewModel.isEditingProfile) {
        AlertDialog(
            onDismissRequest = { authViewModel.cancelEditingProfile() },
            containerColor = Color.White,
            title = { Text("Editar perfil", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Column {
                    OutlinedTextField(
                        value = authViewModel.editFullName,
                        onValueChange = { authViewModel.editFullName = it },
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = authViewModel.editPhone,
                        onValueChange = { authViewModel.editPhone = it },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (authViewModel.profileUpdateError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authViewModel.profileUpdateError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { authViewModel.saveProfile {} },
                    enabled = !authViewModel.isSavingProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                ) {
                    if (authViewModel.isSavingProfile) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Guardar", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.cancelEditingProfile() }) {
                    Text("Cancelar", color = Color.Black)
                }
            }
        )
    }

    // Fix 2 — Verificar correo con código pegado (alternativa a un enlace
    // clicable): el usuario pega aquí el token que recibió por correo y lo
    // envía a POST /auth/verify junto con su propio userId.
    if (authViewModel.isVerificationDialogOpen) {
        AlertDialog(
            onDismissRequest = { authViewModel.cancelEnteringVerificationCode() },
            containerColor = Color.White,
            title = { Text("Verificar correo", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Column {
                    Text(
                        "Pega el código que recibiste por correo electrónico.",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = authViewModel.verificationCodeInput,
                        onValueChange = { authViewModel.verificationCodeInput = it },
                        label = { Text("Código de verificación") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (authViewModel.verifyCodeError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authViewModel.verifyCodeError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { authViewModel.submitVerificationCode() },
                    enabled = !authViewModel.isVerifyingCode,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                ) {
                    if (authViewModel.isVerifyingCode) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Verificar", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.cancelEnteringVerificationCode() }) {
                    Text("Cancelar", color = Color.Black)
                }
            }
        )
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 12.sp, color = TextGray)
    }
}

@Composable
fun VerificationItem(
    label: String,
    isVerified: Boolean,
    onVerifyClick: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    val isClickable = onVerifyClick != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier.clickable { onVerifyClick?.invoke() }
                else Modifier
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isVerified) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (isVerified) PrimaryCyan else Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        if (isClickable) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = actionLabel ?: (if (isVerified) "Modificar" else "Verificar"),
                fontSize = 12.sp,
                color = PrimaryCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
