package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.catalog.domain.Vehicle
import pe.edu.upc.rent2go_kotlin.catalog.domain.VehicleFilters
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.ui.theme.CardLight
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.common.ui.map.MapLatLng
import pe.edu.upc.rent2go_kotlin.common.ui.map.MapMarker
import pe.edu.upc.rent2go_kotlin.common.ui.map.OsmMapView
import pe.edu.upc.rent2go_kotlin.common.ui.map.rememberMapCameraState
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    onCarClick: (Int) -> Unit,
    viewModel: VehicleListViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return VehicleListViewModel(DependencyProvider.vehicleRepository) as T
            }
        }
    )
) {
    val state = viewModel.state.value
    var showFilterSheet by remember { mutableStateOf(false) }

    // TS19 — geo-radius search: a long-press on the map drops a pin and searches within
    // radiusKm of it, using the same centerLatitude/centerLongitude/radiusKm backend params
    // Flutter's map/radius search uses (VehicleController.java — no contract difference).
    var radiusCenter by remember { mutableStateOf<MapLatLng?>(null) }
    var radiusKm by remember { mutableStateOf(10f) }

    val lima = MapLatLng(-12.046374, -77.042793)
    val cameraState = rememberMapCameraState(initialCenter = lima, initialZoom = 11.0)
    // TASK 4 follow-up (2026-07-06): the previous mapLoadFailed/retry fallback existed
    // to work around blank Google Maps tiles caused by the invalid placeholder
    // MAPS_API_KEY. Migrating to OSMDroid (no API key required) removes that failure
    // mode entirely, so the fallback UI and its dead mapLoadFailed state (it was never
    // actually set to true anywhere — GoogleMap does not expose a failure callback)
    // have been removed rather than carried forward.
    val coroutineScope = rememberCoroutineScope()

    val firstVehicleWithLocation = state.vehicles.firstOrNull { it.latitude != null && it.longitude != null }
    LaunchedEffect(firstVehicleWithLocation) {
        if (firstVehicleWithLocation != null && radiusCenter == null) {
            cameraState.moveTo(
                MapLatLng(firstVehicleWithLocation.latitude!!, firstVehicleWithLocation.longitude!!),
                13.0
            )
        }
    }

    // Recargar cada vez que la pantalla entra en composición (ej: al cambiar de tab)
    LaunchedEffect(Unit) {
        viewModel.loadFirstPage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg)
            .statusBarsPadding()
    ) {
        // Search Bar — US26: free-text search by make/model/location
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar por marca, modelo o ubicación", fontSize = 13.sp) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda", tint = Color.Gray)
                            }
                        }
                    }
                )
                // US27: open the filter sheet. A filled badge indicates active filters.
                Box {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Filtros",
                            tint = if (!state.filters.isEmpty) PrimaryCyan else Color.Black
                        )
                    }
                    if (!state.filters.isEmpty) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .background(PrimaryCyan, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.loadFirstPage() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val vehicleMarkers = remember(state.vehicles, radiusCenter, radiusKm) {
                buildList {
                    state.vehicles.forEach { vehicle ->
                        if (vehicle.latitude != null && vehicle.longitude != null) {
                            add(
                                MapMarker(
                                    position = MapLatLng(vehicle.latitude, vehicle.longitude),
                                    title = "${vehicle.make} ${vehicle.model}",
                                    snippet = "S/ ${String.format("%.0f", vehicle.dailyPrice)}/día"
                                )
                            )
                        }
                    }
                    radiusCenter?.let { center ->
                        add(
                            MapMarker(
                                position = center,
                                title = "Buscar aquí",
                                snippet = "Radio: ${radiusKm.toInt()} km"
                            )
                        )
                    }
                }
            }
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                markers = vehicleMarkers,
                onMapLongClick = { point -> radiusCenter = point }
            )
            if (radiusCenter == null) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Mantén presionado el mapa para buscar por zona",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // TS19 — radius control row: only shown once a search pin has been dropped.
        if (radiusCenter != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Radio de búsqueda: ${radiusKm.toInt()} km", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 1f..10f,
                        colors = SliderDefaults.colors(thumbColor = PrimaryCyan, activeTrackColor = PrimaryCyan)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            radiusCenter = null
                            viewModel.applyFilters(state.filters.copy(centerLatitude = null, centerLongitude = null, radiusKm = null))
                        }) {
                            Text("Quitar zona")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val center = radiusCenter ?: return@Button
                                viewModel.applyFilters(
                                    state.filters.copy(
                                        centerLatitude = center.latitude,
                                        centerLongitude = center.longitude,
                                        radiusKm = radiusKm.toDouble()
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                        ) {
                            Text("Buscar en esta zona")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vehicle Grid
        if (state.vehicles.isNotEmpty()) {
            val gridState = rememberLazyGridState()

            // Infinite scroll: detect when near the end and load more
            LaunchedEffect(gridState) {
                snapshotFlow {
                    val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val totalItems = gridState.layoutInfo.totalItemsCount
                    lastVisible to totalItems
                }.collect { (lastVisible, totalItems) ->
                    if (totalItems > 0
                        && lastVisible >= totalItems - 4
                        && state.hasMorePages
                        && !state.isLoadingMore
                        && !state.isLoading
                    ) {
                        viewModel.loadNextPage()
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                items(state.vehicles) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onClick = {
                            // Phase 7 — recenter the map camera on the selected vehicle's
                            // coordinates before navigating away. Does NOT hide/filter any
                            // other marker — every vehicle marker stays visible, only the
                            // camera moves.
                            if (vehicle.latitude != null && vehicle.longitude != null) {
                                coroutineScope.launch {
                                    cameraState.animateTo(
                                        MapLatLng(vehicle.latitude, vehicle.longitude),
                                        15.0
                                    )
                                }
                            }
                            onCarClick(vehicle.id)
                        }
                    )
                }

                // Loading indicator at bottom
                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = PrimaryCyan
                            )
                        }
                    }
                }
            }
        } else if (!state.isLoading) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay vehículos disponibles",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Loading state
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
        }

        // Error state
        if (state.error.isNotBlank()) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                action = {
                    TextButton(onClick = { viewModel.loadFirstPage() }) {
                        Text("Reintentar", color = PrimaryCyan)
                    }
                }
            ) {
                Text(state.error)
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            initialFilters = state.filters,
            onDismiss = { showFilterSheet = false },
            onApply = { filters ->
                viewModel.applyFilters(filters)
                showFilterSheet = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilterSheet = false
            }
        )
    }
}

/**
 * US27 — Filtrar vehículos por criterios (precio, asientos, transmisión, combustible).
 * Forwards directly to the existing backend query params — no backend change required.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    initialFilters: VehicleFilters,
    onDismiss: () -> Unit,
    onApply: (VehicleFilters) -> Unit,
    onClear: () -> Unit
) {
    var minPrice by remember { mutableStateOf(initialFilters.minPrice?.toInt()?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(initialFilters.maxPrice?.toInt()?.toString() ?: "") }
    var seats by remember { mutableStateOf(initialFilters.seats?.toString() ?: "") }
    var transmission by remember { mutableStateOf(initialFilters.transmission) }
    var fuelType by remember { mutableStateOf(initialFilters.fuelType) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                // US63 — the sheet's content can grow taller than the screen (long fuel-chip
                // wrap, small devices); without this it would previously overflow/clip.
                .verticalScroll(rememberScrollState())
        ) {
            Text("Filtrar vehículos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))

            FilterSectionLabel("Precio por día (S/)")
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { minPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("Mínimo") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("Máximo") },
                    modifier = Modifier.weight(1f)
                )
            }

            FilterSectionDivider()

            FilterSectionLabel("Asientos mínimos")
            OutlinedTextField(
                value = seats,
                onValueChange = { seats = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                placeholder = { Text("Ej. 4") }
            )

            FilterSectionDivider()

            FilterSectionLabel("Transmisión")
            Row(modifier = Modifier.padding(top = 8.dp)) {
                listOf("MANUAL", "AUTOMATIC").forEach { option ->
                    FilterChip(
                        selected = transmission == option,
                        onClick = { transmission = if (transmission == option) null else option },
                        label = { Text(if (option == "MANUAL") "Manual" else "Automática") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            FilterSectionDivider()

            FilterSectionLabel("Combustible")
            // US63 — fuel chips now wrap onto multiple lines instead of a non-wrapping Row,
            // which used to clip/overflow on narrower screens with all 4 options visible.
            FlowRow(
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("GASOLINE", "DIESEL", "ELECTRIC", "HYBRID").forEach { option ->
                    FilterChip(
                        selected = fuelType == option,
                        onClick = { fuelType = if (fuelType == option) null else option },
                        label = { Text(option.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        onApply(
                            VehicleFilters(
                                minPrice = minPrice.toDoubleOrNull(),
                                maxPrice = maxPrice.toDoubleOrNull(),
                                seats = seats.toIntOrNull(),
                                transmission = transmission,
                                fuelType = fuelType,
                                // TS19 — preserve any active geo-radius search when applying
                                // the other filters, so they combine instead of overwriting it.
                                centerLatitude = initialFilters.centerLatitude,
                                centerLongitude = initialFilters.centerLongitude,
                                radiusKm = initialFilters.radiusKm
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Aplicar")
                }
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    // Phase 9 (US77) — theme-derived instead of hardcoded Color.Black.
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun FilterSectionDivider() {
    // Phase 9 (US77) — theme-derived instead of hardcoded Color.Black.
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = CardLight
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (vehicle.primaryImageUrl != null) {
                AsyncImage(
                    model = vehicle.primaryImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${vehicle.make} ${vehicle.model}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${vehicle.year} · ${vehicle.transmission}",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = PrimaryCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = vehicle.categoryName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = PrimaryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "S/ ${String.format("%.0f", vehicle.dailyPrice)}/día",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalGasStation,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = vehicle.fuelType,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = vehicle.location,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
