package com.iancovski.hush.ui.history

import android.text.format.DateFormat
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.allowlist.AllowedNumberDialog
import com.iancovski.hush.ui.theme.HushTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** List of blocked calls, newest first, with an option to clear it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onBackClick: () -> Unit,
    onClearHistory: () -> Unit,
    onCallNumberClick: (HistoryItem) -> Unit,
    onAddToAllowlistClick: (HistoryItem) -> Unit,
    onAddToContactsClick: (HistoryItem) -> Unit,
    onAllowlistDialogInputChange: () -> Unit,
    onAllowlistDialogConfirm: (number: String, name: String) -> Unit,
    onAllowlistDialogDismiss: () -> Unit,
    onAddedToAllowlistShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    // Which call's actions sheet is open. Only the id is saved, so the sheet survives rotation.
    var selectedCallId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val addedMessage = stringResource(R.string.history_added_to_allowlist)

    LaunchedEffect(uiState.showAddedToAllowlist) {
        if (uiState.showAddedToAllowlist) {
            // Shown from its own scope: marking it as shown changes this effect's key, which would
            // cancel a snackbar launched here.
            snackbarScope.launch { snackbarHostState.showSnackbar(addedMessage) }
            onAddedToAllowlistShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                actions = {
                    // Nothing to clear in an empty history, so the menu only shows with calls.
                    if (uiState.calls.isNotEmpty()) {
                        HistoryMenu(onClearClick = { showClearDialog = true })
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Unit
            uiState.calls.isEmpty() -> EmptyHistory(Modifier.padding(innerPadding))
            else -> BlockedCallList(
                calls = uiState.calls,
                onCallClick = { call -> selectedCallId = call.id },
                contentPadding = innerPadding,
            )
        }
    }

    if (showClearDialog) {
        ClearHistoryDialog(
            onConfirm = {
                showClearDialog = false
                onClearHistory()
            },
            onDismiss = { showClearDialog = false },
        )
    }
    uiState.allowlistDialog?.let { dialog ->
        AllowedNumberDialog(
            state = dialog,
            onInputChange = onAllowlistDialogInputChange,
            onConfirm = onAllowlistDialogConfirm,
            onDismiss = onAllowlistDialogDismiss,
        )
    }
    uiState.calls.firstOrNull { it.id == selectedCallId }?.let { call ->
        CallActionsSheet(
            call = call,
            onCallNumberClick = { onCallNumberClick(call) },
            onAddToAllowlistClick = { onAddToAllowlistClick(call) },
            onAddToContactsClick = { onAddToContactsClick(call) },
            onDismiss = { selectedCallId = null },
        )
    }
}

@Composable
private fun HistoryMenu(
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.history_more_options),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.history_clear)) },
                onClick = {
                    expanded = false
                    onClearClick()
                },
            )
        }
    }
}

@Composable
private fun ClearHistoryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Text buttons default to primary, which stays below the text contrast ratio on dark surfaces.
    val buttonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(stringResource(R.string.history_clear_dialog_title)) },
        text = { Text(stringResource(R.string.history_clear_dialog_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = buttonColors) {
                Text(stringResource(R.string.history_clear_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = buttonColors) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun BlockedCallList(
    calls: List<HistoryItem>,
    onCallClick: (HistoryItem) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val zone = remember { ZoneId.systemDefault() }
    // Read once: a history left open past midnight keeps its labels until it is opened again.
    val today = remember(zone) { LocalDate.now(zone) }
    val days = remember(calls, zone) { groupByDay(calls, zone) }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
        days.forEach { day ->
            item(key = "day-${day.date}") {
                DayHeader(date = day.date, today = today)
            }
            itemsIndexed(day.calls, key = { _, call -> call.id }) { index, call ->
                BlockedCallItem(call = call, zone = zone, onClick = { onCallClick(call) })
                if (index < day.calls.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

/** "Today", "Yesterday" or the date, such as "25 de novembro" (with the year when it is not this year). */
@Composable
private fun DayHeader(
    date: LocalDate,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val label = when (date) {
        today -> stringResource(R.string.history_today)
        today.minusDays(1) -> stringResource(R.string.history_yesterday)
        else -> remember(date, today, locale) {
            val skeleton = if (date.year == today.year) "dMMMM" else "dMMMMy"
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(date)
        }
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp),
    )
}

@Composable
private fun BlockedCallItem(
    call: HistoryItem,
    zone: ZoneId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val number = call.number
    val caller = number ?: stringResource(R.string.history_private_number)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(R.string.history_call_options, caller),
                role = Role.Button,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Decorative: the screen title already says these calls were blocked.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_block),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = caller,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = rememberFormattedTime(call.receivedAt, zone),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Opens from the bottom when a call is tapped, with what the user can do with its number. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallActionsSheet(
    call: HistoryItem,
    onCallNumberClick: () -> Unit,
    onAddToAllowlistClick: () -> Unit,
    onAddToContactsClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Only hidden or fully open: the sheet is short, so a half-open state would add nothing.
    // A hidden number cannot be called back or saved, so its actions show as unavailable.
    val hasNumber = call.number != null
    val sheetState = rememberBottomSheetState(SheetValue.Hidden, setOf(SheetValue.Hidden, SheetValue.Expanded))
    val scope = rememberCoroutineScope()
    // Slides the sheet away first, then runs the action, so the next screen opens on a closed sheet.
    val closeThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            action()
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp),
            ) {
                Text(
                    text = call.number ?: stringResource(R.string.history_private_number),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = rememberFormattedDateTime(call.receivedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SheetAction(
                iconRes = R.drawable.ic_call,
                label = stringResource(R.string.history_call_number),
                enabled = hasNumber,
                onClick = { closeThen(onCallNumberClick) },
            )
            SheetAction(
                iconRes = R.drawable.ic_person,
                label = stringResource(R.string.history_add_to_allowlist),
                enabled = hasNumber,
                onClick = { closeThen(onAddToAllowlistClick) },
            )
            SheetAction(
                iconRes = R.drawable.ic_person_add,
                label = stringResource(R.string.history_add_to_contacts),
                enabled = hasNumber,
                onClick = { closeThen(onAddToContactsClick) },
            )
        }
    }
}

@Composable
private fun SheetAction(
    @DrawableRes iconRes: Int,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Material's disabled look: the same colors at 38% opacity.
    val alpha = if (enabled) 1f else 0.38f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Decorative: the label says what the action does.
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        )
    }
}

/** Day, month and time in the user's language and 12/24-hour preference, such as "27 de nov., 18:11". */
@Composable
private fun rememberFormattedDateTime(epochMillis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(epochMillis, locale) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "dMMMjm"), locale)
            .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
    }
}

/** Time in the user's language and 12/24-hour preference, such as "13:32" or "1:32 PM". */
@Composable
private fun rememberFormattedTime(epochMillis: Long, zone: ZoneId): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(epochMillis, zone, locale) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "jm"), locale)
            .format(Instant.ofEpochMilli(epochMillis).atZone(zone))
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_history),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.history_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HistoryScreenLoadedPreview() {
    HushTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                isLoading = false,
                calls = listOf(
                    HistoryItem(id = 3, number = "+55 11 90000-1111", receivedAt = 1_795_794_683_000),
                    HistoryItem(id = 2, number = null, receivedAt = 1_795_790_000_000),
                    HistoryItem(id = 1, number = "+55 21 3000-2222", receivedAt = 1_795_700_000_000),
                ),
            ),
            onBackClick = {},
            onClearHistory = {},
            onCallNumberClick = {},
            onAddToAllowlistClick = {},
            onAddToContactsClick = {},
            onAllowlistDialogInputChange = {},
            onAllowlistDialogConfirm = { _, _ -> },
            onAllowlistDialogDismiss = {},
            onAddedToAllowlistShown = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HistoryScreenEmptyPreview() {
    HushTheme {
        HistoryScreen(
            uiState = HistoryUiState(isLoading = false),
            onBackClick = {},
            onClearHistory = {},
            onCallNumberClick = {},
            onAddToAllowlistClick = {},
            onAddToContactsClick = {},
            onAllowlistDialogInputChange = {},
            onAllowlistDialogConfirm = { _, _ -> },
            onAllowlistDialogDismiss = {},
            onAddedToAllowlistShown = {},
        )
    }
}
