package com.iancovski.hush.data

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import java.util.Locale

/**
 * Converts numbers to E.164 (such as +5511987654321), using the country of the network or, without
 * one, of the SIM. Reading these countries needs no permission.
 */
class PhoneNumberNormalizer(context: Context) {

    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)

    fun toE164(number: String): String? {
        val countryIso = currentCountryIso() ?: return null
        return PhoneNumberUtils.formatNumberToE164(number, countryIso)
    }

    private fun currentCountryIso(): String? {
        val manager = telephonyManager ?: return null
        return listOf(manager.networkCountryIso, manager.simCountryIso)
            .firstOrNull { !it.isNullOrBlank() }
            ?.uppercase(Locale.ROOT)
    }
}
