package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.edu.upc.rent2go_kotlin.common.DependencyProvider

/**
 * US41 (Renter) — dispute/report submission dialog for a reservation.
 * Category-free per the backend DTO (OpenReservationDisputeResource has no
 * category field) — reason/description text only, mirroring Flutter's Phase 6 UI.
 */
@Composable
fun DisputeDialog(
    reservationId: Int,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: DisputeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DisputeViewModel(DependencyProvider.communityRepository) as T
            }
        }
    )
) {
    var reason by remember { mutableStateOf("") }
    val state = viewModel.state.value

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onSubmitted()
        }
    }

    AlertDialog(
        modifier = Modifier.testTag("dispute_dialog"),
        onDismissRequest = {
            viewModel.resetState()
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Reportar un problema", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Text(
                    text = "Cuéntanos qué ocurrió con esta reserva. Nuestro equipo revisará tu reporte.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo del reporte") },
                    placeholder = { Text("Describe el problema...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("dispute_reason_input")
                )
                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("dispute_error_text")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.submitDispute(reservationId, reason) {} },
                enabled = !state.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("dispute_submit_button")
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onError, modifier = Modifier.size(18.dp))
                } else {
                    Text("Enviar reporte", color = MaterialTheme.colorScheme.onError)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.resetState()
                    onDismiss()
                },
                modifier = Modifier.testTag("dispute_cancel_button")
            ) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
