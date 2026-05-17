package pe.edu.upc.rent2go_kotlin.booking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
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
import coil.compose.AsyncImage
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    userName: String,
    onBackClick: () -> Unit
) {
    var messageList by remember { mutableStateOf(chatMessages) }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.LightGray) {
                            Icon(Icons.Default.Person, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = userName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "En línea", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Atras", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.8f))
            )
        },
        containerColor = LightBlueBg,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                color = Color.White.copy(alpha = 0.8f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    }
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Escribe un mensaje...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.05f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    IconButton(onClick = { 
                        if (messageText.isNotBlank()) {
                            messageList = messageList + ChatMessage(messageText, true)
                            messageText = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://platform.cstatic-images.com/xlarge/in/v2/stock_photos/06981146-24e5-472e-8344-9040d2165249/098797f1-8404-45e0-9944-80226c6d0426.png",
                            contentDescription = null,
                            modifier = Modifier.size(60.dp, 40.dp).clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tesla Model 3", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("12 May 10:00 → 14 May 18:00", fontSize = 12.sp, color = Color.Gray)
                        }
                        Surface(color = PrimaryCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                            Text("Confirmada", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = PrimaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("HOY · 14:18", fontSize = 10.sp, color = Color.Gray)
                }
            }

            items(messageList) { message ->
                ChatBubble(message)
            }
            
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Visto · 14:22",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isFromMe) Color.Black else Color.White.copy(alpha = 0.6f)
    val textColor = if (message.isFromMe) Color.White else Color.Black

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

data class ChatMessage(
    val text: String,
    val isFromMe: Boolean
)

val chatMessages = listOf(
    ChatMessage("¡Hola Diego! ¿Confirmamos la recogida en Goya 24 a las 10:00?", false),
    ChatMessage("¡Hola Lucia! Sí, perfecto. Voy a ir en metro hasta Goya.", true),
    ChatMessage("Genial. Te dejo el coche con carga completa (498 km).", false),
    ChatMessage("Perfecto, te espero a las 10h en Goya 24", false)
)
