package com.iancovski.hush.data

import kotlinx.coroutines.flow.Flow

/** Single source of truth for the history of blocked calls. */
class BlockedCallRepository(
    private val dao: BlockedCallDao,
    private val normalizeNumber: (String) -> String?,
) {

    /** Every blocked call, newest first. */
    val blockedCalls: Flow<List<BlockedCall>> = dao.observeAll()

    /** Saves a blocked call. [number] is null when the caller hid it. */
    suspend fun record(number: String?, receivedAt: Long) {
        dao.insert(
            BlockedCall(
                number = number,
                normalizedNumber = number?.let(normalizeNumber),
                receivedAt = receivedAt,
            ),
        )
    }

    suspend fun clear() {
        dao.deleteAll()
    }
}
