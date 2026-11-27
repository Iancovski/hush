package com.iancovski.hush.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A call that Hush rejected. */
@Entity(
    tableName = "blocked_calls",
    indices = [Index("received_at"), Index("normalized_number")],
)
data class BlockedCall(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** The number as received, or null when the caller hid it. */
    val number: String?,
    /** E.164 form of [number], or null when it is hidden or cannot be normalized. */
    @ColumnInfo(name = "normalized_number")
    val normalizedNumber: String?,
    /** When the call arrived, in epoch milliseconds (UTC). */
    @ColumnInfo(name = "received_at")
    val receivedAt: Long,
)
