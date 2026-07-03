package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import pe.edu.upc.rent2go_kotlin.common.ui.theme.PrimaryCyan
import pe.edu.upc.rent2go_kotlin.community.domain.ReviewCategory

/**
 * US43 (Renter) — rating/review submission dialog for a completed reservation.
 * Star rating (1-5) + category picker + optional comment, per the backend DTO.
 */
@Composable
fun RatingDialog(
    reservationId: Int,
    vehicleId: Int,
    reviewedUserId: Int?,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: RatingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return RatingViewModel(DependencyProvider.communityRepository) as T
            }
        }
    )
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ReviewCategory.RENTAL_EXPERIENCE) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val state = viewModel.state.value

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onSubmitted()
        }
    }

    AlertDialog(
        modifier = Modifier.testTag("rating_dialog"),
        onDismissRequest = {
            viewModel.resetState()
            onDismiss()
        },
        containerColor = Color.White,
        title = { Text("Califica tu experiencia", fontWeight = FontWeight.Bold, color = Color.Black) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("rating_stars_row"),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (star in 1..5) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "$star estrella${if (star != 1) "s" else ""}",
                            tint = if (star <= rating) PrimaryCyan else Color.Gray,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = star }
                                .testTag("rating_star_$star")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Categoría", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { categoryMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("rating_category_selector")
                    ) {
                        Text(category.label, color = Color.Black)
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        ReviewCategory.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    category = option
                                    categoryMenuExpanded = false
                                },
                                modifier = Modifier.testTag("rating_category_option_${option.name}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario (opcional)") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("rating_comment_input")
                )

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("rating_error_text")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitRating(
                        reservationId = reservationId,
                        vehicleId = vehicleId,
                        reviewedUserId = reviewedUserId,
                        category = category,
                        rating = rating,
                        comment = comment
                    ) {}
                },
                enabled = !state.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                modifier = Modifier.testTag("rating_submit_button")
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("Enviar reseña", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.resetState()
                    onDismiss()
                },
                modifier = Modifier.testTag("rating_cancel_button")
            ) {
                Text("Cancelar", color = Color.Black)
            }
        }
    )
}
