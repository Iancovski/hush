package com.iancovski.hush.testing

import com.iancovski.hush.notifications.Notifier
import com.iancovski.hush.notifications.SchedulePolicy
import com.iancovski.hush.notifications.SummaryScheduler

/** Records what would have been posted. */
class FakeNotifier : Notifier {
    val blockedCalls = mutableListOf<Pair<String?, Long>>()
    val dailySummaries = mutableListOf<Int>()

    override fun showBlockedCall(number: String?, receivedAt: Long) {
        blockedCalls += number to receivedAt
    }

    override fun showDailySummary(blockedCount: Int) {
        dailySummaries += blockedCount
    }
}

/** Records the scheduling calls instead of talking to WorkManager. */
class FakeSummaryScheduler : SummaryScheduler {
    val scheduled = mutableListOf<SchedulePolicy>()
    var cancellations = 0
        private set

    override fun schedule(policy: SchedulePolicy) {
        scheduled += policy
    }

    override fun cancel() {
        cancellations++
    }
}
