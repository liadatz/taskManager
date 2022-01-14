package com.miniproject

import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.time.*
import java.util.*
import java.util.regex.Pattern

/**
 * Validates a String for representing number
 * @param string The string to test
 * @return true if string is a valid number
 */
fun isValidNumber(string: String) : Boolean {
    string.toIntOrNull() ?: return false
    return true
}

/**
 * Converts String to LocalDateTime
 * @param string The string to convert
 * @return str parsed to LocalDateTime or null if str is not in a valid ISO8601 format
 */
fun convertToDate(string: String): LocalDateTime? {
    val date = try {
        ISO8601Utils.parse(string, ParsePosition(0))
    }
    catch (e: ParseException) {
        return null
    }
    return convertToLocalDateTimeViaInstant(date)
}

private fun convertToLocalDateTimeViaInstant(dateToConvert: Date): LocalDateTime? {
    return dateToConvert.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}
/**
 * Validates a String format for email
 * format : {someText}@{someText}.{someText}
 * @param emailString The string to test
 * @return true if string is in valid email format
 */
fun isValidEmail(emailString : String) : Boolean {
    val emailAddressPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    fun isValidString(str: String): Boolean{
        return emailAddressPattern.matcher(str).matches()
    }
    return isValidString(emailString)
}