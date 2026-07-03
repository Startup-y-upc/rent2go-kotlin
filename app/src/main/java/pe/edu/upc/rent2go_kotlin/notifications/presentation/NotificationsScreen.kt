package pe.edu.upc.rent2go_kotlin.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.notifications.domain.AppNotification
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * US50/US51/US52 (Renter) — in-app notification feed, mirroring Flutter's
 * Phase 8 screen. In-app-only (no push): list is loaded on screen open.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return NotificationsViewModel(DependencyProvider.notificationRepository) as T
            }
        }
    )
) {
    val state = viewModel.state.value

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        modifier = Modifier.testTag("notifications_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("notifications_back_button")
                    ) {
                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Volver", tint = Color.Black)
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
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().testTag("notifications_loading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryCyan)
                    }
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp).testTag("notifications_error"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.error, color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadNotifications() },
                            modifier = Modifier.testTag("notifications_retry_button")
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                state.notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp).testTag("notifications_empty"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tienes notificaciones por ahora.",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("notifications_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { viewModel.markAsRead(notification.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !notification.isRead) { onClick() }
            .testTag("notification_item_${notification.id}"),
        color = if (notification.isRead) Color.White.copy(alpha = 0.5f) else Color.White,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = if (notification.isRead) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // WCAG AA: read/unread is conveyed via icon + text label, not color alone.
            Icon(
                imageVector = if (notification.isRead) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                contentDescription = if (notification.isRead) "Leída" else "No leída",
                tint = if (notification.isRead) Color.Gray else PrimaryCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notificationTypeLabel(notification.type),
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Text(
                        text = if (notification.isRead) "Leída" else "No leída",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (notification.isRead) Color.Gray else PrimaryCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatNotificationDate(notification.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun notificationTypeLabel(type: String): String {
    return when (type) {
        "RESERVATION_STATUS" -> "Estado de reserva"
        "DISPUTE" -> "Reporte"
        "REVIEW" -> "Reseña"
        "MESSAGE" -> "Mensaje"
        "PAYMENT" -> "Pago"
        else -> type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun formatNotificationDate(dateStr: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es"))
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        try {
            val localDateTime = LocalDateTime.parse(dateStr)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es"))
            localDateTime.format(formatter)
        } catch (e2: Exception) {
            dateStr
        }
    }
}
