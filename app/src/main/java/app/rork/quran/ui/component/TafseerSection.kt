package app.rork.quran.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.quran.domain.model.Ayah
import app.rork.quran.ui.tafseer.TafseerViewModel
import app.rork.quran.ui.theme.LocalQuranColors

@Composable
internal fun TafseerSection(ayah: Ayah) {
    val viewModel: TafseerViewModel = hiltViewModel(key = "tafseer:${ayah.id}")
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalQuranColors.current
    LaunchedEffect(ayah.id) { viewModel.load(ayah) }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = colors.primary, modifier = Modifier.size(22.dp))
            Text("التفسير الميسر", color = colors.primary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        when {
            state.loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(22.dp), color = colors.primary, strokeWidth = 2.dp)
                Text("جارٍ تحميل التفسير…", color = colors.textMuted, fontSize = 15.sp)
            }
            state.failed -> {
                Text("تعذّر تحميل التفسير. تحقق من اتصال الإنترنت وحاول مجددًا.", color = colors.textMuted, fontSize = 16.sp)
                OutlinedButton(onClick = { viewModel.load(ayah) }) { Text("إعادة المحاولة") }
            }
            else -> {
                SelectionContainer {
                    Text(state.text.orEmpty(), color = colors.text, fontSize = 18.sp,
                        lineHeight = 34.sp, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                }
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                TextButton(onClick = { uriHandler.openUri("https://quran.com/${ayah.surahNumber}:${ayah.ayahNumber}/tafsirs/ar-tafsir-muyassar") },
                    contentPadding = PaddingValues(0.dp)) {
                    Text("المصدر: Quran.com · التفسير الميسر", color = colors.primary, fontSize = 13.sp)
                }
            }
        }
    }
}
