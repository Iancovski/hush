package com.iancovski.hush.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNumberNormalizerTest {

    /** Stand-in for PhoneNumberUtils: knows that 46991187214 is only valid in Brazil. */
    private val tried = mutableListOf<String>()
    private val format: (String, String) -> String? = { number, country ->
        tried += country
        when {
            number.startsWith("+") -> number.filter { it == '+' || it.isDigit() }
            country == "BR" && number == "46991187214" -> "+5546991187214"
            else -> null
        }
    }

    @Test
    fun `the first country where the number is valid wins`() {
        val normalized = normalizeWithCountries("46991187214", listOf("us", "us", "BR"), format)

        assertEquals("+5546991187214", normalized)
        // Duplicates are skipped and codes are uppercase, as PhoneNumberUtils expects.
        assertEquals(listOf("US", "BR"), tried)
    }

    @Test
    fun `missing countries are skipped`() {
        val normalized = normalizeWithCountries("46991187214", listOf(null, "", "br"), format)

        assertEquals("+5546991187214", normalized)
        assertEquals(listOf("BR"), tried)
    }

    @Test
    fun `a number valid nowhere is null`() {
        assertNull(normalizeWithCountries("1234", listOf("US", "BR"), format))
    }

    @Test
    fun `a number with a country code is valid with the first country`() {
        val normalized = normalizeWithCountries("+55 46 99118-7214", listOf("US", "BR"), format)

        assertEquals("+5546991187214", normalized)
        assertEquals(listOf("US"), tried)
    }
}
