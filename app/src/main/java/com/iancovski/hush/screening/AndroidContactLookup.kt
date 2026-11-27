package com.iancovski.hush.screening

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import androidx.core.content.ContextCompat

/**
 * Looks the number up with PhoneLookup, which matches numbers the way the dialer does (with or
 * without country code, spaces and dashes). The enterprise URI also covers work profile contacts.
 */
class AndroidContactLookup(private val context: Context) : ContactLookup {

    override fun isSavedContact(number: String): Boolean? {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val uri = Uri.withAppendedPath(PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(number))
        return try {
            context.contentResolver.query(uri, arrayOf(PhoneLookup._ID), null, null, null)
                ?.use { cursor -> cursor.count > 0 }
        } catch (_: SecurityException) {
            // The permission can be revoked between the check and the query.
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
