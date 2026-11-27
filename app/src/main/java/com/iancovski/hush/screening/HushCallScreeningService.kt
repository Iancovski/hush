package com.iancovski.hush.screening

import android.database.SQLException
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import com.iancovski.hush.HushApplication
import kotlinx.coroutines.launch

/**
 * Entry point the system calls for every call once Hush holds ROLE_CALL_SCREENING. A thin adapter:
 * the decision lives in [CallScreener].
 */
class HushCallScreeningService : CallScreeningService() {

    private val container
        get() = (application as HushApplication).container

    override fun onCreate() {
        super.onCreate()
        // Starts reading the settings before the first call is screened.
        container.callScreener
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val call = callDetails.toIncomingCall()
        // The contact lookup is a disk query, so the screening runs off the main thread.
        // respondToCall may be called from any thread.
        container.applicationScope.launch(container.ioDispatcher) {
            var decision: ScreeningDecision = ScreeningDecision.Allow
            try {
                decision = container.callScreener.screen(call)
            } finally {
                // Always answer, even if screening failed: the call then goes through (fail open).
                respondToCall(callDetails, decision.toResponse())
            }
            if (decision == ScreeningDecision.Block) {
                recordBlockedCall(call.number, receivedAt = callDetails.creationTimeMillis)
                container.blockedCallAlerts.onCallBlocked(call.number, receivedAt = callDetails.creationTimeMillis)
            }
        }
    }

    private suspend fun recordBlockedCall(number: String?, receivedAt: Long) {
        try {
            container.blockedCallRepository.record(number, receivedAt)
        } catch (_: SQLException) {
            // The call was already blocked. Losing its history entry must not crash the app.
        }
    }

    private fun Call.Details.toIncomingCall() = IncomingCall(
        isIncoming = callDirection == Call.Details.DIRECTION_INCOMING,
        number = handle?.schemeSpecificPart
            ?.takeIf { handlePresentation == TelecomManager.PRESENTATION_ALLOWED && it.isNotBlank() },
    )

    private fun ScreeningDecision.toResponse(): CallResponse = when (this) {
        ScreeningDecision.Allow -> CallResponse.Builder().build()
        ScreeningDecision.Block -> CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipNotification(true)
            .setSkipCallLog(true)
            .build()
    }
}
