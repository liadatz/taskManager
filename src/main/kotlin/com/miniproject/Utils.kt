package com.miniproject

import com.google.gson.internal.bind.util.ISO8601Utils
import java.text.ParseException
import java.text.ParsePosition
import java.time.*
import java.util.*


fun isValidNumber(string: String) : Boolean {
    string.toIntOrNull() ?: return false
    return true
}

fun convertToDate(str: String): LocalDateTime? {
    val date = try {
        ISO8601Utils.parse(str, ParsePosition(0))
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