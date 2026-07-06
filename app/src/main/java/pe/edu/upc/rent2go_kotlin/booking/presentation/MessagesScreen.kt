package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.community.domain.Conversation

@Composable
fun MessagesScreen(
    onChatClick: (Int) -> Unit,
    viewModel: MessagesViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadConversations()
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
                text = "Mensajes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            viewModel.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            viewModel.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.errorMessage ?: "", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadConversations() }) { Text("Reintentar") }
                    }
                }
            }
            viewModel.conversations.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Todavía no tienes conversaciones", color = Color.Gray)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp)
                ) {
                    items(viewModel.conversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            unreadCount = viewModel.unreadCountsByConversation[conversation.id] ?: 0,
                            onClick = { onChatClick(conversation.id) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: Conversation, unreadCount: Int = 0, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Phase 8 (item 7) — counterparty's real profile photo when available, replacing
        // the placeholder Person icon-only avatar.
        val isCurrentUserOwnerForAvatar = pe.edu.upc.rent2go_kotlin.common.SessionManager.getUserId() == conversation.ownerId
        val counterpartyForAvatar = if (isCurrentUserOwnerForAvatar) conversation.renter else conversation.owner
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = Color.LightGray
        ) {
            if (!counterpartyForAvatar.profileImageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = counterpartyForAvatar.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // TS18/US60 — real counterparty name instead of the free-text subject / raw
            // conversation ID fallback. Perspective-aware: shows the *other* party.
            val isCurrentUserOwner = pe.edu.upc.rent2go_kotlin.common.SessionManager.getUserId() == conversation.ownerId
            val counterparty = if (isCurrentUserOwner) conversation.renter else conversation.owner
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = counterparty.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                if (counterparty.kycVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = "Verificado", modifier = Modifier.size(13.dp), tint = Color(0xFF00E5FF))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Vehículo #${conversation.vehicleId}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = conversation.lastMessagePreview ?: "Sin mensajes todavía",
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 1
            )
        }

        // Phase 8 (item 6) — client-derived unread badge (see MessagesViewModel).
        if (unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = PrimaryCyan,
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
