package com.miniproject.models

import kotlinx.serialization.Serializable

@Serializable
data class Person(var id : String = "0",
                  var name: String,
                  var email: String,
                  var favoriteProgrammingLanguage: String,
                  var activeTaskCount: Int = 0)