package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    carId: Int,
    onBackClick: () -> Unit,
    onPaymentClick: () -> Unit
) {
    var selectedCoverage by remember { mutableStateOf("PLUS") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar reserva", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBlueBg)
            )
        },
        containerColor = LightBlueBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Car Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = "https://platform.cstatic-images.com/xlarge/in/v2/stock_photos/06981146-24e5-472e-8344-9040d2165249/098797f1-8404-45e0-9944-80226c6d0426.png",
                        contentDescription = null,
                        modifier = Modifier.size(80.dp, 50.dp).clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Tesla Model 3", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.DarkGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lucía M.", fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rental Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RentalInfoItem(Icons.Default.CalendarMonth, "Recogida", "Mar 12 May · 10:00")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))
                    RentalInfoItem(Icons.Default.AccessTime, "Devolución", "Jue 14 May · 18:00")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))
                    RentalInfoItem(Icons.Default.LocationOn, "Punto de encuentro", "Calle Goya 24, Madrid")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Cobertura", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            // Coverage Options
            CoverageOption(
                title = "Esencial",
                subtitle = "Franquicia 1.500 €",
                price = "0 €",
                isSelected = selectedCoverage == "ESSENTIAL",
                onClick = { selectedCoverage = "ESSENTIAL" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            CoverageOption(
                title = "Plus",
                tag = "Popular",
                subtitle = "Sin franquicia · Recomendada",
                price = "8 €/día",
                isSelected = selectedCoverage == "PLUS",
                onClick = { selectedCoverage = "PLUS" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            CoverageOption(
                title = "Premium",
                subtitle = "Sin franquicia + asistencia ilimitada",
                price = "14 €/día",
                isSelected = selectedCoverage == "PREMIUM",
                onClick = { selectedCoverage = "PREMIUM" }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Price Breakdown Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PriceRow("49 € × 2 días", "98,00 €")
                    PriceRow("Cobertura Plus", "16,00 €")
                    PriceRow("Tasa de servicio", "9,40 €")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("Total", fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Color.Black)
                        Text("123,40 €", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onPaymentClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Pagar y reservar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RentalInfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Black)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}

@Composable
fun CoverageOption(
    title: String,
    subtitle: String,
    price: String,
    tag: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, PrimaryCyan) else null,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCyan, unselectedColor = Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    if (tag != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = PrimaryCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                            Text(tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp), color = PrimaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Text(price, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
        }
    }
}

@Composable
fun PriceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.7f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}
