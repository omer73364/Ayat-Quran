package app.rork.quran.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import app.rork.quran.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val REMINDER_ENABLED   = booleanPreferencesKey("reminder_enabled")
        val FROM_HOUR          = intPreferencesKey("from_hour")
        val FROM_MINUTE        = intPreferencesKey("from_minute")
        val TO_HOUR            = intPreferencesKey("to_hour")
        val TO_MINUTE          = intPreferencesKey("to_minute")
        val DAILY_GOAL         = intPreferencesKey("daily_goal")
        val DARK_MODE          = booleanPreferencesKey("dark_mode")
    }

    fun observeSettings(): Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            reminderEnabled  = prefs[Keys.REMINDER_ENABLED] ?: false,
            reminderFromHour = prefs[Keys.FROM_HOUR] ?: 6,
            reminderFromMinute = prefs[Keys.FROM_MINUTE] ?: 0,
            reminderToHour   = prefs[Keys.TO_HOUR] ?: 22,
            reminderToMinute = prefs[Keys.TO_MINUTE] ?: 0,
            dailyGoal        = prefs[Keys.DAILY_GOAL] ?: 5,
            darkMode         = prefs[Keys.DARK_MODE] ?: false,
        )
    }

    suspend fun updateSettings(settings: UserSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.REMINDER_ENABLED]   = settings.reminderEnabled
            prefs[Keys.FROM_HOUR]          = settings.reminderFromHour
            prefs[Keys.FROM_MINUTE]        = settings.reminderFromMinute
            prefs[Keys.TO_HOUR]            = settings.reminderToHour
            prefs[Keys.TO_MINUTE]          = settings.reminderToMinute
            prefs[Keys.DAILY_GOAL]         = settings.dailyGoal
            prefs[Keys.DARK_MODE]          = settings.darkMode
        }
    }
}
