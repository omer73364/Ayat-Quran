package app.rork.quran

import android.app.Application
import android.os.UserManager
import androidx.work.Configuration
import app.rork.quran.notification.NotificationHelper
import app.rork.quran.notification.ReminderRecoveryWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper(this).createChannel()
        // Direct Boot receivers must be able to start the process while private storage is locked.
        if ((getSystemService(USER_SERVICE) as UserManager).isUserUnlocked) {
            ReminderRecoveryWorker.ensurePeriodicRecovery(this)
        }
    }
}
