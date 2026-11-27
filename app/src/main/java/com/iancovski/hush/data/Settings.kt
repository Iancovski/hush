package com.iancovski.hush.data

/** How Hush tells the user about blocked calls. */
enum class NotificationMode {
    EVERY_CALL,
    DAILY_SUMMARY,
    NEVER,
}

/** User settings, with the defaults applied when nothing is stored yet. */
data class Settings(
    val blockingEnabled: Boolean = true,
    val notificationMode: NotificationMode = NotificationMode.EVERY_CALL,
    val setupCompleted: Boolean = false,
)
