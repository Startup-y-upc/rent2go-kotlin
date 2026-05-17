package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import pe.edu.upc.rent2go_kotlin.catalog.domain.Car

import pe.edu.upc.rent2go_kotlin.common.ui.theme.CardLight
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan

@Composable
fun ExploreScreen(
    onCarClick: (Int) -> Unit,
    viewModel: CarListViewModel = viewModel()
) {
    val state = viewModel.state.value
    val madrid = LatLng(40.4168, -3.7038)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(madrid, 12f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg)
    ) {
        // Google Maps Integration (Top Half)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes half of the available space
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Add markers for cars
                Marker(
                    state = MarkerState(position = LatLng(40.4189, -3.7025)),
                    title = "Tesla Model 3"
                )
                Marker(
                    state = MarkerState(position = LatLng(40.4215, -3.7088)),
                    title = "Mini Cooper S"
                )
            }

            // Search Bar (Over the map)
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Madrid · Centro", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Mar 12 May → Jue 14 May", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Black)
                }
            }
        }

        // Bottom Car Carousel and Space (Bottom Half)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes the other half
                .padding(top = 16.dp)
        ) {
            Text(
                text = "42 coches cerca",
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.cars) { car ->
                    ExploreCarCard(car = car, onClick = { onCarClick(car.id) })
                }
            }
            
            // Spacer for the bottom navigation bar height
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
        }

        if (state.error.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp)).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ExploreCarCard(car: Car, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = CardLight
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = car.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${car.brand} ${car.model}", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "${car.type} · ${car.transmission}", fontSize = 12.sp, color = Color.Gray)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(24.dp), shape = CircleShape, color = Color.LightGray) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = car.ownerName.split(" ")[0], fontSize = 12.sp, color = Color.Black)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text(text = car.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.Black else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Box(modifier = Modifier.padding(top = 2.dp).width(20.dp).height(2.dp).background(PrimaryCyan))
        }
    }
}
