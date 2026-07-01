package pe.edu.upc.rent2go_kotlin.iam.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.theme.SurfaceBlue
import pe.edu.upc.rent2go_kotlin.common.ui.theme.TextGray
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun ValidationScreen(
    viewModel: AuthViewModel,
    onFinishClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Track which card we're uploading for
    var uploadingCard by remember { mutableStateOf<String?>(null) }

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
                if (nameIndex >= 0) it.getString(nameIndex) else "image.jpg"
            } else "image.jpg"
        } ?: "image.jpg"
    }

    // Image pickers for each document
    val dniFrontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = readBytes(uri) ?: return@rememberLauncherForActivityResult
            uploadingCard = "dniFront"
            viewModel.uploadImage(bytes, getFileName(uri)) { url ->
                viewModel.kycDniFrontUrl = url
                pe.edu.upc.rent2go_kotlin.common.SessionManager.saveKycUrls(
                    dniFront = url,
                    dniBack = viewModel.kycDniBackUrl,
                    license = viewModel.kycLicenseUrl
                )
                uploadingCard = null
            }
        }
    }

    val dniBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = readBytes(uri) ?: return@rememberLauncherForActivityResult
            uploadingCard = "dniBack"
            viewModel.uploadImage(bytes, getFileName(uri)) { url ->
                viewModel.kycDniBackUrl = url
                pe.edu.upc.rent2go_kotlin.common.SessionManager.saveKycUrls(
                    dniFront = viewModel.kycDniFrontUrl,
                    dniBack = url,
                    license = viewModel.kycLicenseUrl
                )
                uploadingCard = null
            }
        }
    }

    val licensePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = readBytes(uri) ?: return@rememberLauncherForActivityResult
            uploadingCard = "license"
            viewModel.uploadImage(bytes, getFileName(uri)) { url ->
                viewModel.kycLicenseUrl = url
                pe.edu.upc.rent2go_kotlin.common.SessionManager.saveKycUrls(
                    dniFront = viewModel.kycDniFrontUrl,
                    dniBack = viewModel.kycDniBackUrl,
                    license = url
                )
                uploadingCard = null
            }
        }
    }

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
            text = "Valida tu cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sube una foto clara de tu DNI y de tu licencia de conducir.",
            fontSize = 16.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // DNI - Front
        UploadCard(
            title = "DNI - Anverso (Frente)",
            subtitle = "Foto clara del frente de tu DNI",
            isUploaded = viewModel.kycDniFrontUrl.isNotBlank(),
            imageUrl = viewModel.kycDniFrontUrl,
            isUploading = uploadingCard == "dniFront",
            onUploadClick = { dniFrontPicker.launch("image/*") },
            onDeleteClick = {
                viewModel.kycDniFrontUrl = ""
                viewModel.isUploadingDniFront = false
                pe.edu.upc.rent2go_kotlin.common.SessionManager.saveKycUrls(
                    dniFront = "",
                    dniBack = viewModel.kycDniBackUrl,
                    license = viewModel.kycLicenseUrl
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // DNI - Back
        UploadCard(
            title = "DNI - Reverso (Atrás)",
            subtitle = "Foto clara del reverso de tu DNI",
            isUploaded = viewModel.kycDniBackUrl.isNotBlank(),
            imageUrl = viewModel.kycDniBackUrl,
            isUploading = uploadingCard == "dniBack",
            onUploadClick = { dniBackPicker.launch("image/*") },
            onDeleteClick = {
                viewModel.kycDniBackUrl = ""
                viewModel.isUploadingDniBack = false
                pe.edu.upc.rent2go_kotlin.common.SessionManager.saveKycUrls(
                    dniFront = viewModel.kycDniFrontUrl,
                    dniBack = "",
                    license = viewModel.kycLicenseUrl
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Driver's License
        UploadCard(
            title = "Licencia de conducir",
            subtitle = "Vigente, en color y completa",
            isUploaded = viewModel.kycLicenseUrl.isNotBlank(),
            imageUrl = viewModel.kycLicenseUrl,
            isUploading = uploadingCard == "license",
            onUploadClick = { licensePicker.launch("image/*") },
            onDeleteClick = {
                viewModel.kycLicenseUrl = ""
                viewModel.isUploadingLicense = false
                pe.edu.upc.rent2go_kotlin.common.SessionManager.saveKycUrls(
                    dniFront = viewModel.kycDniFrontUrl,
                    dniBack = viewModel.kycDniBackUrl,
                    license = ""
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (viewModel.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = viewModel.errorMessage ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onFinishClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                border = BorderStroke(1.dp, Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Omitir",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Button(
                onClick = {
                    viewModel.submitKyc {
                        onFinishClick()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isLoading && uploadingCard == null
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Enviar",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun UploadCard(
    title: String,
    subtitle: String? = null,
    isUploaded: Boolean,
    imageUrl: String = "",
    isUploading: Boolean = false,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceBlue,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    color = if (isUploaded) Color.Transparent else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isUploaded && imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = title, color = Color.White, fontSize = 14.sp)
                    if (subtitle != null) {
                        Text(text = subtitle, color = TextGray, fontSize = 10.sp)
                    }

                    if (isUploading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryCyan,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SUBIENDO...",
                                color = PrimaryCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isUploaded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                text = "✓ SUBIDO",
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUploadClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    enabled = !isUploading
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isUploading) Color.White.copy(alpha = 0.3f) else Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isUploaded) "Volver a subir" else "Subir foto",
                        fontSize = 12.sp,
                        color = if (isUploading) Color.White.copy(alpha = 0.3f) else Color.White
                    )
                }

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    enabled = isUploaded && !isUploading
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isUploaded && !isUploading) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Borrar",
                        fontSize = 12.sp,
                        color = if (isUploaded && !isUploading) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
