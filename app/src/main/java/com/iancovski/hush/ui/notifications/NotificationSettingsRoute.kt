package com.iancovski.hush.ui.notifications

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.iancovski.hush.data.NotificationMode

/** Connects [NotificationSettingsScreen] to the notification permission and the system settings. */
@Composable
fun NotificationSettingsRoute(
    uiState: NotificationSettingsUiState,
    onBackClick: () -> Unit,
    onNotificationModeSelect: (NotificationMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(context.notificationsAllowed()) }

    // The user can turn notifications on or off in the system settings at any time.
    LifecycleResumeEffect(Unit) {
        notificationsAllowed = context.notificationsAllowed()
        onPauseOrDispose {}
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsAllowed = context.notificationsAllowed()
    }

    NotificationSettingsScreen(
        uiState = uiState,
        notificationsAllowed = notificationsAllowed,
        onBackClick = onBackClick,
        onNotificationModeSelect = { mode ->
            onNotificationModeSelect(mode)
            // Asked only when a mode needs it. Denying keeps the mode; Hush then behaves as NEVER.
            val needsPermission = mode != NotificationMode.NEVER &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !context.notificationsAllowed()
            if (needsPermission) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        onEnableNotificationsClick = { context.openNotificationSettings() },
        modifier = modifier,
    )
}

private fun Context.notificationsAllowed(): Boolean {
    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    return permissionGranted && NotificationManagerCompat.from(this).areNotificationsEnabled()
}

/** The switch there also grants the permission, even after Android stopped asking. */
private fun Context.openNotificationSettings() {
    try {
        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
    } catch (_: ActivityNotFoundException) {
        // Very customized systems may lack this screen; the user can still reach it from Settings.
    }
}
