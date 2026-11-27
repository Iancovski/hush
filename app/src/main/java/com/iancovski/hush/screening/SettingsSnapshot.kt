package com.iancovski.hush.screening

import com.iancovski.hush.data.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The latest settings, kept in memory so screening a call never waits for a DataStore read.
 *
 * Most calls arrive while the app is closed, and the process then starts for the call itself. In
 * that case the first read may still be running, so [current] waits for it, but only briefly.
 */
class SettingsSnapshot(
    settings: Flow<Settings>,
    scope: CoroutineScope,
    private val firstLoadTimeoutMillis: Long = 1_000,
) {

    private val latest: StateFlow<Settings?> = settings.stateIn(scope, SharingStarted.Eagerly, null)

    /** The latest settings, or null if they could not be read in time. */
    suspend fun current(): Settings? =
        latest.value ?: withTimeoutOrNull(firstLoadTimeoutMillis) { latest.filterNotNull().first() }
}
