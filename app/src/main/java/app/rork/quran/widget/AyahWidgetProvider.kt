package app.rork.quran.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.rork.quran.MainActivity
import app.rork.quran.R
import app.rork.quran.domain.model.Ayah
import app.rork.quran.notification.NotificationHelper

class AyahWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            render(context, manager, id)
            enqueue(context, id, false)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        // Resizing must not select a different verse.
        render(context, manager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val manager = AppWidgetManager.getInstance(context)
            if (id in manager.getAppWidgetIds(ComponentName(context, AyahWidgetProvider::class.java))) {
                enqueue(context, id, true)
            }
        }
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach {
            WorkManager.getInstance(context).cancelUniqueWork("ayah-widget-$it")
            AyahWidgetStore(context).remove(it)
        }
    }

    companion object {
        private const val ACTION_REFRESH = "app.rork.quran.widget.REFRESH"

        private fun enqueue(context: Context, id: Int, refresh: Boolean) {
            val request = OneTimeWorkRequestBuilder<AyahWidgetWorker>()
                .setInputData(workDataOf("widget_id" to id, "refresh" to refresh)).build()
            // A tap during the initial update must still request another verse.
            val policy = if (refresh) ExistingWorkPolicy.APPEND_OR_REPLACE else ExistingWorkPolicy.KEEP
            WorkManager.getInstance(context).enqueueUniqueWork("ayah-widget-$id", policy, request)
        }

        internal fun render(context: Context, manager: AppWidgetManager, id: Int) {
            val ayah = AyahWidgetStore(context).load(id) ?: AyahWidgetStore.fallback
            manager.updateAppWidget(id, views(context, id, ayah, manager.getAppWidgetOptions(id)))
        }

        internal fun views(context: Context, id: Int, ayah: Ayah, options: Bundle): RemoteViews {
            val open = Intent(context, MainActivity::class.java).apply {
                data = Uri.parse("ayat://widget/$id/verse/${ayah.id}")
                putExtra(NotificationHelper.EXTRA_AYAH_ID, ayah.id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val refresh = Intent(context, AyahWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
                data = Uri.parse("ayat://widget/$id/refresh")
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            }
            val immutable = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180)
            val lines = ((height - 104) / (28f * context.resources.configuration.fontScale)).toInt().coerceIn(1, 14)
            return RemoteViews(context.packageName, R.layout.widget_ayah).apply {
                setTextViewText(R.id.widget_verse, ayah.textArabic)
                setTextViewText(R.id.widget_reference, context.getString(R.string.widget_reference, ayah.surahNameArabic, ayah.ayahNumber))
                setInt(R.id.widget_verse, "setMaxLines", lines)
                setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, id, open, immutable))
                setOnClickPendingIntent(R.id.widget_refresh, PendingIntent.getBroadcast(context, id, refresh, immutable))
            }
        }
    }
}
