package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.ui.theme.DarkBlue
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.theme.TextGray
import pe.edu.upc.rent2go_kotlin.community.presentation.DisputeDialog
import pe.edu.upc.rent2go_kotlin.community.presentation.RatingDialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: Int,
    onBackClick: () -> Unit,
    onChatClick: (Int) -> Unit = {},
    viewModel: BookingDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return BookingDetailViewModel(
                    DependencyProvider.bookingRepository,
                    DependencyProvider.vehicleRepository
                ) as T
            }
        }
    )
) {
    val scrollState = rememberScrollState()

    // Bugfix (reportado por Renter): reutiliza el mismo contrato PaymentSheetRequest/
    // PaymentSheetOutcome de BookingConfirmationScreen para presentar Stripe PaymentSheet
    // al reintentar el pago de una reserva PENDING, en vez de duplicar la lógica de sheet.
    val paymentSheet = rememberPaymentSheet { result ->
        viewModel.onPaymentSheetResult(
            when (result) {
                is PaymentSheetResult.Completed -> PaymentSheetOutcome.Completed
                is PaymentSheetResult.Canceled -> PaymentSheetOutcome.Canceled
                is PaymentSheetResult.Failed -> PaymentSheetOutcome.Failed(
                    result.error.localizedMessage ?: result.error.message ?: "tarjeta rechazada"
                )
            },
            bookingId
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

    LaunchedEffect(bookingId) {
        viewModel.loadBookingDetail(bookingId)
    }

    val booking = viewModel.booking
    val vehicle = viewModel.vehicle

    // US41/US43 (Renter) — dispute/rating submission entry points for this reservation.
    var showDisputeDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var disputeSubmittedMessage by remember { mutableStateOf<String?>(null) }
    var ratingSubmittedMessage by remember { mutableStateOf<String?>(null) }

    if (showDisputeDialog && booking != null) {
        DisputeDialog(
            reservationId = booking.id,
            onDismiss = { showDisputeDialog = false },
            onSubmitted = {
                showDisputeDialog = false
                disputeSubmittedMessage = "Tu reporte fue enviado. Nuestro equipo lo revisará pronto."
            }
        )
    }

    if (showRatingDialog && booking != null) {
        RatingDialog(
            reservationId = booking.id,
            vehicleId = booking.vehicleId,
            reviewedUserId = booking.ownerId,
            onDismiss = { showRatingDialog = false },
            onSubmitted = {
                showRatingDialog = false
                ratingSubmittedMessage = "¡Gracias! Tu reseña fue enviada."
            }
        )
    }

    if (disputeSubmittedMessage != null) {
        AlertDialog(
            onDismissRequest = { disputeSubmittedMessage = null },
            containerColor = Color.White,
            title = { Text("Reporte enviado", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text(disputeSubmittedMessage ?: "", color = Color.DarkGray) },
            confirmButton = {
                TextButton(
                    onClick = { disputeSubmittedMessage = null },
                    modifier = Modifier.testTag("dispute_confirmation_ok_button")
                ) {
                    Text("Entendido", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (ratingSubmittedMessage != null) {
        AlertDialog(
            onDismissRequest = { ratingSubmittedMessage = null },
            containerColor = Color.White,
            title = { Text("Reseña enviada", fontWeight = FontWeight.Bold, color = Color.Black) },
            text = { Text(ratingSubmittedMessage ?: "", color = Color.DarkGray) },
            confirmButton = {
                TextButton(
                    onClick = { ratingSubmittedMessage = null },
                    modifier = Modifier.testTag("rating_confirmation_ok_button")
                ) {
                    Text("Entendido", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Reserva", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                viewModel.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryCyan)
                    }
                }
                viewModel.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(viewModel.error ?: "", color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBookingDetail(bookingId) }) {
                            Text("Reintentar")
                        }
                    }
                }
                booking != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        // ── Header: Status + Reservation Code ──
                        BookingDetailHeader(booking = booking)

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Vehicle Card ──
                        BookingDetailVehicleCard(vehicle = vehicle, booking = booking)

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Dates Card ──
                        BookingDetailDatesCard(booking = booking)

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Locations Card ──
                        BookingDetailLocationsCard(booking = booking)

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Issue 5: vehicle pickup coordinates map preview, so the renter can
                        // see exactly where to meet the owner before doing so. Vehicle.location
                        // above (Ubicaciones card) is a free-text address; this adds the actual
                        // lat/lng when the vehicle has them, mirroring ExploreScreen.kt's
                        // GoogleMap usage (same maps.compose dependency, read-only here). ──
                        if (vehicle?.latitude != null && vehicle.longitude != null) {
                            BookingDetailMapCard(latitude = vehicle.latitude, longitude = vehicle.longitude)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // ── Coverage Card ──
                        BookingDetailCoverageCard(booking = booking)

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Confirmation Timestamps ──
                        if (booking.pickupConfirmedAt != null || booking.returnConfirmedAt != null) {
                            BookingDetailConfirmationsCard(booking = booking)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // ── Amount Card ──
                        BookingDetailAmountCard(booking = booking)

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Pickup Photos ──
                        if (booking.pickupPhotos.isNotEmpty()) {
                            BookingDetailPhotosCard(
                                title = "Fotos de Recogida",
                                icon = Icons.Default.AddAPhoto,
                                photos = booking.pickupPhotos
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // ── Return Photos ──
                        if (booking.returnPhotos.isNotEmpty()) {
                            BookingDetailPhotosCard(
                                title = "Fotos de Devolución",
                                icon = Icons.Default.PhotoLibrary,
                                photos = booking.returnPhotos
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // ── Damage Report ──
                        if (!booking.damageReport.isNullOrBlank()) {
                            BookingDetailDamageCard(damageReport = booking.damageReport)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // ── Bugfix: Retry Payment (only when reservation is PENDING, i.e.
                        // created but payment not yet confirmed by Stripe's webhook) ──
                        if (booking.status == "PENDING") {
                            BookingDetailPaymentRetryCard(
                                isProcessing = viewModel.isProcessingPayment,
                                errorMessage = viewModel.paymentErrorMessage,
                                onRetryClick = { viewModel.retryPayment() }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // ── US41/US43 (Renter) — Rating & Dispute entry points ──
                        // Phase 8 (item 5) — chat entry point added alongside rate/report.
                        BookingDetailActionsCard(
                            status = booking.status,
                            onRateClick = { showRatingDialog = true },
                            onReportClick = { showDisputeDialog = true },
                            onChatClick = { viewModel.openChatForBooking { conversationId -> onChatClick(conversationId) } },
                            isOpeningChat = viewModel.isOpeningChat
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Section Composables
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun BookingDetailHeader(booking: pe.edu.upc.rent2go_kotlin.booking.domain.Booking) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Reserva #${booking.reservationCode}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${booking.id}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Surface(
            color = getStatusColor(booking.status).copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = getStatusText(booking.status),
                color = getStatusColor(booking.status),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun BookingDetailVehicleCard(
    vehicle: pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle?,
    booking: pe.edu.upc.rent2go_kotlin.booking.domain.Booking
) {
    val carName = if (vehicle != null) "${vehicle.make} ${vehicle.model}" else "Vehículo #${booking.vehicleId}"
    // Phase 8 (item 7) — fallback to the reservation's own vehicle_image when the vehicle
    // lookup by ID hasn't resolved yet.
    val imageUrl = vehicle?.primaryImageUrl ?: booking.vehicleImage ?: ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Text(carName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                if (vehicle != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${vehicle.categoryName} · ${vehicle.year}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Phase 8 (item 7) — owner's real profile photo when available,
                        // falling back to the generic account icon.
                        if (!booking.owner.profileImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = booking.owner.profileImageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.DarkGray
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        // TS18/US60 — real owner name (+ verification badges) instead of a raw ID.
                        Text(
                            booking.owner.fullName,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        // Phase 8 (item 7) — DNI/license badges alongside the existing KYC badge,
                        // per CounterpartyResource's dni_verified/license_verified split.
                        if (booking.owner.kycVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "KYC verificado",
                                modifier = Modifier.size(14.dp),
                                tint = PrimaryCyan
                            )
                        }
                        if (booking.owner.dniVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = "DNI verificado",
                                modifier = Modifier.size(14.dp),
                                tint = PrimaryCyan
                            )
                        }
                        if (booking.owner.licenseVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = "Licencia verificada",
                                modifier = Modifier.size(14.dp),
                                tint = PrimaryCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${vehicle.transmission} · ${vehicle.fuelType}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingDetailDatesCard(booking: pe.edu.upc.rent2go_kotlin.booking.domain.Booking) {
    val startFormatted = formatDateString(booking.startDate)
    val endFormatted = formatDateString(booking.endDate)
    val rentalDays = try {
        val start = LocalDate.parse(booking.startDate)
        val end = LocalDate.parse(booking.endDate)
        ChronoUnit.DAYS.between(start, end).coerceAtLeast(1).toInt()
    } catch (e: Exception) {
        null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Fechas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recogida", fontSize = 11.sp, color = Color.Gray)
                    Text(startFormatted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).align(Alignment.CenterVertically),
                    tint = PrimaryCyan
                )
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Devolución", fontSize = 11.sp, color = Color.Gray)
                    Text(endFormatted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }
            }
            if (rentalDays != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$rentalDays día${if (rentalDays != 1) "s" else ""} de renta",
                    fontSize = 12.sp,
                    color = PrimaryCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun BookingDetailLocationsCard(booking: pe.edu.upc.rent2go_kotlin.booking.domain.Booking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ubicaciones", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            DetailInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Recogida",
                value = booking.pickupLocation
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailInfoRow(
                icon = Icons.Default.Flag,
                label = "Devolución",
                value = booking.returnLocation
            )
        }
    }
}

/**
 * Issue 5 — read-only map preview of the vehicle's registered pickup coordinates, so the renter
 * can see where to go before meeting the owner. Reuses the same maps.compose GoogleMap/Marker
 * pattern already proven in ExploreScreen.kt (same dependency, no new API key/library needed);
 * disables gestures/UI controls here since this is a preview, not an interactive picker.
 */
@Composable
private fun BookingDetailMapCard(latitude: Double, longitude: Double) {
    val vehicleLatLng = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vehicleLatLng, 14f)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ubicación del vehículo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = false,
                        zoomGesturesEnabled = false,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    Marker(state = MarkerState(position = vehicleLatLng))
                }
            }
        }
    }
}

@Composable
private fun BookingDetailCoverageCard(booking: pe.edu.upc.rent2go_kotlin.booking.domain.Booking) {
    val coverageLabel = when (booking.coveragePlan) {
        "ESSENTIAL" -> "Esencial"
        "PLUS" -> "Plus"
        "PREMIUM" -> "Premium"
        else -> booking.coveragePlan
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cobertura", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = PrimaryCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(coverageLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = PrimaryCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        booking.coveragePlan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingDetailConfirmationsCard(booking: pe.edu.upc.rent2go_kotlin.booking.domain.Booking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Confirmaciones", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            if (booking.pickupConfirmedAt != null) {
                DetailInfoRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Recogida confirmada",
                    value = formatDateTimeString(booking.pickupConfirmedAt)
                )
            }
            if (booking.returnConfirmedAt != null) {
                if (booking.pickupConfirmedAt != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                DetailInfoRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Devolución confirmada",
                    value = formatDateTimeString(booking.returnConfirmedAt)
                )
            }
        }
    }
}

@Composable
private fun BookingDetailAmountCard(booking: pe.edu.upc.rent2go_kotlin.booking.domain.Booking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkBlue,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total pagado", fontSize = 16.sp, color = TextGray)
            Text(
                "S/ ${String.format("%.2f", booking.totalAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PrimaryCyan
            )
        }
    }
}

@Composable
private fun BookingDetailPhotosCard(
    title: String,
    icon: ImageVector,
    photos: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = PrimaryCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                photos.forEach { photoUrl ->
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingDetailDamageCard(damageReport: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFF56C6C)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Reporte de Daños",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(damageReport, fontSize = 13.sp, color = Color.DarkGray)
        }
    }
}

@Composable
private fun BookingDetailPaymentRetryCard(
    isProcessing: Boolean,
    errorMessage: String?,
    onRetryClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Esta reserva aún no tiene un pago confirmado",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, fontSize = 12.sp, color = Color(0xFFF56C6C))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetryClick,
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("retry_payment_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pagar ahora", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun BookingDetailActionsCard(
    status: String,
    onRateClick: () -> Unit,
    onReportClick: () -> Unit,
    onChatClick: () -> Unit = {},
    isOpeningChat: Boolean = false
) {
    Spacer(modifier = Modifier.height(12.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("¿Necesitas ayuda con esta reserva?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            // Phase 8 (item 5) — chat entry point for this reservation's counterparty.
            OutlinedButton(
                onClick = onChatClick,
                enabled = !isOpeningChat,
                modifier = Modifier.fillMaxWidth().testTag("chat_reservation_button")
            ) {
                if (isOpeningChat) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryCyan)
                } else {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar mensaje", color = PrimaryCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // US43 — rating is only offered once the rental has completed.
                if (status == "COMPLETED") {
                    Button(
                        onClick = onRateClick,
                        modifier = Modifier.weight(1f).testTag("rate_reservation_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calificar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onReportClick,
                    modifier = Modifier.weight(1f).testTag("report_reservation_button")
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF56C6C), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reportar problema", color = Color(0xFFF56C6C), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Shared Helpers
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.DarkGray)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}

private fun formatDateString(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.forLanguageTag("es"))
        date.format(formatter).replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatDateTimeString(dateTimeStr: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(dateTimeStr)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es"))
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        try {
            val localDateTime = LocalDateTime.parse(dateTimeStr)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es"))
            localDateTime.format(formatter)
        } catch (e2: Exception) {
            dateTimeStr
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
