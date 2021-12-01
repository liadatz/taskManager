package com.miniproject

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.miniproject.plugins.*
import com.miniproject.routes.registerPeopleRoutes
import com.miniproject.routes.registerTaskRoutes
import org.jetbrains.exposed.sql.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        registerPeopleRoutes()
        registerTaskRoutes()
    }.start(wait = true)
}
