package app.rork.quran.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.rork.quran.data.repository.AyahRepository
import app.rork.quran.data.repository.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/** Replenishes the alarm window even when no notification is opened or read. */
class ReminderRecoveryWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun settingsRepository(): SettingsRepository
        fun ayahRepository(): AyahRepository
        fun alarmScheduler(): AlarmScheduler
    }

    override suspend fun doWork(): Result {
        return try {
            val dependencies = EntryPointAccessors.fromApplication(
                applicationContext, Dependencies::class.java,
            )
            val settings = dependencies.settingsRepository().observeSettings().first()
            if (settings.reminderEnabled) {
                val ayahs = dependencies.ayahRepository().getRandomAyahsForNotifications(70)
                dependencies.alarmScheduler().schedule(ayahs, force = false)
            } else {
                dependencies.alarmScheduler().cancelAll()
            }
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e("ReminderRecovery", "Could not restore reminder alarms", error)
            Result.retry()
        }
    }

    companion object {
        fun ensurePeriodicRecovery(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "reminder-periodic-recovery",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ReminderRecoveryWorker>(12, TimeUnit.HOURS).build(),
            )
        }

        fun recoverNow(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder-recovery",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ReminderRecoveryWorker>().build(),
            )
        }
    }
}
