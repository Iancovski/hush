package com.iancovski.hush.data

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import java.util.Locale

/**
 * Converts numbers to E.164 (such as +5511987654321). A number typed without a country code is read
 * as a number of the first country, in [candidateCountries] order, where it is valid. Reading these
 * countries needs no permission. Typed numbers and incoming numbers go through the same conversion,
 * so they match.
 */
class PhoneNumberNormalizer(context: Context) {

    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)

    fun toE164(number: String): String? =
        normalizeWithCountries(number, candidateCountries(), PhoneNumberUtils::formatNumberToE164)

    /**
     * The SIM comes first because it is the user's home country and does not change when roaming.
     * Then the network, and the device locale for devices without a usable SIM.
     */
    private fun candidateCountries(): List<String?> =
        listOf(telephonyManager?.simCountryIso, telephonyManager?.networkCountryIso, Locale.getDefault().country)
}

/** Returns the first valid E.164 form of [number], trying each country in order. */
internal fun normalizeWithCountries(
    number: String,
    countries: List<String?>,
    formatToE164: (number: String, countryIso: String) -> String?,
): String? =
    countries
        .filterNot { it.isNullOrBlank() }
        .map { it.orEmpty().uppercase(Locale.ROOT) }
        .distinct()
        .firstNotNullOfOrNull { countryIso -> formatToE164(number, countryIso) }
