package app.rork.quran.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rork.quran.domain.model.DailyRead
import app.rork.quran.ui.theme.LocalQuranColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Sunday=0 … Saturday=6  (Java dayOfWeek.value % 7)
private val DAY_LABELS_AR = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")

// All heights are fixed so day labels never shift
private val BAR_MAX_HEIGHT = 88.dp
private val COUNT_LABEL_RESERVED = 18.dp  // always reserve space, even when empty
private val DAY_LABEL_RESERVED = 22.dp

@Composable
fun WeeklyChart(
    data: List<DailyRead>,
    goal: Int,
    modifier: Modifier = Modifier,
) {
    val colors = LocalQuranColors.current
    val maxCount = remember(data, goal) { maxOf(goal, data.maxOfOrNull { it.count } ?: 0, 1) }
    val today = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card)
            .padding(20.dp),
    ) {
        // ── Header ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "هذا الأسبوع",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accent.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "الهدف: $goal/يوم",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Chart ────────────────────────────────────────────────
        // Layout per column (top→bottom):
        //   COUNT_LABEL_RESERVED  (count text or empty space)
        //   BAR_MAX_HEIGHT        (bar grows from bottom up)
        //   DAY_LABEL_RESERVED    (always at the same position)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            data.forEach { entry ->
                val fraction = (entry.count.toFloat() / maxCount).coerceIn(0f, 1f)
                val barHeight = (fraction * BAR_MAX_HEIGHT.value).dp.coerceAtLeast(3.dp)
                val isToday = entry.date == today
                val metGoal = entry.count >= goal
                val dayIndex = try {
                    LocalDate.parse(entry.date).dayOfWeek.value % 7
                } catch (_: Exception) { 0 }

                val barColor = when {
                    metGoal -> colors.primary
                    isToday -> colors.primaryLight
                    else    -> colors.surfaceAlt
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 1. Count label — fixed height, shows number or blank
                    Box(
                        modifier = Modifier
                            .height(COUNT_LABEL_RESERVED)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (entry.count > 0) {
                            Text(
                                text = entry.count.toString(),
                                fontSize = 10.sp,
                                color = colors.textMuted,
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    // 2. Bar area — fixed height, bar anchored to bottom
                    Box(
                        modifier = Modifier
                            .height(BAR_MAX_HEIGHT)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(barColor),
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // 3. Day label — fixed height, always at same vertical position
                    Box(
                        modifier = Modifier
                            .height(DAY_LABEL_RESERVED)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(
                            text = DAY_LABELS_AR[dayIndex],
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) colors.primary else colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}
