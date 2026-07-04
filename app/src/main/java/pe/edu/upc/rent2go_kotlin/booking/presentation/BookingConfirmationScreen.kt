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
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
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

    // US58/TS16 — presents Stripe's PaymentSheet once the ViewModel has a real client secret
    // from the backend's PaymentIntent; the sheet's own result (success/decline/cancel) is the
    // only thing allowed to mark the payment complete, not merely creating the intent.
    val paymentSheet = rememberPaymentSheet { result ->
        viewModel.onPaymentSheetResult(
            when (result) {
                is PaymentSheetResult.Completed -> PaymentSheetOutcome.Completed
                is PaymentSheetResult.Canceled -> PaymentSheetOutcome.Canceled
                is PaymentSheetResult.Failed -> PaymentSheetOutcome.Failed(
                    result.error.localizedMessage ?: result.error.message ?: "tarjeta rechazada"
                )
            }
        )
    }

    LaunchedEffect(viewModel.paymentSheetRequest) {
        val request = viewModel.paymentSheetRequest
        if (request is PaymentSheetRequest.Ready) {
            paymentSheet.presentWithPaymentIntent(
                request.clientSecret,
                PaymentSheet.Configuration(merchantDisplayName = "Rent2Go")
            )
            viewModel.onPaymentSheetLaunched()
        }
    }

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
                                                viewModel.checkAvailability()
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
                                                viewModel.checkAvailability()
                                            }
                                        }
                                ) {
                                    RentalInfoItem(Icons.Default.AccessTime, "Devolución (Click para cambiar)", formatDisplayDate(viewModel.endDate))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.1f))

                                RentalInfoItem(Icons.Default.LocationOn, "Punto de encuentro", vehicle.location)
                            }
                        }

                        // US15 (Renter, read-only) — availability feedback for the selected range.
                        Spacer(modifier = Modifier.height(12.dp))
                        when {
                            viewModel.isCheckingAvailability -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verificando disponibilidad…", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            !viewModel.isRangeAvailable -> {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "El vehículo ya está reservado en parte de este rango de fechas.",
                                                fontSize = 12.sp,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (viewModel.blockedRanges.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            viewModel.blockedRanges.forEach { (start, end) ->
                                                Text("Ocupado: $start a $end", fontSize = 11.sp, color = Color(0xFFE65100))
                                            }
                                        }
                                    }
                                }
                            }
                            viewModel.availabilityError != null -> {
                                Text(
                                    "No se pudo verificar la disponibilidad. Puedes continuar, se validará al confirmar.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Cobertura", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        // K8: opciones reales desde GET /payments/coverage-plans
                        // (BASIC/STANDARD/PREMIUM/NONE), ya no códigos/precios inventados.
                        if (viewModel.isLoadingCoveragePlans) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryCyan)
                            }
                        } else if (viewModel.coveragePlans.isEmpty()) {
                            Text("No se pudieron cargar los planes de cobertura.", fontSize = 12.sp, color = Color.Red)
                        } else {
                            viewModel.coveragePlans.forEachIndexed { index, plan ->
                                CoverageOption(
                                    title = plan.name,
                                    tag = if (plan.code == "STANDARD") "Popular" else null,
                                    subtitle = plan.description,
                                    price = if (plan.dailyRateUsd == 0.0) "S/ 0" else "S/ ${String.format("%.2f", plan.dailyRateUsd)}/día",
                                    isSelected = viewModel.coveragePlan == plan.code,
                                    onClick = { viewModel.coveragePlan = plan.code }
                                )
                                if (index < viewModel.coveragePlans.lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Price Breakdown Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val coverageName = viewModel.coveragePlans.firstOrNull { it.code == viewModel.coveragePlan }?.name ?: viewModel.coveragePlan
                                PriceRow("Renta (S/ ${String.format("%.0f", vehicle.dailyPrice)} × ${viewModel.rentalDays} días)", "S/ ${String.format("%.2f", viewModel.subtotal)}")
                                PriceRow("Cobertura $coverageName", "S/ ${String.format("%.2f", viewModel.coverageTotal)}")
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
                            enabled = !viewModel.isSubmitting && viewModel.isRangeAvailable,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (viewModel.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else if (!viewModel.isRangeAvailable) {
                                Text("Fechas no disponibles", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
