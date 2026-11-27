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

    @Query("DELETE FROM blocked_calls")
    suspend fun deleteAll()
}
