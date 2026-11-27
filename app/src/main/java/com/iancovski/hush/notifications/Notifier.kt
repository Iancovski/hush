package com.iancovski.hush.notifications

/**
 * Posts Hush's notifications. Implementations post nothing without the notification permission, so
 * Hush then behaves as if the mode were NEVER without changing the stored setting.
 */
interface Notifier {

    /** [number] is null when the caller hid it. */
    fun showBlockedCall(number: String?, receivedAt: Long)

    fun showDailySummary(blockedCount: Int)
}
