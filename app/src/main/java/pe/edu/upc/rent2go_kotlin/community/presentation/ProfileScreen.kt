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
    onKycClick: () -> Unit
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
                    Column {
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
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(profileViewModel.completedTrips.toString(), "Viajes")
                    StatItem(String.format("%.2f", profileViewModel.averageRating), "Valoración")
                    StatItem("${profileViewModel.acceptanceRate.toInt()}%", "Aceptación")
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
                val kycSubmitted = user?.status == "ACTIVE" || user?.status == "VERIFIED" ||
                        authViewModel.isKycSuccess
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
                        Text(
                            text = "$verifiedCount / 4",
                            color = PrimaryCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    VerificationItem(
                        label = "Identidad y documentos (KYC)",
                        isVerified = kycSubmitted,
                        onVerifyClick = if (!kycSubmitted) onKycClick else null
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.05f))
                    VerificationItem("Email verificado", emailOk)
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
            
            // Placeholder for lower section
            Surface(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Configuración de la cuenta", color = Color.Black.copy(alpha = 0.5f))
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
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 12.sp, color = TextGray)
    }
}

@Composable
fun VerificationItem(label: String, isVerified: Boolean, onVerifyClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isVerified) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = if (isVerified) PrimaryCyan else Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 14.sp, color = Color.Black)
        }
        if (!isVerified && onVerifyClick != null) {
            Text(
                text = "Verificar",
                fontSize = 12.sp,
                color = PrimaryCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onVerifyClick() }
            )
        }
    }
}
