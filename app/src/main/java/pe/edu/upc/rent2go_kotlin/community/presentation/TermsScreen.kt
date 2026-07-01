package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private sealed class TermsUiState {
    object Loading : TermsUiState()
    data class Success(val content: String) : TermsUiState()
    object Error : TermsUiState()
}

/**
 * Reads and displays the canonical Terms & Conditions content, bundled as a
 * static asset (assets/legal/terms-and-conditions.md) — no network fetch,
 * no backend endpoint. Canonical source: docs/legal/terms-and-conditions.md
 * (see docs/legal/check-parity.md before editing that source).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var state by remember { mutableStateOf<TermsUiState>(TermsUiState.Loading) }
    var reloadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(reloadTrigger) {
        state = TermsUiState.Loading
        state = try {
            val text = context.assets.open("legal/terms-and-conditions.md")
                .bufferedReader()
                .use { it.readText() }
            TermsUiState.Success(text)
        } catch (e: Exception) {
            TermsUiState.Error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y Condiciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val current = state) {
                is TermsUiState.Loading -> CircularProgressIndicator()
                is TermsUiState.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null)
                    Text("No se pudo cargar el contenido de Términos y Condiciones.")
                    Button(onClick = { reloadTrigger++ }) {
                        Text("Reintentar")
                    }
                }
                is TermsUiState.Success -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(current.content)
                }
            }
        }
    }
}
