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

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called BEFORE super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                QuranNavGraph()
            }
        }
    }
}
