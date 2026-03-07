package app.rork.quran.ui.component

import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rork.quran.domain.model.Ayah
import app.rork.quran.ui.theme.KfgqpcFont
import app.rork.quran.ui.theme.LocalQuranColors

@Composable
fun AyahCard(
    ayah: Ayah,
    isBookmarked: Boolean,
    onBookmark: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQuranColors.current
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(ayah.id) { visible = false; visible = true }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "alpha")
    val cardScale by animateFloatAsState(if (visible) 1f else 0.97f, spring(stiffness = Spring.StiffnessLow), label = "scale")

    var bookmarkBounce by remember { mutableStateOf(false) }
    val bookmarkScale by animateFloatAsState(
        targetValue = if (bookmarkBounce) 1.3f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        finishedListener = { bookmarkBounce = false },
        label = "bookmark",
    )

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .graphicsLayer(alpha = alpha, scaleX = cardScale, scaleY = cardScale)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card)
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(colors.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(ayah.surahNameArabic, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.primary)
            }
            Text("آية ${ayah.ayahNumber}", fontSize = 13.sp, color = colors.textMuted)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(Modifier.height(20.dp))
        Text(
            text = ayah.textArabic,
            fontFamily = KfgqpcFont,
            fontSize = 28.sp,
            lineHeight = 54.sp,
            textAlign = TextAlign.Center,
            color = colors.text,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .clickable(onClick = onNext)
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("الآية التالية", color = colors.textOnPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                Icon(Icons.Default.Refresh, null, tint = colors.textOnPrimary, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { bookmarkBounce = true; onBookmark() },
                modifier = Modifier.size(42.dp).clip(CircleShape).background(colors.surfaceAlt),
            ) {
                Icon(
                    if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    "حفظ",
                    tint = if (isBookmarked) colors.accent else colors.textMuted,
                    modifier = Modifier.size(20.dp).scale(bookmarkScale),
                )
            }
            IconButton(
                onClick = {
                    val text = "${ayah.textArabic}\n\n— سورة ${ayah.surahNameArabic}، آية ${ayah.ayahNumber}"
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "مشاركة الآية"))
                },
                modifier = Modifier.size(42.dp).clip(CircleShape).background(colors.surfaceAlt),
            ) {
                Icon(Icons.Default.Share, "مشاركة", tint = colors.textMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}
