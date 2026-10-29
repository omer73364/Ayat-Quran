package app.rork.quran.ui.screen.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rork.quran.data.repository.AyahRepository
import app.rork.quran.data.repository.SettingsRepository
import app.rork.quran.domain.model.UserSettings
import app.rork.quran.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

data class ReminderStatus(
    val notificationsAllowed: Boolean = false,
    val exactAlarmsAllowed: Boolean = false,
    val batteryExempt: Boolean = false,
    val backgroundRestricted: Boolean = false,
    val nextReminder: Long? = null,
    val recovery: String = "",
    val launchSettingsAvailable: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val ayahRepository: AyahRepository,
    private val notificationHelper: app.rork.quran.notification.NotificationHelper,
    private val alarmScheduler: AlarmScheduler,
) : ViewModel() {

    val settings = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun canScheduleExactAlarms(): Boolean = alarmScheduler.canScheduleExact()

    fun isBatteryOptimizationIgnored(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun openAppSettings() {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
    }

    val feedback = MutableStateFlow<String?>(null)
    val isRepairing = MutableStateFlow(false)
    val reminderStatus = MutableStateFlow(ReminderStatus())
    private val launchSettings = BackgroundLaunchSettings(context)

    suspend fun refreshReminderStatus() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        reminderStatus.value = ReminderStatus(
            notificationsAllowed(), canScheduleExactAlarms(), isBatteryOptimizationIgnored(),
            isBackgroundRestricted(), alarmScheduler.nextReminderTime(), alarmScheduler.recoveryStatus(),
            launchSettings.isAvailable(),
        )
    }

    fun openLaunchSettings() {
        if (!launchSettings.open()) {
            feedback.value = "ابحث في إعدادات التطبيق عن التشغيل التلقائي أو البطارية والعمل في الخلفية"
            openAppSettings()
        }
    }
    private val updateMutex = Mutex()

    fun notificationsAllowed(): Boolean {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channel = manager.getNotificationChannel(app.rork.quran.notification.NotificationHelper.CHANNEL_ID)
        return androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            (channel == null || channel.importance != android.app.NotificationManager.IMPORTANCE_NONE)
    }

    fun isBackgroundRestricted(): Boolean = android.os.Build.VERSION.SDK_INT >= 28 &&
        (context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager).isBackgroundRestricted

    fun repairReminders() {
        if (isRepairing.value) return
        isRepairing.value = true
        feedback.value = null
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withTimeout(30_000) {
                    updateMutex.withLock {
                        check(settingsRepository.observeSettings().first().reminderEnabled)
                        alarmScheduler.schedule(ayahRepository.getRandomAyahsForNotifications(70))
                    }
                    refreshReminderStatus()
                }
                feedback.value = "تمت إعادة الجدولة بنجاح"
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                feedback.value = "استغرقت الجدولة وقتاً أطول من المتوقع، حاول مجدداً"
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                feedback.value = "تعذّرت إعادة الجدولة، تحقّق من إعدادات التذكير وحاول مجدداً"
            } finally {
                isRepairing.value = false
            }
        }
    }

    fun openNotificationSettings() {
        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun sendTestNotification() {
        if (!notificationsAllowed()) {
            feedback.value = "اسمح بالإشعارات أولاً من إعدادات الهاتف"
            openNotificationSettings()
            return
        }
        viewModelScope.launch {
            try {
                val ayah = ayahRepository.getRandomAyahsForNotifications(1).first()
                val shown = notificationHelper.showAyahNotification(ayah, 90_000, isTest = true)
                feedback.value = if (shown) "تم إرسال إشعار تجريبي" else "اسمح بالإشعارات من إعدادات الهاتف"
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                feedback.value = "تعذر إرسال الإشعار التجريبي، حاول مرة أخرى"
            }
        }
    }

    fun update(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            updateMutex.withLock {
                try {
                    val previous = settingsRepository.observeSettings().first()
                    val settings = transform(previous)
                    settingsRepository.updateSettings(settings)
                    val scheduleChanged = previous.reminderEnabled != settings.reminderEnabled ||
                        previous.remindersPerDay != settings.remindersPerDay ||
                        previous.reminderFromHour != settings.reminderFromHour ||
                        previous.reminderFromMinute != settings.reminderFromMinute ||
                        previous.reminderToHour != settings.reminderToHour ||
                        previous.reminderToMinute != settings.reminderToMinute
                    if (scheduleChanged) {
                        if (settings.reminderEnabled) {
                            alarmScheduler.schedule(ayahRepository.getRandomAyahsForNotifications(70))
                        } else {
                            alarmScheduler.cancelAll()
                        }
                    }
                } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    feedback.value = "تعذر تحديث التذكيرات، ستتم إعادة المحاولة في الخلفية"
                    app.rork.quran.notification.ReminderRecoveryWorker.recoverNow(context)
                } finally {
                    refreshReminderStatus()
                }
            }
        }
    }
}
