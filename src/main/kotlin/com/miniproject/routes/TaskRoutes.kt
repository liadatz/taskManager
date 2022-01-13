package com.miniproject.routes


import com.google.gson.Gson
import com.miniproject.data.MyDatabase
import com.miniproject.isValidNumber
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

import kotlin.collections.HashMap


fun Route.taskRouting() {
    route("/tasks") {
        get("{id}") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            val task = MyDatabase.getTaskAsync(paramId)
            task ?: return@get call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound)

            call.respond(task)
        }
        patch("{id}") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@patch call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            MyDatabase.getTaskAsync(paramId) ?: return@patch call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound)

            var fieldToChange: HashMap<String, Any> = HashMap()
            fieldToChange = Gson().fromJson(call.receiveText(), fieldToChange.javaClass)
            val task = MyDatabase.updateAndGetTask(paramId, fieldToChange)

            task ?: return@patch call.respondText(
                "One or more fields is invalid!",
                status = HttpStatusCode.BadRequest)

            call.respond(task)
        }
        delete("{id}") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@delete call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!
            MyDatabase.getTaskAsync(paramId) ?: return@delete call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound)

            MyDatabase.deleteTasks(paramId)
            call.respondText("Task removed successfully.", status = HttpStatusCode.OK)
        }
        get("{id}/status") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            val task = MyDatabase.getTaskAsync(paramId)
            task ?: return@get call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound)

            call.respondText(task.status.toString(), status = HttpStatusCode.OK)
        }
        put("{id}/status") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@put call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            MyDatabase.getTaskAsync(paramId) ?: return@put call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound)

            MyDatabase.updateTaskStatus(paramId, call.receiveText())
            call.respondText("task's status updated successfully.", status = HttpStatusCode.OK)
        }
        get("{id}/owner") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            val task = MyDatabase.getTaskAsync(paramId)
            task ?: return@get call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound)

            call.respondText(task.ownerId, status = HttpStatusCode.OK)
        }
        put("{id}/owner") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@put call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            MyDatabase.getTaskAsync(paramId) ?: return@put call.respondText(
                "A task with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound)

            val ownerID = call.receiveText()
            MyDatabase.getPersonAsync(ownerID) ?: return@put call.respondText(
                "A person with the id '${ownerID}' does not exist.",
                status = HttpStatusCode.NotFound)

            MyDatabase.updateTaskOwner(paramId, ownerID)
            call.respondText("task's owner updated successfully.", status = HttpStatusCode.OK)
        }
    }
}

fun Application.registerTaskRoutes() {
    routing {
        taskRouting()
    }
}