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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan

@Composable
fun CarDetailScreen(
    carId: Int,
    onBackClick: () -> Unit,
    onReserveClick: (Int) -> Unit,
    viewModel: VehicleDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return VehicleDetailViewModel(
                    DependencyProvider.vehicleRepository,
                    DependencyProvider.bookingRepository
                ) as T
            }
        }
    )
) {
    val state = viewModel.state.value

    LaunchedEffect(carId) {
        viewModel.loadVehicle(carId)
    }

    val scrollState = rememberScrollState()
    var isFavorite by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(LightBlueBg)) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryCyan)
                }
            }

            state.error.isNotBlank() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadVehicle(carId) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            state.vehicle != null -> {
                val vehicle = state.vehicle

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Header with Image
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        if (vehicle.primaryImageUrl != null) {
                            AsyncImage(
                                model = vehicle.primaryImageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
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
                        // Category badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = PrimaryCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = vehicle.categoryName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = PrimaryCyan,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${vehicle.make} · ${vehicle.year}",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${vehicle.make} ${vehicle.model}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = vehicle.location,
                            fontSize = 18.sp,
                            color = Color.Gray
                        )

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
                                SpecItem(
                                    icon = Icons.Default.LocalGasStation,
                                    value = vehicle.fuelType,
                                    label = "Combustible"
                                )
                                SpecItem(
                                    icon = Icons.Default.Groups,
                                    value = vehicle.seats.toString(),
                                    label = "Plazas"
                                )
                                SpecItem(
                                    icon = Icons.Default.Settings,
                                    value = vehicle.transmission,
                                    label = "Cambio"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Features
                        if (vehicle.features.isNotEmpty()) {
                            Text(
                                text = "CARACTERÍSTICAS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = Color.White.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    vehicle.features.forEach { feature ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = PrimaryCyan
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = feature,
                                                fontSize = 14.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Description
                        Text(
                            text = "SOBRE EL VEHÍCULO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = vehicle.description,
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.8f)
                        )

                        // Vehicle details
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "DETALLES",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailRow("Placa", vehicle.licensePlate)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.1f))
                                DetailRow("VIN", vehicle.vin)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.1f))
                                DetailRow("Estado", if (state.occupiedUntil != null) "OCUPADO" else vehicle.status)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.1f))
                                DetailRow("Año", vehicle.year.toString())
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.1f))
                                DetailRow("Disponibilidad", if (state.occupiedUntil != null) "Hasta ${state.occupiedUntil}" else "Libre")
                            }
                        }

                        Spacer(modifier = Modifier.height(160.dp)) // Padding for bottom bar
                    }
                }

                // Bottom Price and Booking
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "S/ ${String.format("%.0f", vehicle.dailyPrice)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = " /día",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Button(
                            onClick = { onReserveClick(carId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.occupiedUntil != null) Color.Gray else Color.Black,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp).width(180.dp),
                            enabled = state.occupiedUntil == null
                        ) {
                            Text(
                                text = if (state.occupiedUntil != null) "Reservado" else "Reservar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.Black)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.weight(1.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
