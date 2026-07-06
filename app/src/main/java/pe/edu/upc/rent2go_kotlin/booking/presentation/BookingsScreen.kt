package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.booking.domain.Booking
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.ui.theme.DarkBlue
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onBookingClick: (Int) -> Unit = {},
    viewModel: BookingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return BookingsViewModel(
                    DependencyProvider.bookingRepository,
                    DependencyProvider.vehicleRepository
                ) as T
            }
        }
    )
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Próximas", "Activas", "Pasadas")
    val state = viewModel.state.value
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    if (bookingToCancel != null) {
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            containerColor = Color.White,
            title = { Text("¿Cancelar reserva?", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas cancelar esta reserva? Esta acción no se puede deshacer.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val booking = bookingToCancel!!
                        viewModel.cancelBooking(booking.id) {
                            // Canceled successfully
                        }
                        bookingToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF56C6C))
                ) {
                    Text("Sí, cancelar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { bookingToCancel = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) {
                    Text("Atrás")
                }
            }
        )
    }

    val filteredBookings = remember(state.bookings, selectedTab) {
        state.bookings.filter { booking ->
            when (selectedTab) {
                0 -> booking.status == "PENDING" || booking.status == "CONFIRMED"
                1 -> booking.status == "ACTIVE"
                2 -> booking.status == "COMPLETED" || booking.status == "CANCELLED" || booking.status == "EXPIRED"
                else -> false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        // Phase 7 — pull-to-refresh wired to the existing loadBookings() reload path.
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.loadBookings() },
            modifier = Modifier.fillMaxSize()
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Task 5 fix: PullToRefreshBox already shows its own spinner while
            // state.isLoading is true (e.g. during pull-to-refresh on a non-empty list).
            // Only show this full-screen inline loader for the *initial* load, when there
            // is nothing on screen yet — otherwise both spinners would render at once.
            if (state.isLoading && state.bookings.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryCyan)
                    }
                }
            } else if (state.error.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.error, color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBookings() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (filteredBookings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes reservas en esta sección.",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                if (selectedTab == 0 || selectedTab == 1) {
                    val nextBooking = filteredBookings.first()
                    val vehicle = state.vehicles[nextBooking.vehicleId]
                    item {
                        NextBookingCard(nextBooking, vehicle, onBookingClick = onBookingClick, onCancelClick = { bookingToCancel = nextBooking })
                    }
                    
                    if (filteredBookings.size > 1) {
                        item {
                            Text(
                                text = "Otras reservas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(filteredBookings.drop(1)) { booking ->
                            val v = state.vehicles[booking.vehicleId]
                            PreviousBookingItem(booking, v, onBookingClick = onBookingClick, onCancelClick = { bookingToCancel = booking })
                        }
                    }
                } else {
                    items(filteredBookings) { booking ->
                        val v = state.vehicles[booking.vehicleId]
                        PreviousBookingItem(booking, v, onBookingClick = onBookingClick)
                    }
                }
            }
        }
        }
    }
}

@Composable
fun NextBookingCard(booking: Booking, vehicle: Vehicle?, onBookingClick: (Int) -> Unit = {}, onCancelClick: () -> Unit) {
    val carName = if (vehicle != null) "${vehicle.make} ${vehicle.model}" else "Vehículo #${booking.vehicleId}"
    val yearAndCategory = if (vehicle != null) "${vehicle.categoryName} · ${vehicle.year}" else ""
    // Phase 8 (item 7) — vehicle.primaryImageUrl first (full catalog lookup), falling back
    // to the reservation's own vehicle_image (ReservationResource) when the vehicle lookup
    // by ID hasn't resolved (e.g. still loading), instead of showing no thumbnail at all.
    val imageUrl = vehicle?.primaryImageUrl ?: booking.vehicleImage ?: ""
    val location = booking.pickupLocation

    Surface(
        color = DarkBlue,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().clickable { onBookingClick(booking.id) }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(getStatusColor(booking.status), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getStatusText(booking.status),
                    color = getStatusColor(booking.status),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(carName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(yearAndCategory, color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("S/ ${String.format("%.2f", booking.totalAmount)}", color = PrimaryCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp, 60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Recoge", color = TextGray, fontSize = 12.sp)
                    Text(booking.startDate, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text("-----------", color = TextGray.copy(alpha = 0.3f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Devuelve", color = TextGray, fontSize = 12.sp)
                    Text(booking.endDate, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    Text("Código: ${booking.reservationCode}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(location, color = TextGray, fontSize = 12.sp)
                }
                if (booking.status == "PENDING" || booking.status == "CONFIRMED") {
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF56C6C)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp).padding(start = 8.dp)
                    ) {
                        Text("Cancelar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PreviousBookingItem(booking: Booking, vehicle: Vehicle?, onBookingClick: (Int) -> Unit = {}, onCancelClick: (() -> Unit)? = null) {
    val carName = if (vehicle != null) "${vehicle.make} ${vehicle.model}" else "Vehículo #${booking.vehicleId}"
    val dates = "${booking.startDate} — ${booking.endDate}"
    val price = "S/ ${String.format("%.2f", booking.totalAmount)}"
    // Phase 8 (item 7) — same fallback as NextBookingCard.
    val imageUrl = vehicle?.primaryImageUrl ?: booking.vehicleImage ?: ""
    
    Surface(
        color = Color.White.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onBookingClick(booking.id) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp, 40.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(carName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text("$dates · $price", fontSize = 12.sp, color = Color.DarkGray)
            }
            
            Surface(
                color = getStatusColor(booking.status).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = getStatusText(booking.status),
                    color = getStatusColor(booking.status),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if ((booking.status == "PENDING" || booking.status == "CONFIRMED") && onCancelClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onCancelClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF56C6C))
                ) {
                    Text("Cancelar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getStatusText(status: String): String {
    return when (status) {
        "PENDING" -> "Pendiente"
        "CONFIRMED" -> "Confirmada"
        "ACTIVE" -> "Activa"
        "COMPLETED" -> "Completada"
        "CANCELLED" -> "Cancelada"
        "EXPIRED" -> "Expirada"
        else -> status
    }
}

private fun getStatusColor(status: String): Color {
    return when (status) {
        "PENDING" -> Color(0xFFE6A23C)
        "CONFIRMED" -> PrimaryCyan
        "ACTIVE" -> Color(0xFF67C23A)
        "COMPLETED" -> Color.LightGray
        "CANCELLED" -> Color(0xFFF56C6C)
        "EXPIRED" -> Color.Gray
        else -> Color.LightGray
    }
}
