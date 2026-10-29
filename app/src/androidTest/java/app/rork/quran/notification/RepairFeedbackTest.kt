package app.rork.quran.notification

import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.rork.quran.domain.model.UserSettings
import app.rork.quran.ui.screen.settings.SettingsViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepairFeedbackTest {
    @Test fun repairFinishesAndUpdatesCurrentScreen() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val dependencies = EntryPointAccessors.fromApplication(context, ReminderRecoveryWorker.Dependencies::class.java)
        val previous = dependencies.settingsRepository().observeSettings().first()
        val store = ViewModelStore()
        lateinit var model: SettingsViewModel
        try {
            dependencies.settingsRepository().updateSettings(UserSettings(reminderEnabled = true))
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                model = SettingsViewModel(context, dependencies.settingsRepository(), dependencies.ayahRepository(),
                    NotificationHelper(context), dependencies.alarmScheduler())
                store.put("settings", model)
                model.repairReminders()
                assertTrue(model.isRepairing.value)
            }
            withTimeout(35_000) { while (model.isRepairing.value) delay(100) }
            assertEquals("تمت إعادة الجدولة بنجاح", model.feedback.value)
            assertNotNull(model.reminderStatus.value.nextReminder)
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync { store.clear() }
            dependencies.settingsRepository().updateSettings(previous)
            if (!previous.reminderEnabled) dependencies.alarmScheduler().cancelAll()
        }
    }
}
