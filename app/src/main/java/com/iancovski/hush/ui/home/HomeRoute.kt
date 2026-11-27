package com.iancovski.hush.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.iancovski.hush.ui.permissions.rememberPermissionsRequester

/** Connects [HomeScreen] to the permission checks and to the Android permission dialogs. */
@Composable
fun HomeRoute(
    uiState: HomeUiState,
    onRefreshPermissions: () -> Unit,
    onBlockingChange: (Boolean) -> Unit,
    onRoleResult: (dialogShown: Boolean) -> Unit,
    onContactsResult: (granted: Boolean, canAskAgain: Boolean) -> Unit,
    onPermissionsGranted: () -> Unit,
    onPermissionsDialogDismiss: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAllowlist: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The user can remove a permission in the system settings at any time.
    LifecycleResumeEffect(Unit) {
        onRefreshPermissions()
        onPauseOrDispose {}
    }

    val requestPermissions = rememberPermissionsRequester(
        uiState = uiState.permissions,
        onRoleResult = onRoleResult,
        onContactsResult = onContactsResult,
        onAllGranted = onPermissionsGranted,
    )

    HomeScreen(
        uiState = uiState,
        onBlockingChange = onBlockingChange,
        onRequestPermissions = requestPermissions,
        onPermissionsDialogDismiss = onPermissionsDialogDismiss,
        onOpenHistory = onOpenHistory,
        onOpenAllowlist = onOpenAllowlist,
        onOpenNotificationSettings = onOpenNotificationSettings,
        modifier = modifier,
    )
}
