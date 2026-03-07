package app.rork.quran.ui.screen.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.quran.ui.theme.LocalQuranColors
import androidx.compose.material.icons.filled.Add

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = LocalQuranColors.current

    var exactAlarmGranted by remember { mutableStateOf(true) }
    var batteryOptIgnored by remember { mutableStateOf(true) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    // Refresh permission states whenever screen is visible
    LaunchedEffect(settings.reminderEnabled) {
        exactAlarmGranted = viewModel.canScheduleExactAlarms()
        batteryOptIgnored = viewModel.isBatteryOptimizationIgnored()
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = { Text("إذن التنبيهات الدقيقة") },
            text = {
                Text(
                    "يحتاج التطبيق إذن \"التنبيهات والتذكيرات\" لإرسال الإشعارات في الوقت المحدد بدقة.\n\nافتح الإعدادات وفعّل الإذن، ثم عد وفعّل التذكير.",
                    lineHeight = 22.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    viewModel.openExactAlarmSettings()
                }) { Text("فتح الإعدادات", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmDialog = false }) {
                    Text("إلغاء", color = colors.textMuted)
                }
            },
        )
    }

    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false },
            title = { Text("تحسين البطارية") },
            text = {
                Text(
                    "لضمان وصول الإشعارات حتى عند إغلاق التطبيق، يُنصح بإلغاء تقييد البطارية لهذا التطبيق.",
                    lineHeight = 22.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBatteryDialog = false
                    viewModel.openBatteryOptimizationSettings()
                }) { Text("فتح الإعدادات", color = colors.primary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBatteryDialog = false
                    viewModel.update(settings.copy(reminderEnabled = true))
                }) { Text("لاحقاً", color = colors.textMuted) }
            },
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
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
                        text = "الإعدادات",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                }

                Spacer(Modifier.size(38.dp))
            }

            // ── Appearance Section ───────────────────────────────
            SettingsSection(title = "المظهر") {
                SettingsToggleRow(
                    icon = Icons.Default.Brightness4,
                    label = "الوضع الداكن",
                    checked = settings.darkMode,
                    onCheckedChange = { viewModel.update(settings.copy(darkMode = it)) },
                )
            }

            // ── Notifications Section ────────────────────────────
            SettingsSection(title = "التنبيهات") {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    label = "تذكير يومي",
                    description = "تنبيهات لقراءة القرآن",
                    checked = settings.reminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Request POST_NOTIFICATIONS on Android 13+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            // Check exact alarm permission first
                            if (!viewModel.canScheduleExactAlarms()) {
                                showExactAlarmDialog = true
                                return@SettingsToggleRow
                            }
                            // Then prompt battery optimization
                            if (!viewModel.isBatteryOptimizationIgnored()) {
                                showBatteryDialog = true
                                return@SettingsToggleRow
                            }
                            viewModel.update(settings.copy(reminderEnabled = true))
                        } else {
                            viewModel.update(settings.copy(reminderEnabled = false))
                        }
                    },
                )

                // Permission warning banners
                if (settings.reminderEnabled && !exactAlarmGranted) {
                    PermissionWarningBanner(
                        icon = Icons.Default.Alarm,
                        text = "إذن التنبيهات الدقيقة غير ممنوح — اضغط لفتح الإعدادات",
                        onClick = { viewModel.openExactAlarmSettings() },
                    )
                }
                if (settings.reminderEnabled && !batteryOptIgnored) {
                    PermissionWarningBanner(
                        icon = Icons.Default.BatteryAlert,
                        text = "تحسين البطارية مفعّل — قد يؤخر الإشعارات. اضغط للإعداد",
                        onClick = { showBatteryDialog = true },
                    )
                }

                // Time picker (only when enabled)
                if (settings.reminderEnabled) {
                    TimeRangeSection(
                        fromHour = settings.reminderFromHour,
                        fromMinute = settings.reminderFromMinute,
                        toHour = settings.reminderToHour,
                        toMinute = settings.reminderToMinute,
                        onFromHourChange = { viewModel.update(settings.copy(reminderFromHour = it)) },
                        onFromMinuteChange = { viewModel.update(settings.copy(reminderFromMinute = it)) },
                        onToHourChange = { viewModel.update(settings.copy(reminderToHour = it)) },
                        onToMinuteChange = { viewModel.update(settings.copy(reminderToMinute = it)) },
                    )
                }
            }

            // ── Reading Section ──────────────────────────────────
            SettingsSection(title = "القراءة") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceAlt),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.TrackChanges, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("الهدف اليومي", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.text)
                        Text("${settings.dailyGoal} آيات في اليوم", fontSize = 12.sp, color = colors.textMuted)
                    }
                    // Stepper
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepperButton(icon = Icons.Default.Remove) {
                            if (settings.dailyGoal > 1) viewModel.update(settings.copy(dailyGoal = settings.dailyGoal - 1))
                        }
                        Text(settings.dailyGoal.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.text, modifier = Modifier.padding(horizontal = 4.dp))
                        StepperButton(icon = Icons.Default.Add) {
                            if (settings.dailyGoal < 50) viewModel.update(settings.copy(dailyGoal = settings.dailyGoal + 1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    val colors = LocalQuranColors.current
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = colors.textMuted, letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card),
        ) { content() }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalQuranColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.text)
            if (description != null) Text(description, fontSize = 12.sp, color = colors.textMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.38f),
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceAlt,
            ),
        )
    }
}

@Composable
private fun PermissionWarningBanner(icon: ImageVector, text: String, onClick: () -> Unit) {
//    val colors = LocalQuranColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color(0xFFFFF3CD))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF856404), modifier = Modifier.size(16.dp))
        Text(text, fontSize = 12.sp, color = androidx.compose.ui.graphics.Color(0xFF856404), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TimeRangeSection(
    fromHour: Int, fromMinute: Int, toHour: Int, toMinute: Int,
    onFromHourChange: (Int) -> Unit, onFromMinuteChange: (Int) -> Unit,
    onToHourChange: (Int) -> Unit, onToMinuteChange: (Int) -> Unit,
) {
    val colors = LocalQuranColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("وقت التذكير", fontSize = 13.sp, color = colors.textMuted)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimePicker(label = "من", hour = fromHour, minute = fromMinute, onHourChange = onFromHourChange, onMinuteChange = onFromMinuteChange)
            Box(modifier = Modifier.size(16.dp, 2.dp).background(colors.textMuted))
            TimePicker(label = "إلى", hour = toHour, minute = toMinute, onHourChange = onToHourChange, onMinuteChange = onToMinuteChange)
        }
    }
}

@Composable
private fun TimePicker(
    label: String, hour: Int, minute: Int,
    onHourChange: (Int) -> Unit, onMinuteChange: (Int) -> Unit,
) {
    val colors = LocalQuranColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TimeUnit(value = minute, onUp = { onMinuteChange((minute + 15).let { if (it >= 60) 0 else it }) }, onDown = { onMinuteChange((minute - 15).let { if (it < 0) 45 else it }) })
            Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.text)
            TimeUnit(value = hour, onUp = { onHourChange((hour + 1) % 24) }, onDown = { onHourChange((hour - 1 + 24) % 24) })
        }
    }
}

@Composable
private fun TimeUnit(value: Int, onUp: () -> Unit, onDown: () -> Unit) {
    val colors = LocalQuranColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        StepperButton(icon = Icons.Default.Add, onClick = onUp)
        Text(value.toString().padStart(2, '0'), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.text)
        StepperButton(icon = Icons.Default.Remove, onClick = onDown)
    }
}

@Composable
private fun StepperButton(icon: ImageVector, onClick: () -> Unit) {
    val colors = LocalQuranColors.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
    }
}