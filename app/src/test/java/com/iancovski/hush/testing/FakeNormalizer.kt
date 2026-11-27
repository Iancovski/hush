package com.iancovski.hush.testing

/**
 * Stand-in for PhoneNumberNormalizer, as if the phone were in Brazil: numbers with fewer than 10
 * digits are invalid, and numbers without a country code get +55.
 */
fun fakeNormalizeNumber(number: String): String? {
    val digits = number.filter(Char::isDigit)
    return when {
        digits.length < 10 -> null
        number.trim().startsWith("+") -> "+$digits"
        else -> "+55$digits"
    }
}
