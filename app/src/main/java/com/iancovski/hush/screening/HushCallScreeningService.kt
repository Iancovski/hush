package com.iancovski.hush.screening

import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * Entry point the system calls for every call once Hush holds ROLE_CALL_SCREENING.
 *
 * Placeholder: the blocking logic is not implemented yet, so every call is allowed.
 */
class HushCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }
}
