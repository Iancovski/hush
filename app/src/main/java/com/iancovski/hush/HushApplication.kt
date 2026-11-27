package com.iancovski.hush

import android.app.Application
import kotlinx.coroutines.launch

class HushApplication : Application() {

    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        container.notifier.createChannels()
        // WorkManager can lose the daily summary (for example after a force stop), so schedule it
        // again if the user picked it.
        container.applicationScope.launch { container.notificationModeController.restoreSchedule() }
    }
}
