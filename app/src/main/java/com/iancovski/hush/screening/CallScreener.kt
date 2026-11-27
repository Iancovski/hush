package com.iancovski.hush.screening

/** What the screening needs to know about a call, without Android types. */
data class IncomingCall(
    val isIncoming: Boolean,
    /** Null when the caller hid the number. */
    val number: String?,
)

sealed interface ScreeningDecision {
    data object Allow : ScreeningDecision
    data object Block : ScreeningDecision
}

/** Tells whether a number is saved in the contacts. */
interface ContactLookup {

    /** Null when the answer is unknown, for example without contacts access. */
    fun isSavedContact(number: String): Boolean?
}

/**
 * Decides whether a call is blocked. It fails open: whenever something is unknown (settings not
 * loaded, contacts unreadable), the call is allowed, because a missed block is better than a missed
 * legitimate call.
 */
class CallScreener(
    private val settingsSnapshot: SettingsSnapshot,
    private val contactLookup: ContactLookup,
) {

    suspend fun screen(call: IncomingCall): ScreeningDecision {
        if (!call.isIncoming) return ScreeningDecision.Allow
        val settings = settingsSnapshot.current() ?: return ScreeningDecision.Allow
        if (!settings.blockingEnabled) return ScreeningDecision.Allow
        // A hidden number can never match a contact.
        val number = call.number ?: return ScreeningDecision.Block
        return when (contactLookup.isSavedContact(number)) {
            false -> ScreeningDecision.Block
            true, null -> ScreeningDecision.Allow
        }
    }
}
