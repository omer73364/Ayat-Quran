package app.rork.quran.ui.screen.bookmarks

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import app.rork.quran.domain.model.searchKey
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.quran.domain.model.Ayah
import app.rork.quran.ui.theme.KfgqpcFont
import app.rork.quran.ui.theme.LocalQuranColors

@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val bookmarks by viewModel.bookmarkedAyahs.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = bookmarks.filter {
        val key = query.searchKey()
        it.surahNameArabic.searchKey().contains(key) ||
            it.textArabic.searchKey().contains(key)
    }.sortedWith(compareBy<Ayah> { it.surahNumber }.thenBy { it.ayahNumber })
    val colors = LocalQuranColors.current

    Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 32.dp, top = 2.dp),
        ) {

            // ── Header ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceAlt),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "رجوع",
                        tint = colors.text,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = "المحفوظات",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    Text(
                        text = "آيات محفوظة: ${bookmarks.size}",
                        fontSize = 12.sp,
                        color = colors.textMuted,
                    )
                }

                Spacer(Modifier.size(38.dp))
            }

            Spacer(Modifier.height(8.dp))

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl,
            ) {
                androidx.compose.material3.TextField(
                    value = query, onValueChange = { query = it }, singleLine = true,
                    placeholder = { Text("ابحث في محفوظاتك", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.primary, modifier = Modifier.size(22.dp)) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, "مسح البحث", tint = colors.textMuted)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = colors.card, unfocusedContainerColor = colors.card,
                        focusedTextColor = colors.text, unfocusedTextColor = colors.text,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = colors.primary, focusedPlaceholderColor = colors.textMuted,
                        unfocusedPlaceholderColor = colors.textMuted,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
                        .border(1.dp, colors.border, RoundedCornerShape(18.dp)),
                )
            }
            if (bookmarks.isEmpty()) {
                EmptyBookmarksState(colors)
            } else if (filtered.isEmpty()) {
                Text("لا توجد نتائج مطابقة", color = colors.textMuted, modifier = Modifier.padding(24.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    filtered.groupBy { it.surahNumber }.forEach { (surah, ayahs) ->
                        item(key = "surah:$surah") {
                            Text(ayahs.first().surahNameArabic, color = colors.primary,
                                fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(ayahs, key = { it.id }) { ayah ->
                            BookmarkItem(
                                ayah = ayah,
                                fontSize = settings.arabicFontSize,
                                onRemove = { viewModel.removeBookmark(ayah.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkItem(ayah: Ayah, fontSize: Int, onRemove: () -> Unit) {
    val colors = LocalQuranColors.current
    val context = LocalContext.current
    var focused by androidx.compose.runtime.remember(ayah.id) { mutableStateOf(false) }
    if (focused) app.rork.quran.ui.component.ReadingDialog(ayah, fontSize) { focused = false }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary.copy(alpha = 0.07f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = ayah.surahNameArabic,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                )
            }
            Text(
                text = "آية ${ayah.ayahNumber}",
                fontSize = 12.sp,
                color = colors.textMuted,
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = ayah.textArabic,
            fontFamily = KfgqpcFont,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.9f).sp,
            textAlign = TextAlign.Center,
            color = colors.text,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { focused = true }) { Text("قراءة بملء الشاشة", color = colors.primary) }
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onRemove,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.error.copy(alpha = 0.06f)),
            ) {
                Icon(
                    Icons.Default.BookmarkRemove,
                    contentDescription = "إزالة",
                    tint = colors.error,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.size(4.dp))
                Text("إزالة", color = colors.error, fontSize = 13.sp)
            }

            IconButton(
                onClick = {
                    val text = "${ayah.textArabic}\n\n— سورة ${ayah.surahNameArabic}، آية ${ayah.ayahNumber}"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, "مشاركة الآية"))
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceAlt)
                    .size(36.dp),
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "مشاركة",
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyBookmarksState(colors: app.rork.quran.ui.theme.QuranColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Bookmark,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "لا توجد محفوظات",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "احفظ آياتك المفضلة بالضغط على أيقونة الحفظ",
            fontSize = 14.sp,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
    }
}
