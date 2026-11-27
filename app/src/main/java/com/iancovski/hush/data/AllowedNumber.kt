package com.iancovski.hush.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A number on the allowlist: calls from it are never blocked. */
@Entity(
    tableName = "allowed_numbers",
    indices = [Index(value = ["normalized_number"], unique = true), Index("created_at")],
)
data class AllowedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** The number as the user typed it, shown in the list. */
    val number: String,
    /** E.164 form of [number], used to match incoming calls. */
    @ColumnInfo(name = "normalized_number")
    val normalizedNumber: String,
    /** Optional name the user gave to the number. */
    val name: String?,
    /** When the number was added, in epoch milliseconds (UTC). */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
