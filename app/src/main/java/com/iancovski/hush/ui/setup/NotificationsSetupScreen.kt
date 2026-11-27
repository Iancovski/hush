package com.iancovski.hush.ui.setup

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.ui.theme.HushTheme

/** Last wizard step: the user picks how Hush notifies them about blocked calls. */
@Composable
fun NotificationsSetupScreen(
    uiState: NotificationsSetupUiState,
    onModeSelect: (NotificationMode) -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SetupStepScaffold(
        step = SetupStep.NOTIFICATIONS,
        actionLabel = stringResource(R.string.setup_finish),
        onActionClick = onFinishClick,
        modifier = modifier,
        actionEnabled = !uiState.isFinishing,
    ) {
        SetupStepTitle(
            text = stringResource(R.string.setup_notifications_title),
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.setup_notifications_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotificationMode.entries.forEach { mode ->
                NotificationModeOption(
                    mode = mode,
                    selected = mode == uiState.selectedMode,
                    onSelect = { onModeSelect(mode) },
                )
            }
        }
    }
}

@Composable
private fun NotificationModeOption(
    mode: NotificationMode,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The whole card handles the click, so the radio button only shows the state.
            RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(12.dp))
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = stringResource(mode.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(mode.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@get:StringRes
private val NotificationMode.titleRes: Int
    get() = when (this) {
        NotificationMode.EVERY_CALL -> R.string.notification_mode_every_call
        NotificationMode.DAILY_SUMMARY -> R.string.notification_mode_daily_summary
        NotificationMode.NEVER -> R.string.notification_mode_never
    }

@get:StringRes
private val NotificationMode.descriptionRes: Int
    get() = when (this) {
        NotificationMode.EVERY_CALL -> R.string.notification_mode_every_call_description
        NotificationMode.DAILY_SUMMARY -> R.string.notification_mode_daily_summary_description
        NotificationMode.NEVER -> R.string.notification_mode_never_description
    }

@Preview(showSystemUi = true)
@Composable
private fun NotificationsSetupScreenPreview() {
    HushTheme {
        NotificationsSetupScreen(
            uiState = NotificationsSetupUiState(),
            onModeSelect = {},
            onFinishClick = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun NotificationsSetupScreenNeverPreview() {
    HushTheme {
        NotificationsSetupScreen(
            uiState = NotificationsSetupUiState(selectedMode = NotificationMode.NEVER),
            onModeSelect = {},
            onFinishClick = {},
        )
    }
}
