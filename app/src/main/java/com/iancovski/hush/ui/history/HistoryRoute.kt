package com.iancovski.hush.ui.history

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/** Connects [HistoryScreen] to the phone and contacts apps. */
@Composable
fun HistoryRoute(
    uiState: HistoryUiState,
    onBackClick: () -> Unit,
    onClearHistory: () -> Unit,
    onAddToAllowlistClick: (HistoryItem) -> Unit,
    onAllowlistDialogInputChange: () -> Unit,
    onAllowlistDialogConfirm: (number: String, name: String) -> Unit,
    onAllowlistDialogDismiss: () -> Unit,
    onAddedToAllowlistShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    HistoryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onClearHistory = onClearHistory,
        onCallNumberClick = { call -> call.number?.let(context::openDialer) },
        onAddToAllowlistClick = onAddToAllowlistClick,
        onAddToContactsClick = { call -> call.number?.let(context::openAddToContacts) },
        onAllowlistDialogInputChange = onAllowlistDialogInputChange,
        onAllowlistDialogConfirm = onAllowlistDialogConfirm,
        onAllowlistDialogDismiss = onAllowlistDialogDismiss,
        onAddedToAllowlistShown = onAddedToAllowlistShown,
        modifier = modifier,
    )
}

/** Opens the phone app with the number filled in. The user starts the call there, so no permission is needed. */
private fun Context.openDialer(number: String) {
    try {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
    } catch (_: ActivityNotFoundException) {
        // Devices without a phone app, such as some tablets, have nothing to open.
    }
}

/**
 * Hands the number to the contacts app, which lets the user pick an existing contact or create a
 * new one, with the number filled in. The user saves it there, so Hush needs no permission to
 * write contacts.
 */
private fun Context.openAddToContacts(number: String) {
    val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
        .setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE)
        .putExtra(ContactsContract.Intents.Insert.PHONE, number)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Some contacts apps only support creating a new contact.
        try {
            startActivity(
                Intent(ContactsContract.Intents.Insert.ACTION)
                    .setType(ContactsContract.RawContacts.CONTENT_TYPE)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, number),
            )
        } catch (_: ActivityNotFoundException) {
            // No contacts app at all: there is nothing to open.
        }
    }
}
