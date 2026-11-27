package com.iancovski.hush.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Every version bump ships a migration, so the user's history survives updates.
 * - 2: adds the allowlist table.
 */
@Database(
    entities = [BlockedCall::class, AllowedNumber::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class HushDatabase : RoomDatabase() {

    abstract fun blockedCallDao(): BlockedCallDao

    abstract fun allowedNumberDao(): AllowedNumberDao
}
