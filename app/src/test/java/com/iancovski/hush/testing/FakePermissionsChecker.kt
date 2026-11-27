package com.iancovski.hush.testing

import com.iancovski.hush.permissions.PermissionStatus
import com.iancovski.hush.permissions.PermissionsChecker

/** Answers with whatever [status] the test sets, as if the user granted or revoked permissions. */
class FakePermissionsChecker(
    var status: PermissionStatus = PermissionStatus(callScreeningRoleHeld = false, contactsGranted = false),
) : PermissionsChecker {
    override fun check() = status
}
