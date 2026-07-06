package pe.edu.upc.rent2go_kotlin.catalog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upc.rent2go_kotlin.booking.presentation.BookingsScreen
import pe.edu.upc.rent2go_kotlin.booking.presentation.MessagesScreen
import pe.edu.upc.rent2go_kotlin.booking.presentation.MessagesViewModel
import pe.edu.upc.rent2go_kotlin.iam.presentation.AuthViewModel
import pe.edu.upc.rent2go_kotlin.community.presentation.ProfileScreen
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan

@Composable
fun MainDashboard(
    authViewModel: AuthViewModel,
    onCarClick: (Int) -> Unit,
    onChatClick: (Int) -> Unit,
    onLogoutClick: () -> Unit,
    onBookingClick: (Int) -> Unit = {},
    onKycClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    var selectedScreen by remember { mutableStateOf("Explorar") }

    // Phase 8 (item 6) — shared MessagesViewModel instance so the bottom-nav badge total
    // reflects the same client-derived unread counts (per-conversation) used by
    // MessagesScreen's own list badges, without a second independent fetch mechanism.
    val messagesViewModel: MessagesViewModel = viewModel()
    LaunchedEffect(Unit) {
        messagesViewModel.loadConversations()
    }
    val totalUnreadCount = messagesViewModel.unreadCountsByConversation.values.sum()

    Box(modifier = Modifier.fillMaxSize()) {
        // Content area
        when (selectedScreen) {
            "Explorar" -> ExploreScreen(onCarClick = onCarClick)
            "Reservas" -> BookingsScreen(onBookingClick = onBookingClick)
            "Mensajes" -> MessagesScreen(onChatClick = onChatClick, viewModel = messagesViewModel)
            "Perfil" -> ProfileScreen(
                authViewModel = authViewModel,
                onLogoutClick = onLogoutClick,
                onKycClick = onKycClick,
                onTermsClick = onTermsClick,
                onNotificationsClick = onNotificationsClick
            )
        }

        // Bottom Navigation Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = if (selectedScreen == "Explorar") Icons.Default.Search else Icons.Default.Search,
                    label = "Explorar",
                    isSelected = selectedScreen == "Explorar",
                    onClick = { selectedScreen = "Explorar" }
                )
                BottomNavItem(
                    icon = if (selectedScreen == "Reservas") Icons.Default.CalendarMonth else Icons.Default.CalendarMonth,
                    label = "Reservas",
                    isSelected = selectedScreen == "Reservas",
                    onClick = { selectedScreen = "Reservas" }
                )
                BottomNavItem(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "Mensajes",
                    isSelected = selectedScreen == "Mensajes",
                    onClick = { selectedScreen = "Mensajes" },
                    badgeCount = totalUnreadCount
                )
                BottomNavItem(
                    icon = Icons.Outlined.PersonOutline,
                    label = "Perfil",
                    isSelected = selectedScreen == "Perfil",
                    onClick = { selectedScreen = "Perfil" }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(60.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Phase 8 (item 6) — client-derived unread badge on the bottom-nav icon.
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .defaultMinSize(minWidth = 14.dp, minHeight = 14.dp)
                            .background(PrimaryCyan, shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) Color.Black else Color.Gray
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(20.dp)
                        .height(2.dp)
                        .background(PrimaryCyan)
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla de $title", fontSize = 20.sp, color = Color.Black)
    }
}
