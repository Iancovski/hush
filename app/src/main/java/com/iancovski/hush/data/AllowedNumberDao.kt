package com.iancovski.hush.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllowedNumberDao {

    /** Returns the new row id, or -1 when the normalized number is already on the list. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(number: AllowedNumber): Long

    /**
     * Returns the number of updated rows: 0 when another entry already has [normalizedNumber]
     * (the unique index makes the update a no-op) or when [id] no longer exists.
     */
    @Query(
        "UPDATE OR IGNORE allowed_numbers " +
            "SET number = :number, normalized_number = :normalizedNumber, name = :name WHERE id = :id",
    )
    suspend fun update(id: Long, number: String, normalizedNumber: String, name: String?): Int

    /** Newest first. */
    @Query("SELECT * FROM allowed_numbers ORDER BY created_at DESC")
    fun observeAll(): Flow<List<AllowedNumber>>

    @Query("DELETE FROM allowed_numbers WHERE id = :id")
    suspend fun delete(id: Long)
}
