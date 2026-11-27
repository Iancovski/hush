package com.iancovski.hush

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.iancovski.hush.data.AllowlistRepository
import com.iancovski.hush.data.BlockedCallRepository
import com.iancovski.hush.data.HushDatabase
import com.iancovski.hush.data.PhoneNumberNormalizer
import com.iancovski.hush.data.SettingsRepository
import com.iancovski.hush.permissions.AndroidPermissionsChecker
import com.iancovski.hush.permissions.PermissionsChecker
import com.iancovski.hush.screening.AndroidContactLookup
import com.iancovski.hush.screening.CallScreener
import com.iancovski.hush.screening.ScreeningSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Manual dependency injection: creates the app-wide singletons on first use. */
class AppContainer(
    context: Context,
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    private val appContext = context.applicationContext

    /** For work that must outlive a screen or the screening service. */
    val applicationScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    private val database: HushDatabase by lazy {
        Room.databaseBuilder(appContext, HushDatabase::class.java, "hush.db").build()
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext.settingsDataStore)
    }

    private val phoneNumberNormalizer: PhoneNumberNormalizer by lazy {
        PhoneNumberNormalizer(appContext)
    }

    val blockedCallRepository: BlockedCallRepository by lazy {
        BlockedCallRepository(database.blockedCallDao(), phoneNumberNormalizer::toE164)
    }

    val allowlistRepository: AllowlistRepository by lazy {
        AllowlistRepository(database.allowedNumberDao(), phoneNumberNormalizer::toE164)
    }

    val permissionsChecker: PermissionsChecker by lazy {
        AndroidPermissionsChecker(appContext)
    }

    val callScreener: CallScreener by lazy {
        CallScreener(
            snapshot = ScreeningSnapshot(
                settings = settingsRepository.settings,
                allowedNumbers = allowlistRepository.normalizedNumbers,
                scope = applicationScope,
            ),
            contactLookup = AndroidContactLookup(appContext),
            normalizeNumber = phoneNumberNormalizer::toE164,
        )
    }
}
