package com.iancovski.hush.ui.setup

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * When the user has chosen "Don't ask again", Android answers the role request without showing the
 * dialog. There is no API that tells the two cases apart, but no person can read the dialog and
 * deny it this fast.
 */
private const val MIN_ROLE_DIALOG_MILLIS = 500L

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
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentUiState by rememberUpdatedState(uiState)
    var roleRequestedAt by remember { mutableLongStateOf(0L) }

    // Permissions can change in the system settings while the app is in the background.
    LifecycleResumeEffect(Unit) {
        onRefresh()
        onPauseOrDispose {}
    }

    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val canAskAgain = activity?.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) == true
        onContactsResult(granted, canAskAgain)
        // The role cannot change while the contacts dialog is open, so the current state is still valid.
        if (granted && currentUiState.callScreeningRoleHeld) onContinue()
    }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val granted = result.resultCode == Activity.RESULT_OK
        onRoleResult(SystemClock.elapsedRealtime() - roleRequestedAt >= MIN_ROLE_DIALOG_MILLIS)
        // Contacts access cannot change while the role dialog is open, so the current state is still valid.
        when {
            granted && currentUiState.contactsGranted -> onContinue()
            currentUiState.canRequestContacts -> contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    PermissionsScreen(
        uiState = uiState,
        onGrantClick = {
            when (uiState.grantAction) {
                GrantAction.REQUEST_CALL_SCREENING_ROLE -> {
                    val roleManager = context.getSystemService(RoleManager::class.java)
                    if (roleManager != null) {
                        roleRequestedAt = SystemClock.elapsedRealtime()
                        roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                    }
                }
                GrantAction.REQUEST_CONTACTS -> contactsLauncher.launch(Manifest.permission.READ_CONTACTS)
                GrantAction.OPEN_DEFAULT_APPS_SETTINGS -> context.openDefaultAppsSettings()
                GrantAction.OPEN_APP_SETTINGS -> context.openAppSettings()
                null -> Unit
            }
        },
        onContinueClick = onContinue,
        modifier = modifier,
    )
}

/** The call screening role is listed there as the caller ID and spam app. */
private fun Context.openDefaultAppsSettings() {
    try {
        startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    } catch (_: ActivityNotFoundException) {
        openAppSettings()
    }
}

private fun Context.openAppSettings() {
    try {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
        )
    } catch (_: ActivityNotFoundException) {
        // Some very customized systems have no such screen. The user can still grant it from Settings.
    }
}
