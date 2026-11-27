package com.iancovski.hush.testing

import com.iancovski.hush.data.BlockedCall
import com.iancovski.hush.data.BlockedCallDao
import com.iancovski.hush.data.DailyBlockedCounts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [BlockedCallDao] that keeps Room's ordering: newest first. */
class FakeBlockedCallDao : BlockedCallDao {

    private var nextId = 1L
    val calls = MutableStateFlow<List<BlockedCall>>(emptyList())

    override suspend fun insert(call: BlockedCall) {
        calls.value = calls.value + call.copy(id = nextId++)
    }

    override fun observeAll(): Flow<List<BlockedCall>> =
        calls.map { list -> list.sortedByDescending { it.receivedAt } }

    override suspend fun countSince(since: Long): Int = calls.value.count { it.receivedAt > since }

    override fun observeCountsPerDay(
        day0Start: Long,
        day1Start: Long,
        day2Start: Long,
        day3Start: Long,
        day4Start: Long,
        day5Start: Long,
        day6Start: Long,
        day7Start: Long,
    ): Flow<DailyBlockedCounts> = calls.map { list ->
        val starts = listOf(day0Start, day1Start, day2Start, day3Start, day4Start, day5Start, day6Start, day7Start)
        val perDay = (0 until 7).map { day -> list.count { it.receivedAt >= starts[day] && it.receivedAt < starts[day + 1] } }
        DailyBlockedCounts(perDay[0], perDay[1], perDay[2], perDay[3], perDay[4], perDay[5], perDay[6])
    }

    override suspend fun deleteAll() {
        calls.value = emptyList()
    }
}
