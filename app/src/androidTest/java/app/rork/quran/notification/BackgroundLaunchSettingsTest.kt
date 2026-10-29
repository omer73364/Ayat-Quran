package app.rork.quran.notification

import android.content.ComponentName
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.ui.screen.settings.BackgroundLaunchSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackgroundLaunchSettingsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun missingSettingsAreUnavailableAndOpeningThemDoesNotCrash() {
        val settings = BackgroundLaunchSettings(context, listOf(
            ComponentName(context.packageName, "${context.packageName}.MissingSettings"),
        ))
        assertFalse(settings.isAvailable())
        assertFalse(settings.open())
    }

    @Test
    fun capabilityDetectionUsesAvailableActivitiesInsteadOfPhoneBrand() {
        val settings = BackgroundLaunchSettings(context, listOf(
            ComponentName(context.packageName, "${context.packageName}.MissingSettings"),
            ComponentName(context.packageName, "${context.packageName}.MainActivity"),
        ))
        assertTrue(settings.isAvailable())
    }
}
