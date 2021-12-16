package com.miniproject.routes


import com.google.gson.Gson
import com.miniproject.db
import com.miniproject.models.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction


object Tasks : Table() {
    val id: Column<Int> = integer("id").autoIncrement().uniqueIndex()
    val title: Column<String> = varchar("title", 100) //TODO: define length
    val ownerId: Column<String> = varchar("ownerId", 50)
    val status: Column<Status> = enumeration("status", Status::class)
    val details: Column<String> = varchar("details", 50)
    val dueDate: Column<String> = varchar("dueDate", 10)
    override val primaryKey = PrimaryKey(id)
}

fun Route.taskRouting() {
    transaction { SchemaUtils.create(Tasks) }
    route("/tasks") {
        get("{id}") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@get call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            val answer = Task(
                title = getResult.await()!![Tasks.title],
                details = getResult.await()!![Tasks.details],
                dueDate = getResult.await()!![Tasks.dueDate],
                status = getResult.await()!![Tasks.status],
                ownerId = getResult.await()!![Tasks.ownerId])
            // add correct respond
            call.respond(answer);
        }
        patch("{id}") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@patch call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            var getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@patch call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            var fieldToChange: Map<String, String> = HashMap()
            fieldToChange = Gson().fromJson(call.receiveText(), fieldToChange.javaClass)
            if (fieldToChange["title"] != null) {
                suspendedTransactionAsync(Dispatchers.IO, db = db) {
                    Tasks.update({ Tasks.id eq paramId }) {
                        println(fieldToChange["title"])
                        it[Tasks.title] = fieldToChange["title"]!!
                    }
                }.await()
            }
            if (fieldToChange["details"] != null) {
                suspendedTransactionAsync(Dispatchers.IO, db = db) {
                    Tasks.update({ Tasks.id eq paramId }) {
                        it[Tasks.details] = fieldToChange["details"]!!
                    }
                }.await()
            }
            if (fieldToChange["dueDate"] != null) {
                suspendedTransactionAsync(Dispatchers.IO, db = db) {
                    Tasks.update({ Tasks.id eq paramId }) {
                        // TODO: validate date?
                        it[Tasks.dueDate] = fieldToChange["dueDate"]!!
                    }
                }.await()
            }
            if (fieldToChange["status"] != null) {
                suspendedTransactionAsync(Dispatchers.IO, db = db) {
                    Tasks.update({ Tasks.id eq paramId }) {
                        val statusStr = fieldToChange["status"]!!
                        if (statusStr == "active" || statusStr == "Active") it[Tasks.status] = Status.Active //TODO: should be case insensitive
                        else if (statusStr == "done" || statusStr == "Done") it[Tasks.status] = Status.Done //TODO: should be case insensitive
                    }
                }.await()
            }
            getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.single()
            }
            val answer = Task(
                title = getResult.await()[Tasks.title],
                details = getResult.await()[Tasks.details],
                dueDate = getResult.await()[Tasks.dueDate],
                status = getResult.await()[Tasks.status],
                ownerId = getResult.await()[Tasks.ownerId])
            call.respond(answer)
        }

        delete("{id}") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@delete call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            val isExist = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.singleOrNull()
            }
            isExist.await() ?: return@delete call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )
            val deleteResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.deleteWhere { Tasks.id eq paramId }
            }

            deleteResult.await()
            call.respondText("Task removed successfully.", status = HttpStatusCode.OK)
        }


        get("{id}/status") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@get call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            call.respondText(getResult.await()!![Tasks.status].toString(), status = HttpStatusCode.OK);
        }

        put("{id}/status") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@put call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }

            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@put call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )
            val statusStr = call.receiveText()
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.update({ Tasks.id eq paramId }) {
                    if (statusStr == "active" || statusStr == "Active") it[Tasks.status] = Status.Active //TODO: should be case insensitive
                    else if (statusStr == "done" || statusStr == "Done") it[Tasks.status] = Status.Done //TODO: should be case insensitive
                }
            }.await()
            call.respondText("task's status updated successfully.", status = HttpStatusCode.OK)
        }
        get("{id}/owner") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@get call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            call.respondText(getResult.await()!![Tasks.ownerId], status = HttpStatusCode.OK);
        }
        put("{id}/owner") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@put call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }

            val isExist = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.id eq paramId }.singleOrNull()
            }
            isExist.await() ?: return@put call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            val ownerID = call.receiveText();
            val isPersonExist = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.id eq ownerID.toInt() }.singleOrNull()
            }
            isPersonExist.await() ?: return@put call.respondText(
                "A person with the id '${ownerID}' does not exist.",
                status = HttpStatusCode.NotFound
            )
            suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.update({ Tasks.id eq paramId })
                { it[Tasks.ownerId] = ownerID }
            }
            call.respondText("task's owner updated successfully.", status = HttpStatusCode.OK)
        }
    }

}

fun Application.registerTaskRoutes() {
    routing {
        taskRouting()
    }
}