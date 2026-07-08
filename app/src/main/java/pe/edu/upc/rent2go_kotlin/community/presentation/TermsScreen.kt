package pe.edu.upc.rent2go_kotlin.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

private sealed class TermsUiState {
    object Loading : TermsUiState()
    data class Success(val content: String) : TermsUiState()
    object Error : TermsUiState()
}

/** Parsed block-level element for the limited markdown subset this document uses. */
private sealed class TermsBlock {
    data class Title(val text: String) : TermsBlock()
    data class Heading(val text: String) : TermsBlock()
    data class Meta(val text: String) : TermsBlock()
    data class Paragraph(val text: String) : TermsBlock()
    data class Bullets(val items: List<String>) : TermsBlock()
    object Rule : TermsBlock()
}

/**
 * Reads and displays the canonical Terms & Conditions content, bundled as a
 * static asset (assets/legal/terms-and-conditions.md) — no network fetch,
 * no backend endpoint. Canonical source: docs/legal/terms-and-conditions.md
 * (see docs/legal/check-parity.md before editing that source).
 *
 * Rendering: the document only uses a limited markdown subset (H1/H2
 * headings, bold spans, bullet lists, links, a metadata line, a horizontal
 * rule). No Compose markdown library is present in this project's Gradle
 * catalog, so this subset is hand-parsed into Material3-styled composables
 * rather than adding a new dependency for a single screen — no legal text
 * is altered, only how it is displayed.
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
                is TermsUiState.Success -> {
                    val blocks = remember(current.content) { parseTermsMarkdown(current.content) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        blocks.forEach { block -> TermsBlockView(block) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsBlockView(block: TermsBlock) {
    when (block) {
        is TermsBlock.Title -> Text(
            text = block.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        is TermsBlock.Meta -> Text(
            // Phase 9 — fixed white-on-white: this Meta block renders on the screen's
            // default (light) background, not on TermsDarkBg, so Color.White was
            // invisible. MaterialTheme.colorScheme.onSurfaceVariant reads correctly in
            // both light and dark theme.
            text = inlineAnnotatedString(block.text),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        is TermsBlock.Rule -> HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        is TermsBlock.Heading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 10.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
        ) {
            Text(
                text = block.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
        is TermsBlock.Paragraph -> Text(
            text = inlineAnnotatedString(block.text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        is TermsBlock.Bullets -> Column(modifier = Modifier.padding(bottom = 8.dp)) {
            block.items.forEach { item ->
                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = inlineAnnotatedString(item),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

/**
 * Renders bold spans (`**...**`) and links (`[text](url)`) inline within a
 * single paragraph/bullet/meta line. Links are styled (underlined, accent
 * color) but not made clickable — adding a URL-launch/Compose-markdown
 * dependency solely for two static contact links in a legal document is
 * disproportionate versus this project's existing minimal-dependency set.
 */
@Composable
private fun inlineAnnotatedString(
    text: String,
    boldColor: Color = MaterialTheme.colorScheme.onBackground,
    linkColor: Color = MaterialTheme.colorScheme.primary
) = buildAnnotatedString {
    val pattern = Regex("\\*\\*(.+?)\\*\\*|\\[(.+?)]\\((.+?)\\)")
    var cursor = 0
    for (match in pattern.findAll(text)) {
        if (match.range.first > cursor) {
            append(text.substring(cursor, match.range.first))
        }
        val bold = match.groupValues[1]
        val linkText = match.groupValues[2]
        if (bold.isNotEmpty()) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                append(bold)
            }
        } else if (linkText.isNotEmpty()) {
            withStyle(
                SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
            ) {
                append(linkText)
            }
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) {
        append(text.substring(cursor))
    }
}

/** Minimal markdown-subset parser scoped to what terms-and-conditions.md actually uses. */
private fun parseTermsMarkdown(source: String): List<TermsBlock> {
    val lines = source.split("\n")
    val blocks = mutableListOf<TermsBlock>()
    val bulletBuffer = mutableListOf<String>()

    fun flushBullets() {
        if (bulletBuffer.isNotEmpty()) {
            blocks.add(TermsBlock.Bullets(bulletBuffer.toList()))
            bulletBuffer.clear()
        }
    }

    for (rawLine in lines) {
        val line = rawLine.trimEnd()
        when {
            line.startsWith("- ") -> bulletBuffer.add(line.substring(2).trim())
            line.isBlank() -> flushBullets()
            line.startsWith("# ") -> {
                flushBullets()
                blocks.add(TermsBlock.Title(line.substring(2).trim()))
            }
            line.startsWith("## ") -> {
                flushBullets()
                blocks.add(TermsBlock.Heading(line.substring(3).trim()))
            }
            line.trim() == "---" -> {
                flushBullets()
                blocks.add(TermsBlock.Rule)
            }
            line.startsWith("**Última actualización:**") -> {
                flushBullets()
                blocks.add(TermsBlock.Meta(line))
            }
            else -> {
                flushBullets()
                blocks.add(TermsBlock.Paragraph(line))
            }
        }
    }
    flushBullets()
    return blocks
}
