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
                        ConversationItem(conversation = conversation, onClick = { onChatClick(conversation.id) })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = Color.LightGray
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = conversation.subject ?: "Conversación #${conversation.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
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
    }
}
