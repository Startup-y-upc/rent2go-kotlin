package pe.edu.upc.rent2go_kotlin.booking.presentation

import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    carId: Int,
    onBackClick: () -> Unit,
    onPaymentClick: () -> Unit,
    viewModel: BookingConfirmationViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return BookingConfirmationViewModel(
                    DependencyProvider.bookingRepository,
                    DependencyProvider.vehicleRepository
                ) as T
            }
        }
    )
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(carId) {
        viewModel.loadVehicle(carId)
    }

    val vehicle = viewModel.vehicle

    // Helper functions for DatePickerDialog
    fun showDatePicker(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    fun formatDisplayDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEE dd MMM", Locale.forLanguageTag("es"))
        return date.format(formatter).replaceFirstChar { it.uppercase() }
    }

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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.isLoadingVehicle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryCyan)
                    }
                }
                viewModel.errorVehicle != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(viewModel.errorVehicle ?: "", color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadVehicle(carId) }) {
                            Text("Reintentar")
                        }
                    }
                }
                vehicle != null -> {
                    Column(
                        modifier = Modifier
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
                                val imageUrl = vehicle.primaryImageUrl ?: ""
                                if (imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp, 50.dp).clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                Column {
                                    Text("${vehicle.make} ${vehicle.model}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.DarkGray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Propietario #${vehicle.ownerId}", fontSize = 14.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rental Info Card with interactive dates
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showDatePicker(viewModel.startDate) { date ->
                                                viewModel.startDate = date
                                                if (viewModel.endDate.isBefore(date)) {
                                                    viewModel.endDate = date.plusDays(1)
                                                }
                                            }
                                        }
                                ) {
                                    RentalInfoItem(Icons.Default.CalendarMonth, "Recogida (Click para cambiar)", formatDisplayDate(viewModel.startDate))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showDatePicker(viewModel.endDate) { date ->
                                                if (date.isAfter(viewModel.startDate) || date.isEqual(viewModel.startDate)) {
                                                    viewModel.endDate = date
                                                }
                                            }
                                        }
                                ) {
                                    RentalInfoItem(Icons.Default.AccessTime, "Devolución (Click para cambiar)", formatDisplayDate(viewModel.endDate))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))

                                RentalInfoItem(Icons.Default.LocationOn, "Punto de encuentro", vehicle.location)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Cobertura", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Coverage Options
                        CoverageOption(
                            title = "Esencial",
                            subtitle = "Franquicia 1.500 €",
                            price = "S/ 0/día",
                            isSelected = viewModel.coveragePlan == "ESSENTIAL",
                            onClick = { viewModel.coveragePlan = "ESSENTIAL" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CoverageOption(
                            title = "Plus",
                            tag = "Popular",
                            subtitle = "Sin franquicia · Recomendada",
                            price = "S/ 8/día",
                            isSelected = viewModel.coveragePlan == "PLUS",
                            onClick = { viewModel.coveragePlan = "PLUS" }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CoverageOption(
                            title = "Premium",
                            subtitle = "Sin franquicia + asistencia ilimitada",
                            price = "S/ 14/día",
                            isSelected = viewModel.coveragePlan == "PREMIUM",
                            onClick = { viewModel.coveragePlan = "PREMIUM" }
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
                                PriceRow("Renta (S/ ${String.format("%.0f", vehicle.dailyPrice)} × ${viewModel.rentalDays} días)", "S/ ${String.format("%.2f", viewModel.subtotal)}")
                                PriceRow("Cobertura ${viewModel.coveragePlan.lowercase().replaceFirstChar { it.uppercase() }}", "S/ ${String.format("%.2f", viewModel.coverageTotal)}")
                                PriceRow("Tasa de servicio (5%)", "S/ ${String.format("%.2f", viewModel.serviceFee)}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text("Total", fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Color.Black)
                                    Text("S/ ${String.format("%.2f", viewModel.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (viewModel.errorMessage != null) {
                            Text(
                                text = viewModel.errorMessage ?: "",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.confirmAndPayBooking {
                                    showSuccessDialog = true
                                }
                            },
                            enabled = !viewModel.isSubmitting,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (viewModel.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Pagar y reservar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    containerColor = Color.White,
                    title = { Text("¡Reserva Confirmada!", fontWeight = FontWeight.Bold, color = Color.Black) },
                    text = {
                        Text(
                            text = "Tu reserva se ha registrado exitosamente en el sistema. Puedes visualizar los detalles en la pestaña 'Mis reservas'.",
                            color = Color.DarkGray
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onPaymentClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Entendido", color = Color.White)
                        }
                    }
                )
            }
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
