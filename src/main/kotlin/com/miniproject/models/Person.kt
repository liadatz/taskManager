package com.miniproject.models

import com.miniproject.isValidEmail
import io.ktor.http.parsing.*
import kotlinx.serialization.Serializable

@Serializable
data class Person(var id : String = "0",
                  var name: String,
                  var email: String,
                  var favoriteProgrammingLanguage: String,
                  var activeTaskCount: Int = 0) {
    init {
        if (!isValidEmail(email)) throw ParseException("Email is not valid")
    }
}