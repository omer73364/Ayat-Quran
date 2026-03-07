package app.rork.quran.ui.screen.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.quran.ui.component.AyahCard
import app.rork.quran.ui.component.StatCard
import app.rork.quran.ui.component.WeeklyChart
import app.rork.quran.ui.theme.LocalQuranColors
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalQuranColors.current

    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        headerVisible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background,
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // ── Header ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderIconButton(
                    onClick = onNavigateToBookmarks,
                    colors = colors,
                ) {
                    Icon(
                        Icons.Default.Bookmark, contentDescription = "محفوظات",
                        tint = colors.text, modifier = Modifier.size(22.dp),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "آيات قرآنية",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                    )
                    Text(
                        text = "تذكير يومي بالقرآن",
                        fontSize = 12.sp,
                        color = colors.textMuted,
                    )
                }

                HeaderIconButton(onClick = onNavigateToSettings, colors = colors) {
                    Icon(
                        Icons.Default.Settings, contentDescription = "إعدادات",
                        tint = colors.text, modifier = Modifier.size(22.dp),
                    )
                }
            }

            // ── Daily Progress Banner ────────────────────────────
            val todayCount = run {
                val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                state.stats.dailyHistory.find { it.date == today }?.count ?: 0
            }
            val progress = (todayCount.toFloat() / state.settings.dailyGoal).coerceIn(0f, 1f)
            val progressAnim by animateFloatAsState(
                targetValue = progress,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "progress",
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary.copy(alpha = 0.07f))
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "اليوم: $todayCount من ${state.settings.dailyGoal} آيات",
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}٪",
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.primary.copy(alpha = 0.2f))
                        .clipToBounds(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAnim)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.primary),
                    )
                }
            }

            // ── Ayah Card ────────────────────────────────────────
            state.currentAyah?.let { ayah ->
                AyahCard(
                    ayah        = ayah,
                    isBookmarked = state.stats.bookmarkedIds.contains(ayah.id),
                    onBookmark  = { viewModel.toggleBookmark(ayah.id) },
                    onNext      = { viewModel.loadNextAndMarkRead() },
                )
            }

            // ── Stat Cards ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value    = state.stats.bookmarkedIds.size.toString(),
                    label    = "المحفوظة",
                    iconTint = colors.accent,
                    bgColor  = colors.accent.copy(alpha = 0.08f),
                    icon     = Icons.Default.Bookmark,
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value    = state.stats.totalRead.toString(),
                    label    = "تمت القراءة",
                    iconTint = colors.primary,
                    bgColor  = colors.primary.copy(alpha = 0.08f),
                    icon     = Icons.AutoMirrored.Filled.MenuBook,
                )
            }

            // ── Weekly Chart ──────────────────────────────────────
            WeeklyChart(
                data = state.weeklyHistory,
                goal = state.settings.dailyGoal,
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    colors: app.rork.quran.ui.theme.QuranColors,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceAlt),
    ) { content() }
}
