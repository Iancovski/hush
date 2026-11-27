package com.iancovski.hush.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    /**
     * Blocked calls per day, oldest first. [dayStarts] holds the eight epoch-millisecond boundaries
     * of seven consecutive days: the start of each day and the start of the day after the last one.
     */
    fun countsPerDay(dayStarts: List<Long>): Flow<List<Int>> {
        require(dayStarts.size == DAYS_PER_WEEK + 1) { "Expected ${DAYS_PER_WEEK + 1} boundaries" }
        return dao.observeCountsPerDay(
            dayStarts[0], dayStarts[1], dayStarts[2], dayStarts[3],
            dayStarts[4], dayStarts[5], dayStarts[6], dayStarts[7],
        ).map { it.toList() }
    }

    /** How many calls were blocked after [since] (epoch milliseconds). */
    suspend fun countSince(since: Long): Int = dao.countSince(since)

    suspend fun clear() {
        dao.deleteAll()
    }
}

const val DAYS_PER_WEEK = 7
