package com.iancovski.hush.ui.setup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.ui.components.NotificationModeSelector
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
        NotificationModeSelector(
            selectedMode = uiState.selectedMode,
            onModeSelect = onModeSelect,
        )
    }
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
