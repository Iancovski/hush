package com.iancovski.hush.notifications

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Instrumented test names use camelCase: spaces in method names need DEX 040 (API 30), and minSdk is 29.
@RunWith(AndroidJUnit4::class)
class DailySummaryWorkerTest {

    // The worker reaches the app container through the application, so pass that context.
    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @Before
    fun setUpWorkManager() {
        // The summary schedules its next run, which needs a WorkManager the test controls.
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun theWorkerRunsTheSummaryAndSucceeds() = runTest {
        val worker = TestListenableWorkerBuilder<DailySummaryWorker>(context).build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}
