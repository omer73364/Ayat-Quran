package app.rork.quran.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.rork.quran.domain.model.Ayah
import app.rork.quran.domain.model.UserSettings
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderRecoveryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val verse = Ayah("1:1", 1, "الفاتحة", 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")

    @org.junit.Before fun stopBackgroundRefreshForIsolatedStoreTests() {
        androidx.work.WorkManager.getInstance(context).cancelAllWork().result.get()
    }

    @Test fun rebootRestorationKeepsPersistedTimesWithoutDatabase() {
        val store = ReminderAlarmStore(context)
        try {
            store.replace(UserSettings(reminderEnabled = true), listOf(verse))
            val next = store.nextReminderTime()
            assertNotNull(next)
            val recreated = ReminderAlarmStore(context)
            recreated.restore()
            assertEquals(next, recreated.nextReminderTime())
            recreated.recordBoot(android.content.Intent.ACTION_BOOT_COMPLETED)
            assertTrue(recreated.recoveryStatus().contains("تمت استعادة"))
            recreated.replace(UserSettings(reminderEnabled = true), listOf(verse), force = false)
            assertEquals(next, recreated.nextReminderTime())
        } finally { store.disable() }
    }

    @Test fun disabledSnapshotCannotBeRestored() {
        val store = ReminderAlarmStore(context)
        store.replace(UserSettings(reminderEnabled = true), listOf(verse))
        store.disable()
        val recreated = ReminderAlarmStore(context)
        recreated.restore()
        assertNull(recreated.nextReminderTime())
    }

    @Test fun prepareRealRebootProbe() {
        // Run only when explicitly requested by adb; otherwise leave no test alarms behind.
        val args = androidx.test.platform.app.InstrumentationRegistry.getArguments()
        if (args.getString("rebootProbe") != "true") return
        val settings = UserSettings(reminderEnabled = true)
        val dependencies = dagger.hilt.android.EntryPointAccessors.fromApplication(context, ReminderRecoveryWorker.Dependencies::class.java)
        kotlinx.coroutines.runBlocking { dependencies.settingsRepository().updateSettings(settings) }
        val time = System.currentTimeMillis() + 180_000
        val snapshot = ReminderSnapshot(settings, listOf(verse), listOf(
            PlannedReminder(69, time, verse), PlannedReminder(68, time + 5 * 24 * 60 * 60 * 1000L, verse),
        ))
        context.createDeviceProtectedStorageContext().getSharedPreferences("boot_reminders", Context.MODE_PRIVATE)
            .edit().putString("snapshot", com.google.gson.Gson().toJson(snapshot)).commit()
        ReminderAlarmStore(context).restore()
        assertEquals(time, ReminderAlarmStore(context).nextReminderTime())
    }
}
