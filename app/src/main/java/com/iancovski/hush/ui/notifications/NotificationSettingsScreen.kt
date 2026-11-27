package com.iancovski.hush.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.data.NotificationMode
import com.iancovski.hush.ui.components.NotificationModeSelector
import com.iancovski.hush.ui.theme.HushTheme

/** How Hush notifies about blocked calls. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    uiState: NotificationSettingsUiState,
    notificationsAllowed: Boolean,
    onBackClick: () -> Unit,
    onNotificationModeSelect: (NotificationMode) -> Unit,
    onEnableNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.notification_settings_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            NotificationModeSelector(
                selectedMode = uiState.notificationMode,
                onModeSelect = onNotificationModeSelect,
            )
            // Without the permission Hush behaves as NEVER, so say why nothing will arrive.
            if (uiState.notificationMode != NotificationMode.NEVER && !notificationsAllowed) {
                Spacer(Modifier.height(16.dp))
                NotificationsDisabledWarning(onEnableClick = onEnableNotificationsClick)
            }
        }
    }
}

@Composable
private fun NotificationsDisabledWarning(
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.notification_settings_disabled),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        TextButton(
            onClick = onEnableClick,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(stringResource(R.string.notification_settings_enable))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    HushTheme {
        NotificationSettingsScreen(
            uiState = NotificationSettingsUiState(isLoading = false),
            notificationsAllowed = true,
            onBackClick = {},
            onNotificationModeSelect = {},
            onEnableNotificationsClick = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsScreenNotificationsDisabledPreview() {
    HushTheme {
        NotificationSettingsScreen(
            uiState = NotificationSettingsUiState(isLoading = false, notificationMode = NotificationMode.DAILY_SUMMARY),
            notificationsAllowed = false,
            onBackClick = {},
            onNotificationModeSelect = {},
            onEnableNotificationsClick = {},
        )
    }
}
