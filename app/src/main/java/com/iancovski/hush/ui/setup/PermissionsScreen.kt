package com.iancovski.hush.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iancovski.hush.R
import com.iancovski.hush.ui.theme.HushTheme

/** Second wizard step: explains and asks for the call screening role and contacts access. */
@Composable
fun PermissionsScreen(
    uiState: PermissionsUiState,
    onGrantClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionLabel = when {
        uiState.allGranted -> R.string.setup_continue
        uiState.needsSystemSettings -> R.string.setup_permissions_open_settings
        else -> R.string.setup_permissions_grant
    }
    SetupStepScaffold(
        step = SetupStep.PERMISSIONS,
        actionLabel = stringResource(actionLabel),
        onActionClick = if (uiState.allGranted) onContinueClick else onGrantClick,
        modifier = modifier,
    ) {
        SetupStepTitle(
            text = stringResource(R.string.setup_permissions_title),
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.setup_permissions_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PermissionItem(
                title = stringResource(R.string.permission_call_screening_title),
                reason = stringResource(R.string.permission_call_screening_reason),
                granted = uiState.callScreeningRoleHeld,
            )
            PermissionItem(
                title = stringResource(R.string.permission_contacts_title),
                reason = stringResource(R.string.permission_contacts_reason),
                granted = uiState.contactsGranted,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(
                when {
                    uiState.allGranted -> R.string.setup_permissions_all_granted
                    uiState.needsSystemSettings -> R.string.setup_permissions_settings_hint
                    else -> R.string.setup_permissions_hint
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    reason: String,
    granted: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(16.dp))
            // The icon shape changes with the state, so it does not rely on color alone.
            Icon(
                painter = painterResource(
                    if (granted) R.drawable.ic_check_circle else R.drawable.ic_radio_button_unchecked,
                ),
                contentDescription = stringResource(
                    if (granted) R.string.permission_status_granted else R.string.permission_status_missing,
                ),
                tint = if (granted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PermissionsScreenMissingPreview() {
    HushTheme {
        PermissionsScreen(uiState = PermissionsUiState(), onGrantClick = {}, onContinueClick = {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PermissionsScreenBlockedPreview() {
    HushTheme {
        PermissionsScreen(
            uiState = PermissionsUiState(callScreeningRoleHeld = true, contactsDialogBlocked = true),
            onGrantClick = {},
            onContinueClick = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PermissionsScreenGrantedPreview() {
    HushTheme {
        PermissionsScreen(
            uiState = PermissionsUiState(callScreeningRoleHeld = true, contactsGranted = true),
            onGrantClick = {},
            onContinueClick = {},
        )
    }
}
