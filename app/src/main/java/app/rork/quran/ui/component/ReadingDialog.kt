package app.rork.quran.ui.component

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.rork.quran.domain.model.Ayah
import app.rork.quran.ui.theme.KfgqpcFont
import app.rork.quran.ui.theme.LocalQuranColors

@Composable
fun ReadingDialog(ayah: Ayah, fontSize: Int, onDismiss: () -> Unit) {
    val colors = LocalQuranColors.current
    val context = LocalContext.current
    val hostView = LocalView.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val bars = androidx.core.view.ViewCompat.getRootWindowInsets(hostView)
        ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
    val heightPixels = hostView.rootView.height.takeIf { it > 0 }
        ?: (configuration.screenHeightDp * density.density).toInt()
    val readingHeight = with(density) { (heightPixels - (bars?.top ?: 0) - (bars?.bottom ?: 0)).coerceAtLeast(400).toDp() }
    var readingSize by rememberSaveable(ayah.id, fontSize) { mutableStateOf(fontSize) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(Modifier.fillMaxWidth().height(readingHeight), color = colors.background) {
                Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp).background(colors.surfaceAlt, RoundedCornerShape(14.dp))) {
                            Icon(Icons.Default.Close, "إغلاق القراءة", tint = colors.text)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("تدبّر آية", color = colors.text, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                            Text("لحظة هدوء مع كلام الله", color = colors.textMuted, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${ayah.textArabic}\n\n— سورة ${ayah.surahNameArabic}، آية ${ayah.ayahNumber}")
                            }, "مشاركة الآية"))
                        }, modifier = Modifier.size(44.dp).background(colors.surfaceAlt, RoundedCornerShape(14.dp))) {
                            Icon(Icons.Default.Share, "مشاركة الآية", tint = colors.primary)
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxWidth()
                        .border(1.dp, colors.border, RoundedCornerShape(26.dp))
                        .background(Brush.verticalGradient(listOf(colors.primary.copy(alpha = .035f), colors.card)), RoundedCornerShape(26.dp))
                        .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(22.dp)) {
                        Box(Modifier.size(48.dp).background(colors.primary.copy(alpha = .08f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = colors.primary)
                        }
                        Text("سورة ${ayah.surahNameArabic}", color = colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.width(48.dp).height(1.dp).background(colors.accent.copy(alpha = .35f)))
                            Text("✦", color = colors.accent, fontSize = 15.sp)
                            Box(Modifier.width(48.dp).height(1.dp).background(colors.accent.copy(alpha = .35f)))
                        }
                        SelectionContainer {
                            Text(ayah.textArabic, fontFamily = KfgqpcFont, fontSize = readingSize.sp,
                                lineHeight = (readingSize * 1.9f).sp, textAlign = TextAlign.Center, color = colors.text,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                        }
                        Text("الآية ${ayah.ayahNumber}", color = colors.primary, fontSize = 13.sp,
                            modifier = Modifier.background(colors.primary.copy(alpha = .07f), RoundedCornerShape(50)).padding(horizontal = 18.dp, vertical = 8.dp))
                    }
                    Row(Modifier.fillMaxWidth().background(colors.card, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("حجم الخط", color = colors.textMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { readingSize = (readingSize - 2).coerceAtLeast(20) }, enabled = readingSize > 20) {
                            Icon(Icons.Default.Remove, "تصغير الخط", tint = colors.primary)
                        }
                        Text(readingSize.toString(), color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        IconButton(onClick = { readingSize = (readingSize + 2).coerceAtMost(44) }, enabled = readingSize < 44) {
                            Icon(Icons.Default.Add, "تكبير الخط", tint = colors.primary)
                        }
                    }
                }
            }
        }
    }
}
