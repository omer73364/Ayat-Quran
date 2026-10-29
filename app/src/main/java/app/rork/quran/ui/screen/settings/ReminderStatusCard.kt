package app.rork.quran.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rork.quran.ui.theme.LocalQuranColors

@Composable
fun ReminderStatusCard(
    enabled: Boolean, status: ReminderStatus, repairing: Boolean, feedback: String?, isHonor: Boolean,
    onNotifications: () -> Unit, onExactAlarms: () -> Unit, onBattery: () -> Unit,
    onLaunchSettings: () -> Unit, onRepair: () -> Unit, onTest: () -> Unit,
) {
    val colors = LocalQuranColors.current
    var diagnostics by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("حالة التذكيرات", color = colors.textMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Column(Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(22.dp))
                .background(colors.card, RoundedCornerShape(22.dp))) {
                Column(Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(colors.primary.copy(alpha = .12f), colors.primary.copy(alpha = .03f))), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.NotificationsActive, null, tint = colors.primary)
                        Text(if (enabled) "على موعد مع القرآن" else "لحظات من الطمأنينة", color = colors.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    val next = status.nextReminder?.let {
                        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(it))
                    }
                    Text(when {
                        !enabled -> "فعّل التذكير اليومي ليصلك وردك"
                        !status.notificationsAllowed -> "اسمح بالإشعارات لاستقبال تذكيراتك"
                        next != null -> "التذكير القادم • $next"
                        else -> "لا يوجد موعد قادم، أعد جدولة تذكيراتك"
                    }, color = colors.primary, fontSize = 13.sp, lineHeight = 21.sp)
                }
                StatusRow(Icons.Default.Notifications, "الإشعارات", if (status.notificationsAllowed) "مسموح بها" else "تحتاج إلى السماح", status.notificationsAllowed, onNotifications)
                StatusRow(Icons.Default.Alarm, "دقة المواعيد", if (status.exactAlarmsAllowed) "في الوقت المحدد" else "قد تتأخر التذكيرات", status.exactAlarmsAllowed, onExactAlarms)
                StatusRow(Icons.Default.BatteryChargingFull, "العمل في الخلفية", when {
                    status.backgroundRestricted -> "مقيّد من إعدادات الهاتف"
                    status.batteryExempt -> "دون تقييد تحسين البطارية"
                    else -> "تحسين البطارية مفعّل"
                }, status.batteryExempt && !status.backgroundRestricted, onBattery)
                if (isHonor) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        .background(colors.accent.copy(alpha = .09f), RoundedCornerShape(16.dp)).padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("للتذكير بعد إعادة تشغيل HONOR", color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("ابحث عن «تشغيل التطبيقات» في إعدادات الهاتف، وأوقف الإدارة التلقائية للتطبيق، ثم فعّل التشغيل التلقائي والتشغيل الثانوي والعمل في الخلفية.",
                            color = colors.textMuted, fontSize = 12.sp, lineHeight = 21.sp)
                        TextButton(onClick = onLaunchSettings, contentPadding = PaddingValues(0.dp)) {
                            Text("فتح إعدادات التشغيل", color = colors.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onRepair, enabled = enabled && !repairing,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.textOnPrimary)) {
                        if (repairing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.primary)
                        else Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (repairing) "جارٍ إعداد مواعيدك…" else "إعادة جدولة التذكيرات")
                    }
                    OutlinedButton(onClick = onTest, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)) {
                        Icon(Icons.Default.NotificationsNone, null, Modifier.size(18.dp), tint = colors.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("تجربة إشعار", color = colors.primary)
                    }
                    feedback?.let { Text(it, color = colors.primary, fontSize = 12.sp, lineHeight = 20.sp) }
                    TextButton(onClick = { diagnostics = !diagnostics }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(if (diagnostics) "إخفاء تفاصيل الاستعادة" else "تفاصيل الاستعادة بعد التشغيل", color = colors.textMuted, fontSize = 12.sp)
                    }
                    if (diagnostics) Text(status.recovery, color = colors.textMuted, fontSize = 12.sp, lineHeight = 21.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusRow(icon: ImageVector, title: String, detail: String, ready: Boolean, onClick: () -> Unit) {
    val colors = LocalQuranColors.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(40.dp).background(colors.surfaceAlt, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = colors.textMuted, fontSize = 12.sp)
        }
        Icon(if (ready) Icons.Default.CheckCircle else Icons.Default.Info, null,
            tint = if (ready) colors.primary else colors.accent, modifier = Modifier.size(17.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "فتح الإعدادات", tint = colors.textMuted, modifier = Modifier.size(18.dp))
    }
}
