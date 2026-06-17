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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg

@Composable
fun MessagesScreen(
    onChatClick: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Activos", "Sin leer")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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

        // Filters
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            items(mockChats) { chat ->
                MessageItem(chat = chat, onClick = { onChatClick(chat.userName) })
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.05f))
            }
        }
    }
}

@Composable
fun MessageItem(chat: ChatSummary, onClick: () -> Unit) {
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
            Text(text = chat.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            if (chat.carName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = chat.carName, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Text(
                text = chat.lastMessage,
                fontSize = 14.sp,
                color = if (chat.isUnread) Color.Black else Color.DarkGray,
                maxLines = 1,
                fontWeight = if (chat.isUnread) FontWeight.Bold else FontWeight.Normal
            )
        }
        
        if (chat.showCheck) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
    }
}

data class ChatSummary(
    val userName: String,
    val carName: String? = null,
    val lastMessage: String,
    val isUnread: Boolean = false,
    val showCheck: Boolean = false
)

val mockChats = listOf(
    ChatSummary("Lucía M.", "Tesla Model 3", "Perfecto, te espero a las 10h en Goya 24", showCheck = true),
    ChatSummary("Andrés R.", "Mini Cooper", "Tú: Gracias por todo, gran coche!"),
    ChatSummary("Soporte Rent2Go", null, "Hemos actualizado tu cobertura.", isUnread = true),
    ChatSummary("Carla V.", "BMW Serie 1", "¿Te viene bien recogerlo a las 9?"),
    ChatSummary("Marco T.", "VW Golf", "¡Buen viaje!")
)
