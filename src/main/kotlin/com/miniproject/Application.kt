package com.miniproject

import com.miniproject.routes.Peoples
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.miniproject.routes.registerPeopleRoutes
import com.miniproject.routes.registerTaskRoutes
import io.ktor.application.*
import io.ktor.features .*
import io.ktor.serialization.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

val db = Database.connect("jdbc:sqlite:./src/main/kotlin/com/miniproject/data/data.db", "org.sqlite.JDBC")

fun main() {
    transaction {
        SchemaUtils.drop(Peoples)
    }
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        registerPeopleRoutes()
        registerTaskRoutes()
    }.start(wait = true)
}
