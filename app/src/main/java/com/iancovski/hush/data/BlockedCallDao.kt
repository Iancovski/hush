package com.iancovski.hush.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedCallDao {

    @Insert
    suspend fun insert(call: BlockedCall)

    /** Newest first, as the history shows them. */
    @Query("SELECT * FROM blocked_calls ORDER BY received_at DESC")
    fun observeAll(): Flow<List<BlockedCall>>

    /** Calls blocked after [since], counted in SQL so the daily summary never loads the list. */
    @Query("SELECT COUNT(*) FROM blocked_calls WHERE received_at > :since")
    suspend fun countSince(since: Long): Int

    /**
     * Calls per day, counted in SQL. Each day is [dayNStart, dayN+1Start): the caller computes the
     * boundaries in local time, so days with a daylight saving change keep their real length.
     */
    @Query(
        """
        SELECT
            COALESCE(SUM(received_at >= :day0Start AND received_at < :day1Start), 0) AS day0,
            COALESCE(SUM(received_at >= :day1Start AND received_at < :day2Start), 0) AS day1,
            COALESCE(SUM(received_at >= :day2Start AND received_at < :day3Start), 0) AS day2,
            COALESCE(SUM(received_at >= :day3Start AND received_at < :day4Start), 0) AS day3,
            COALESCE(SUM(received_at >= :day4Start AND received_at < :day5Start), 0) AS day4,
            COALESCE(SUM(received_at >= :day5Start AND received_at < :day6Start), 0) AS day5,
            COALESCE(SUM(received_at >= :day6Start AND received_at < :day7Start), 0) AS day6
        FROM blocked_calls
        WHERE received_at >= :day0Start AND received_at < :day7Start
        """,
    )
    fun observeCountsPerDay(
        day0Start: Long,
        day1Start: Long,
        day2Start: Long,
        day3Start: Long,
        day4Start: Long,
        day5Start: Long,
        day6Start: Long,
        day7Start: Long,
    ): Flow<DailyBlockedCounts>

    @Query("DELETE FROM blocked_calls")
    suspend fun deleteAll()
}
