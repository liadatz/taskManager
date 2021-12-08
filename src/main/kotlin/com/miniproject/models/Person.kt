package com.miniproject.models

import kotlinx.serialization.Serializable

@Serializable
data class Person(var name: String,
                  var email: String,
                  var favoriteProgrammingLanguage: String)