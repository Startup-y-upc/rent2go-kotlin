package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.catalog.domain.Car
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan

@Composable
fun CarDetailScreen(
    carId: Int,
    onBackClick: () -> Unit,
    onReserveClick: (Int) -> Unit
) {
    // In a real app we'd fetch the car from a ViewModel. 
    // Using a placeholder for UI demonstration.
    val car = Car(
        id = carId,
        brand = "Tesla",
        model = "Model 3",
        type = "Eléctrico",
        transmission = "Auto",
        fuel = "Eléctrico",
        seats = 5,
        pricePerDay = 49.0,
        rating = 4.96,
        imageUrl = "https://platform.cstatic-images.com/xlarge/in/v2/stock_photos/06981146-24e5-472e-8344-9040d2165249/098797f1-8404-45e0-9944-80226c6d0426.png",
        description = "Coche impecable, ideal para escapadas. Cargador Type 2 incluido y acceso a la red Supercharger. Asientos calefactables, piloto automático y techo panorámico.",
        ownerName = "Lucía M."
    )

    val scrollState = rememberScrollState()

    var isFavorite by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(LightBlueBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header with Image
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                AsyncImage(
                    model = car.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Top Bar actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(40.dp).clickable { onBackClick() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.padding(8.dp), tint = Color.Black)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = CircleShape, 
                            color = Color.White, 
                            modifier = Modifier.size(40.dp).clickable { /* Share Action */ }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(10.dp), tint = Color.Black)
                        }
                        Surface(
                            shape = CircleShape, 
                            color = Color.White, 
                            modifier = Modifier.size(40.dp).clickable { isFavorite = !isFavorite }
                        ) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                                contentDescription = null, 
                                modifier = Modifier.padding(10.dp), 
                                tint = if (isFavorite) Color.Red else Color.Black
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = car.type, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = PrimaryCyan, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Text(text = " ${car.rating} · 142 viajes", fontSize = 12.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "${car.brand} ${car.model}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "Long Range · 2024", fontSize = 18.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                // Technical Specs
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SpecItem(Icons.Default.ElectricBolt, "498 km", "Autonomía")
                        SpecItem(Icons.Default.Groups, "5", "Plazas")
                        SpecItem(Icons.Default.Settings, "Auto", "Cambio")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Host info
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color.LightGray) {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row {
                                    Text(text = car.ownerName, color = Color.Black, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Verificado", color = PrimaryCyan, fontSize = 12.sp)
                                }
                                Text(text = "Anfitriona desde 2022 · Responde en ~1h", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(text = "Mensaje", color = Color.Black, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ValidationTag("DNI verificado")
                            ValidationTag("Carnet validado")
                            ValidationTag("Teléfono")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "SOBRE EL COCHE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = car.description, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.8f))
                
                Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom bar
            }
        }

        // Bottom Price and Booking
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = car.pricePerDay.toInt().toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = " /día", fontSize = 14.sp, color = Color.Gray)
                    }
                    Text(text = "2 días · Total 98 €", fontSize = 12.sp, color = Color.Gray)
                }
                Button(
                    onClick = { onReserveClick(carId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp).width(180.dp)
                ) {
                    Text(text = "Reservar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun SpecItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.Black)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ValidationTag(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(PrimaryCyan.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 10.sp, color = Color.Black)
    }
}
