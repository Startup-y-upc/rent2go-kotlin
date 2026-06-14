package pe.edu.upc.rent2go_kotlin.iam.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.theme.SurfaceBlue
import pe.edu.upc.rent2go_kotlin.common.ui.theme.TextGray

@Composable
fun AccountTypeScreen(
    viewModel: AuthViewModel,
    onContinueClick: () -> Unit
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
            text = "¿Cómo vas a usar\nRent2Go?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Puedes cambiarlo cuando quieras desde tu perfil.",
            fontSize = 16.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        AccountTypeCard(
            title = "Quiero alquilar",
            description = "Encuentra el coche perfecto cerca de ti. Reserva por horas o días.",
            items = listOf("Buscar y reservar coches", "Pagar de forma segura", "Mensajes con propietarios"),
            icon = Icons.Default.DirectionsCar,
            isSelected = viewModel.selectedAccountType == "RENTER",
            onClick = { viewModel.selectedAccountType = "RENTER" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AccountTypeCard(
            title = "Quiero rentar mi auto",
            description = "Convierte tu coche parado en ingresos. Tú decides precio y disponibilidad.",
            items = listOf("Publicar tu vehículo", "Gestionar reservas", "Cobrar mensualmente"),
            icon = Icons.Default.Person,
            isSelected = viewModel.selectedAccountType == "OWNER",
            onClick = { viewModel.selectedAccountType = "OWNER" }
        )

        Spacer(modifier = Modifier.weight(1f))

        if (viewModel.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = viewModel.errorMessage ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                viewModel.register {
                    onContinueClick()
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
                Text(
                    text = "Continuar como ${if (viewModel.selectedAccountType == "RENTER") "arrendatario" else "propietario"}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AccountTypeCard(
    title: String,
    description: String,
    items: List<String>,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = SurfaceBlue,
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, PrimaryCyan) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = description, color = TextGray, fontSize = 12.sp)
                }
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan, unselectedColor = TextGray)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = TextGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar, // Placeholder for check icon
                        contentDescription = null,
                        tint = PrimaryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item, color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
