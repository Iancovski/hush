package com.iancovski.hush.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Single source of truth for the user settings, stored in DataStore. */
class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {

    val settings: Flow<Settings> = dataStore.data
        .catch { exception ->
            // A corrupted or unreadable file must not crash the app: fall back to the defaults.
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences.toSettings() }

    /** Saves the notification mode picked in the setup wizard and marks the setup as done. */
    suspend fun completeSetup(notificationMode: NotificationMode) {
        dataStore.edit { preferences ->
            preferences.setNotificationMode(notificationMode)
            preferences[Keys.SETUP_COMPLETED] = true
        }
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[Keys.BLOCKING_ENABLED] = enabled }
    }

    suspend fun setNotificationMode(mode: NotificationMode) {
        dataStore.edit { preferences -> preferences.setNotificationMode(mode) }
    }

    /** When the last daily summary covered calls up to, or null if there was none yet. */
    suspend fun lastSummaryAt(): Long? =
        dataStore.data
            .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
            .first()[Keys.LAST_SUMMARY_AT]

    suspend fun setLastSummaryAt(epochMillis: Long) {
        dataStore.edit { preferences -> preferences[Keys.LAST_SUMMARY_AT] = epochMillis }
    }

    private fun MutablePreferences.setNotificationMode(mode: NotificationMode) {
        this[Keys.NOTIFICATION_MODE] = mode.name
        // The first daily summary only counts calls blocked after the user picked the mode.
        if (mode == NotificationMode.DAILY_SUMMARY) {
            this[Keys.LAST_SUMMARY_AT] = currentTimeMillis()
        }
    }

    private fun Preferences.toSettings(): Settings {
        val defaults = Settings()
        return Settings(
            blockingEnabled = this[Keys.BLOCKING_ENABLED] ?: defaults.blockingEnabled,
            notificationMode = this[Keys.NOTIFICATION_MODE]
                ?.let { name -> NotificationMode.entries.firstOrNull { it.name == name } }
                ?: defaults.notificationMode,
            setupCompleted = this[Keys.SETUP_COMPLETED] ?: defaults.setupCompleted,
        )
    }

    private object Keys {
        val BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val NOTIFICATION_MODE = stringPreferencesKey("notification_mode")
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val LAST_SUMMARY_AT = longPreferencesKey("last_summary_at")
    }
}
