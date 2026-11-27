package com.iancovski.hush.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Steps of the first-run wizard. Finishing the wizard removes all of them from the back stack. */
@Serializable
sealed interface SetupKey : NavKey

@Serializable
data object SetupWelcome : SetupKey

@Serializable
data object SetupPermissions : SetupKey

@Serializable
data object SetupNotifications : SetupKey

@Serializable
data object Home : NavKey
