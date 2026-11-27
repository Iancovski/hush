package com.iancovski.hush.notifications

import android.content.Context
import android.database.SQLException
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iancovski.hush.HushApplication

/** Thin adapter: the summary logic lives in [DailySummaryRunner]. */
class DailySummaryWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as HushApplication).container
        return try {
            container.dailySummaryRunner.run()
            Result.success()
        } catch (_: SQLException) {
            // The database could not be read: try again later instead of losing the summary.
            Result.retry()
        }
    }
}
