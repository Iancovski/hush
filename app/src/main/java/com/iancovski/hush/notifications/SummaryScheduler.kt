package com.iancovski.hush.notifications

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/** Local time the daily summary targets. Android may run it a little later to save battery. */
val DAILY_SUMMARY_TIME: LocalTime = LocalTime.of(20, 0)

/** How long until the next [at], today if it is still ahead, otherwise tomorrow. Handles DST changes. */
fun delayUntilNextSummary(now: ZonedDateTime, at: LocalTime = DAILY_SUMMARY_TIME): Duration {
    val today = now.with(at)
    val next = if (today.isAfter(now)) today else now.plusDays(1).with(at)
    return Duration.between(now, next)
}

enum class SchedulePolicy {
    /** The mode just changed to DAILY_SUMMARY: start over from the next 20:00. */
    REPLACE,

    /** App start: only schedule if the work was lost. */
    KEEP,

    /** From the running summary itself: queue the next run after it finishes. */
    AFTER_CURRENT,
}

interface SummaryScheduler {
    fun schedule(policy: SchedulePolicy)
    fun cancel()
}

/**
 * Unique one-time work with a delay up to the next 20:00. Periodic work is not used, because its run
 * time drifts over the days. Exact alarms are never used either.
 */
class WorkManagerSummaryScheduler(
    private val workManager: WorkManager,
    private val now: () -> ZonedDateTime = ZonedDateTime::now,
) : SummaryScheduler {

    override fun schedule(policy: SchedulePolicy) {
        val request = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .setInitialDelay(delayUntilNextSummary(now()).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        val existingWorkPolicy = when (policy) {
            SchedulePolicy.REPLACE -> ExistingWorkPolicy.REPLACE
            SchedulePolicy.KEEP -> ExistingWorkPolicy.KEEP
            // REPLACE would cancel the summary that is running right now.
            SchedulePolicy.AFTER_CURRENT -> ExistingWorkPolicy.APPEND_OR_REPLACE
        }
        workManager.enqueueUniqueWork(WORK_NAME, existingWorkPolicy, request)
    }

    override fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "daily_summary"
    }
}
