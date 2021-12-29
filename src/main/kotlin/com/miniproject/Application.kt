package com.miniproject

import com.miniproject.data.MyDatabase
import com.miniproject.routes.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.application.*
import io.ktor.features .*
import io.ktor.gson.*
import io.ktor.serialization.*
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    MyDatabase.createTables()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
            gson()
        }
        registerPeopleRoutes()
        registerTaskRoutes()
    }.start(wait = true)
}


