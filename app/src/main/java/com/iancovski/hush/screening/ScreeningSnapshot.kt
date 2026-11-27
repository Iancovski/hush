package com.iancovski.hush.screening

import com.iancovski.hush.data.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withTimeoutOrNull

/** What the screening reads during a call. */
data class ScreeningState(
    val settings: Settings,
    /** E.164 forms of the allowed numbers. */
    val allowedNumbers: Set<String>,
)

/**
 * The latest settings and allowlist, kept in memory so screening a call never waits for DataStore
 * or Room.
 *
 * Most calls arrive while the app is closed, and the process then starts for the call itself. In
 * that case the first read may still be running, so [current] waits for it, but only briefly.
 */
class ScreeningSnapshot(
    settings: Flow<Settings>,
    allowedNumbers: Flow<Set<String>>,
    scope: CoroutineScope,
    private val firstLoadTimeoutMillis: Long = 1_000,
) {

    private val latest: StateFlow<ScreeningState?> = combine(settings, allowedNumbers, ::ScreeningState)
        .stateIn(scope, SharingStarted.Eagerly, null)

    /** The latest state, or null if it could not be read in time. */
    suspend fun current(): ScreeningState? =
        latest.value ?: withTimeoutOrNull(firstLoadTimeoutMillis) { latest.filterNotNull().first() }
}
