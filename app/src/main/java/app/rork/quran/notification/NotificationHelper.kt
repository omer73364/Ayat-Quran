package app.rork.quran.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import app.rork.quran.MainActivity
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.rork.quran.R
import app.rork.quran.domain.model.Ayah
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "quran_reminders"
        const val CHANNEL_NAME = "تذكيرات القرآن"
        const val CHANNEL_DESC = "تذكيرات يومية لقراءة القرآن الكريم"

        // Action for the "✔ قرأت" button — handled silently in background
        const val ACTION_READ = "app.rork.quran.ACTION_READ"

        const val EXTRA_AYAH_ID = "ayah_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showAyahNotification(ayah: Ayah, notificationId: Int, isTest: Boolean = false): Boolean {
        if (!androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (notificationManager.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE) return false
        // Activity pending intents work on Android 12+ without a notification trampoline.
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_AYAH_ID, ayah.id)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, notificationId + 20_000, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // "✔ قرأت" button → marks as read silently, no app open
        val readIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_READ
            putExtra(EXTRA_AYAH_ID, ayah.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val readPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 10_000,
            readIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (isTest) "إشعار تجريبي" else "📖 تذكير بالقرآن")
            .setContentText(ayah.textArabic)
            .setStyle(NotificationCompat.BigTextStyle().bigText(ayah.textArabic))
            .setSubText("سورة ${ayah.surahNameArabic} — آية ${ayah.ayahNumber}")
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setOngoing(!isTest)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply { if (!isTest && (context.getSystemService(Context.USER_SERVICE) as android.os.UserManager).isUserUnlocked) addAction(R.drawable.ic_check, "✔ قرأت", readPendingIntent) }
            .build()

        return try {
            notificationManager.notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun dismissNotification(notificationId: Int) {
        if (notificationId >= 0) notificationManager.cancel(notificationId)
    }
}
