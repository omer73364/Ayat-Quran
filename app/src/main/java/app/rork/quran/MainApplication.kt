package app.rork.quran

import android.app.Application
import app.rork.quran.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var ayahRepository: app.rork.quran.data.repository.AyahRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannel()
        scope.launch { ayahRepository.ensureSeeded() }
    }
}
