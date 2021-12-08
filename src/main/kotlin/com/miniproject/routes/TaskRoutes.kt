package com.miniproject.routes

//import com.miniproject.db
import com.miniproject.models.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Column

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import java.time.LocalDateTime
import java.util.*

object Tasks : Table() {
    val id: Column<String> = varchar("id", 50).uniqueIndex()
    val ownerId: Column<String> = varchar("ownerId", 50)
    val status: Column<Status> = enumeration("status", Status::class)
    val details: Column<String> = varchar("details", 50)
    val dueDate: Column<LocalDateTime> = datetime("dueDate")
    override val primaryKey = PrimaryKey(id)
}

fun Route.taskRouting() {
    route("/tasks") {
        get("{id}") {

        }
        patch("{id}") {

        }
        delete("{id}") {

        }
        get("{id}/status") {

        }
        put("{id}/status") {
//            val parameterId = call.parameters["id"] ?: return@put call.respondText(
//                "Missing or malformed id",
//                status = HttpStatusCode.BadRequest
//            )
//            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
//                Tasks.insert { it[id] = parameterId
//                                it[ownerId]}
//            }
//            getResult.await()
        }
        get("{id}/owner") {

        }
        put("{id}/owner") {

        }
    }

}


fun Application.registerTaskRoutes() {
    routing {
        taskRouting()
    }
}