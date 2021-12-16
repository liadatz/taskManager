package com.miniproject


fun isValidNumber(string: String) : Boolean {
    string.toIntOrNull() ?: return false
    return true
}