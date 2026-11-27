package com.iancovski.hush.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.iancovski.hush.MainActivity
import com.iancovski.hush.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Hush's notifications, on two channels so the user can tune each kind in the system settings. */
class AndroidNotifier(private val context: Context) : Notifier {

    private val manager = NotificationManagerCompat.from(context)

    /** Safe to call on every start: creating an existing channel only updates its name. */
    fun createChannels() {
        manager.createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat.Builder(CHANNEL_BLOCKED_CALLS, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName(context.getString(R.string.notification_channel_blocked_calls))
                    .setDescription(context.getString(R.string.notification_channel_blocked_calls_description))
                    .build(),
                NotificationChannelCompat.Builder(CHANNEL_DAILY_SUMMARY, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName(context.getString(R.string.notification_channel_daily_summary))
                    .setDescription(context.getString(R.string.notification_channel_daily_summary_description))
                    .build(),
            ),
        )
    }

    override fun showBlockedCall(number: String?, receivedAt: Long) {
        val caller = number ?: context.getString(R.string.history_private_number)
        val notification = baseNotification(CHANNEL_BLOCKED_CALLS)
            .setContentTitle(context.getString(R.string.notification_blocked_call_title))
            .setContentText(context.getString(R.string.notification_blocked_call_text, caller, formatTime(receivedAt)))
            .setWhen(receivedAt)
            .setShowWhen(true)
            .build()
        // One notification per call, so each needs its own id.
        post(id = receivedAt.hashCode(), notification = notification)
    }

    override fun showDailySummary(blockedCount: Int) {
        val notification = baseNotification(CHANNEL_DAILY_SUMMARY)
            .setContentTitle(context.getString(R.string.notification_daily_summary_title))
            .setContentText(
                context.resources.getQuantityString(R.plurals.notification_daily_summary_text, blockedCount, blockedCount),
            )
            .build()
        // A new summary replaces the previous one.
        post(id = DAILY_SUMMARY_ID, notification = notification)
    }

    private fun baseNotification(channelId: String): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_block)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            // Phone numbers stay hidden on a locked screen.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openHistory())
            .setAutoCancel(true)

    private fun openHistory(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        MainActivity.openHistoryIntent(context),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** Exact time in the user's 12/24-hour format: the header only shows how long ago it was. */
    private fun formatTime(epochMillis: Long): String {
        val locale = context.resources.configuration.locales[0]
        return DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "jm"), locale)
            .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
    }

    private fun post(id: Int, notification: android.app.Notification) {
        // The permission is checked every time: the user can revoke it whenever they want.
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!permissionGranted || !manager.areNotificationsEnabled()) return
        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // Revoked between the check and the call.
        }
    }

    private companion object {
        const val CHANNEL_BLOCKED_CALLS = "blocked_calls"
        const val CHANNEL_DAILY_SUMMARY = "daily_summary"
        const val DAILY_SUMMARY_ID = 1
    }
}
