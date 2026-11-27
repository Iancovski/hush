package com.iancovski.hush.ui.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.iancovski.hush.ui.permissions.PermissionsUiState
import com.iancovski.hush.ui.permissions.rememberPermissionsRequester

/** Connects [PermissionsScreen] to the Android permission dialogs and settings screens. */
@Composable
fun PermissionsRoute(
    uiState: PermissionsUiState,
    onRefresh: () -> Unit,
    onRoleResult: (dialogShown: Boolean) -> Unit,
    onContactsResult: (granted: Boolean, canAskAgain: Boolean) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Permissions can change in the system settings while the app is in the background.
    LifecycleResumeEffect(Unit) {
        onRefresh()
        onPauseOrDispose {}
    }

    val requestPermissions = rememberPermissionsRequester(
        uiState = uiState,
        onRoleResult = onRoleResult,
        onContactsResult = onContactsResult,
        onAllGranted = onContinue,
    )

    PermissionsScreen(
        uiState = uiState,
        onGrantClick = requestPermissions,
        onContinueClick = onContinue,
        modifier = modifier,
    )
}
