package app.rork.quran.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
        // Action for tapping the notification body — marks as read then opens app
        const val ACTION_TAP = "app.rork.quran.ACTION_TAP"

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

    fun showAyahNotification(ayah: Ayah, notificationId: Int) {
        // Tapping the notification body → receiver marks as read AND opens the app
        val tapIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_TAP
            putExtra(EXTRA_AYAH_ID, ayah.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val tapPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 20_000,
            tapIntent,
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
            .setContentTitle("📖 تذكير بالقرآن")
            .setContentText(ayah.textArabic)
            .setStyle(NotificationCompat.BigTextStyle().bigText(ayah.textArabic))
            .setSubText("سورة ${ayah.surahNameArabic} — آية ${ayah.ayahNumber}")
            .setContentIntent(tapPendingIntent)
            // Sticky: stays visible until user taps "✔ قرأت" or the body
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_check, "✔ قرأت", readPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun dismissNotification(notificationId: Int) {
        if (notificationId >= 0) notificationManager.cancel(notificationId)
    }
}
