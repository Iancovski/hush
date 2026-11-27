package com.iancovski.hush.permissions

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** State of the permissions Hush needs to block calls. */
data class PermissionStatus(
    val callScreeningRoleHeld: Boolean,
    val contactsGranted: Boolean,
) {
    val allGranted: Boolean
        get() = callScreeningRoleHeld && contactsGranted
}

/** Reads the current permission state. The user can revoke permissions at any time, so never cache it. */
interface PermissionsChecker {
    fun check(): PermissionStatus
}

class AndroidPermissionsChecker(private val context: Context) : PermissionsChecker {

    override fun check(): PermissionStatus = PermissionStatus(
        callScreeningRoleHeld = context.getSystemService(RoleManager::class.java)
            ?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true,
        contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED,
    )
}
