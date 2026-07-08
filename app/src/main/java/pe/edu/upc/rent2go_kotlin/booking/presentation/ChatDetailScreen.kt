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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upc.rent2go_kotlin.common.ui.theme.LightBlueBg
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.community.domain.ChatMessage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: Int,
    reservationId: Int?,
    userName: String,
    onBackClick: () -> Unit,
    viewModel: ChatDetailViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId, reservationId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Phase 8 (item 7) — counterparty's real profile photo, replacing the
                        // placeholder Person icon-only avatar.
                        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.LightGray) {
                            if (!viewModel.counterpartyProfileImageUrl.isNullOrBlank()) {
                                coil.compose.AsyncImage(
                                    model = viewModel.counterpartyProfileImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            // TS18/US60 — real counterparty name once the conversation loads;
                            // falls back to the nav-time placeholder ("Conversación") only
                            // during the brief initial load, never to a raw ID.
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = viewModel.counterpartyName ?: userName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                if (viewModel.counterpartyKycVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verificado", modifier = Modifier.size(14.dp), tint = PrimaryCyan)
                                }
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
                    // El endpoint POST .../messages solo acepta 'content' de texto
                    // (SendMessageResource), por lo que adjuntar archivos queda
                    // deshabilitado explícitamente en vez de ser un no-op silencioso.
                    IconButton(onClick = {}, enabled = false) {
                        Icon(Icons.Default.Add, contentDescription = "Adjuntar (próximamente)", tint = Color.Gray)
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
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(conversationId, messageText)
                                messageText = ""
                            }
                        },
                        enabled = !viewModel.sending
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.Black)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
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
                            TextButton(onClick = { viewModel.loadMessages(conversationId, reservationId) }) { Text("Reintentar") }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // F/K: la tarjeta de resumen solo se muestra si la reserva
                        // real fue cargada (reservationId no nulo Y la carga tuvo
                        // éxito) — si es null, se omite en vez de mostrar datos falsos.
                        viewModel.reservationSummary?.let { booking ->
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.White.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(modifier = Modifier.size(60.dp, 40.dp), color = Color.LightGray, shape = RoundedCornerShape(4.dp)) {
                                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.padding(8.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Reserva ${booking.reservationCode}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${booking.startDate} → ${booking.endDate}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Surface(color = PrimaryCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                            Text(booking.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = PrimaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (viewModel.messages.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                    Text("Aún no hay mensajes. ¡Escribe el primero!", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }

                        // Phase 8 — date separators ("Hoy"/"Ayer"/dd/MM/yyyy) inserted whenever
                        // the calendar day changes between consecutive messages, mirroring
                        // Flutter's chat_screen.dart _dateSeparatorLabel convention.
                        var lastDateLabel: String? = null
                        viewModel.messages.forEach { message ->
                            val dateLabel = dateSeparatorLabel(message.createdAt)
                            if (dateLabel != null && dateLabel != lastDateLabel) {
                                lastDateLabel = dateLabel
                                item(key = "date_${dateLabel}_${message.id}") {
                                    ChatDateSeparator(label = dateLabel)
                                }
                            }
                            item(key = message.id) {
                                ChatBubble(message = message, isFromMe = message.senderId == viewModel.currentUserId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isFromMe: Boolean) {
    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isFromMe) Color.Black else Color.White.copy(alpha = 0.6f)
    val textColor = if (isFromMe) Color.White else Color.Black
    val timeLabel = formatMessageTime(message.createdAt)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 14.sp
                )
                if (timeLabel != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeLabel,
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatDateSeparator(label: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            color = Color.Black.copy(alpha = 0.08f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

/** Per-message HH:mm timestamp, tolerant of both zoned and local ISO datetime strings. */
private fun formatMessageTime(isoDateTime: String?): String? {
    if (isoDateTime.isNullOrBlank()) return null
    return try {
        ZonedDateTime.parse(isoDateTime).format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        try {
            LocalDateTime.parse(isoDateTime).format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e2: Exception) {
            null
        }
    }
}

/** "Hoy" / "Ayer" / "dd/MM/yyyy" date-separator label, mirroring Flutter's chat_screen.dart. */
private fun dateSeparatorLabel(isoDateTime: String?): String? {
    if (isoDateTime.isNullOrBlank()) return null
    val messageDate: LocalDate = try {
        ZonedDateTime.parse(isoDateTime).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
    } catch (e: Exception) {
        try {
            LocalDateTime.parse(isoDateTime).toLocalDate()
        } catch (e2: Exception) {
            return null
        }
    }
    val today = LocalDate.now()
    return when (today.toEpochDay() - messageDate.toEpochDay()) {
        0L -> "Hoy"
        1L -> "Ayer"
        else -> messageDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es")))
    }
}
