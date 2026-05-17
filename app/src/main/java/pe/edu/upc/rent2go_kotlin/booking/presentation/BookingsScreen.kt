package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.common.ui.theme.DarkBlue
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.theme.TextGray

@Composable
fun BookingsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Próximas", "Activas", "Pasadas")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis reservas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Custom Tabs
        Surface(
            color = Color.Black.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color.White else Color.Transparent,
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        onClick = { selectedTab = index }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                NextBookingCard()
            }
            
            item {
                Text(
                    text = "Anteriores",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(previousBookings) { booking ->
                PreviousBookingItem(booking)
            }
        }
    }
}

@Composable
fun NextBookingCard() {
    Surface(
        color = DarkBlue,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(PrimaryCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Próxima", color = PrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tesla Model 3", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Long Range · 2024", color = TextGray, fontSize = 14.sp)
                }
                AsyncImage(
                    model = "https://platform.cstatic-images.com/xlarge/in/v2/stock_photos/06981146-24e5-472e-8344-9040d2165249/098797f1-8404-45e0-9944-80226c6d0426.png",
                    contentDescription = null,
                    modifier = Modifier.size(100.dp, 60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Recoge", color = TextGray, fontSize = 12.sp)
                    Text("12 May · 10:00", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text("-----------", color = TextGray.copy(alpha = 0.3f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Devuelve", color = TextGray, fontSize = 12.sp)
                    Text("14 May · 18:00", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Lucía M.", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Calle Goya 24", color = TextGray, fontSize = 12.sp)
                }
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp).width(100.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Abrir", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PreviousBookingItem(booking: PreviousBooking) {
    Surface(
        color = Color.White.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = booking.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp, 40.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.carName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text("${booking.dates} · ${booking.price}", fontSize = 12.sp, color = Color.DarkGray)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Black)
        }
    }
}

data class PreviousBooking(
    val carName: String,
    val dates: String,
    val price: String,
    val imageUrl: String
)

val previousBookings = listOf(
    PreviousBooking("Mini Cooper S", "28 abr — 30 abr", "76 €", "https://img.remediosdigitales.com/391157/mini-cooper-s-2021-11/1366_2000.jpg"),
    PreviousBooking("Volkswagen Golf", "12 abr — 13 abr", "32 €", "https://cdn.pixabay.com/photo/2021/01/21/09/57/car-5936850_1280.jpg")
)
