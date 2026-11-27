package com.iancovski.hush.ui.history

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme
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
    modifier: Modifier = Modifier,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
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
            else -> BlockedCallList(calls = uiState.calls, contentPadding = innerPadding)
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
                BlockedCallItem(call = call, zone = zone)
                if (index < day.calls.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 80.dp, end = 24.dp),
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
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
            text = call.number ?: stringResource(R.string.history_private_number),
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
        )
    }
}
