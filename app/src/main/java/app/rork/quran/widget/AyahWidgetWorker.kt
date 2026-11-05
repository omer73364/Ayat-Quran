package app.rork.quran.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.rork.quran.data.repository.AyahRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException

class AyahWidgetWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies { fun ayahRepository(): AyahRepository }

    override suspend fun doWork(): Result {
        val id = inputData.getInt("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID)
        val manager = AppWidgetManager.getInstance(applicationContext)
        fun installed() = id in manager.getAppWidgetIds(ComponentName(applicationContext, AyahWidgetProvider::class.java))
        if (!installed()) return Result.success()
        val store = AyahWidgetStore(applicationContext)
        try {
            val current = store.load(id)
            if (current == null || !store.isCurrent(id) || inputData.getBoolean("refresh", false)) {
                val repository = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java).ayahRepository()
                // Sampling for widgets must not mark verses as read or advance reading progress.
                val candidates = repository.getRandomAyahsForNotifications(2)
                val next = candidates.firstOrNull { it.id != current?.id } ?: candidates.first()
                if (!installed()) return Result.success()
                store.save(id, next)
            }
            AyahWidgetProvider.render(applicationContext, manager, id)
            return Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Leave the cached verse visible while transient database failures are retried.
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
