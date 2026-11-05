package app.rork.quran

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import app.rork.quran.data.repository.SettingsRepository
import app.rork.quran.domain.model.UserSettings
import app.rork.quran.ui.navigation.QuranNavGraph
import app.rork.quran.ui.theme.QuranTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val requestedTafseer = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val requestedAyah = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedTafseer.value = intent.getBooleanExtra(app.rork.quran.notification.NotificationHelper.EXTRA_SHOW_TAFSEER, false)
        requestedAyah.value = intent.getStringExtra(app.rork.quran.notification.NotificationHelper.EXTRA_AYAH_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called BEFORE super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        app.rork.quran.notification.ReminderRecoveryWorker.recoverNow(this)
        requestedTafseer.value = intent.getBooleanExtra(app.rork.quran.notification.NotificationHelper.EXTRA_SHOW_TAFSEER, false)
        requestedAyah.value = intent.getStringExtra(app.rork.quran.notification.NotificationHelper.EXTRA_AYAH_ID)

        val settingsFlow = settingsRepository.observeSettings()
            .stateIn(lifecycleScope, SharingStarted.Eagerly, UserSettings())

        setContent {
            val settings by settingsFlow.collectAsState()
            val isDark = settings.darkMode

            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            SideEffect {
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }

            QuranTheme(darkTheme = isDark) {
                val ayahId by requestedAyah.collectAsState()
                val showTafseer by requestedTafseer.collectAsState()
                QuranNavGraph(ayahId, showTafseer) {
                    requestedTafseer.value = false
                    intent.removeExtra(app.rork.quran.notification.NotificationHelper.EXTRA_SHOW_TAFSEER)
                    requestedAyah.value = null
                    intent.removeExtra(app.rork.quran.notification.NotificationHelper.EXTRA_AYAH_ID)
                }
            }
        }
    }
}
