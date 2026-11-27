package com.iancovski.hush

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.permissions.AndroidPermissionsChecker
import com.iancovski.hush.permissions.PermissionsChecker

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Manual dependency injection: creates the app-wide singletons on first use. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext.settingsDataStore)
    }

    val permissionsChecker: PermissionsChecker by lazy {
        AndroidPermissionsChecker(appContext)
    }
}
