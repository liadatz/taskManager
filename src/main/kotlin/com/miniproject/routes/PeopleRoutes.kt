package com.miniproject.routes

import com.google.gson.Gson
import com.miniproject.data.MyDatabase
import com.miniproject.data.Peoples
import com.miniproject.data.Tasks
import com.miniproject.models.Person
import com.miniproject.models.Task
import com.miniproject.isValidNumber
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

/**
 * Configuration for people routing
 */
fun Route.peopleRouting() {
    route("/api/people") {
        post ("/"){
            val person = try {
                call.receive<Person>()
            } catch (e: Exception) {
                return@post call.respondText(
                    "Required data fields are missing, data makes no sense, or data contains illegal values.",
                    status = HttpStatusCode.BadRequest)
            }

            if ((MyDatabase.selectByEmailAsync(person.email).await()) != null)
                return@post call.respondText(
                    "A person with email '${person.email}' already exists.",
                    status = HttpStatusCode.BadRequest)

            val insertResult = MyDatabase.insertPersonAsync(person)
            call.response.header(HttpHeaders.Location, "http://localhost:8080/people/${insertResult[Peoples.id]}")
            call.response.header("x-Created-Id", "${insertResult[Peoples.id]}")
            call.respondText("Person created successfully", status = HttpStatusCode.Created)
        }

        get("/") {
            call.respond(MyDatabase.getPeoplesList())
        }

        get("{id}") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!
            val person = MyDatabase.getPersonAsync(paramId)
            person ?: return@get call.respondText(
                "A person with the id '$paramId' does not exist.",
                status = HttpStatusCode.NotFound)

            call.respond(person)
        }
        patch("{id}") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@patch call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            MyDatabase.getPersonAsync(paramId) ?: return@patch call.respondText(
                "A person with the id '$paramId' does not exist.",
                status = HttpStatusCode.NotFound)

            var fieldToChange: HashMap<String, Any> = HashMap()
            fieldToChange = Gson().fromJson(call.receiveText(), fieldToChange.javaClass)
            val person = MyDatabase.updateAndGetPersonAsync(paramId, fieldToChange)

            person ?: return@patch call.respondText(
                "One or more fields is invalid!",
                status = HttpStatusCode.BadRequest)

            call.respond(person)
        }

        delete("{id}") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@delete call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!
            MyDatabase.getPersonAsync(paramId) ?: return@delete call.respondText(
                "A person with the id '$paramId' does not exist.",
                status = HttpStatusCode.NotFound)

            if (MyDatabase.getActiveTaskAsync(paramId).await() != null)
                return@delete call.respondText(
                    "The person with id '$paramId' has active tasks.\n" +
                            "Please delete them or assign them to someone else.",
                    status = HttpStatusCode.BadRequest)

            MyDatabase.deleteAllTasks(paramId)
            MyDatabase.deletePersonAsync(paramId)
            call.respondText("Person removed successfully.", status = HttpStatusCode.OK)
        }
        get("{id}/tasks") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            MyDatabase.getPersonAsync(paramId) ?: return@get call.respondText(
                "A person with the id '$paramId' does not exist.",
                status = HttpStatusCode.NotFound)

            call.respond(MyDatabase.getTasksList(paramId))
        }
        post("{id}/tasks") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@post call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val paramId = call.parameters["id"]!!

            MyDatabase.getPersonAsync(paramId) ?: return@post call.respondText(
                "A person with the id '$paramId' does not exist.",
                status = HttpStatusCode.NotFound)

            val task = try {
                call.receive<Task>()
            } catch (e: Exception) {
                return@post call.respondText(
                    "Required data fields are missing, data makes no sense, or data contains illegal values.",
                    status = HttpStatusCode.BadRequest)
            }
            task.ownerId = paramId
            val postResult = MyDatabase.insertTask(task)

            call.response.header(HttpHeaders.Location, "http://localhost:8080/tasks/${postResult[Tasks.id]}")
            call.response.header("x-Created-Id", "${postResult[Tasks.id]}")
            call.respondText("Task created and assigned successfully", status = HttpStatusCode.Created)
        }
        put("{currOwnerId}/assign/{newOwnerId}") {
            if (!isValidNumber(call.parameters["id"]!!))
                return@put call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val currId = call.parameters["currOwnerId"]!!

            if (!isValidNumber(call.parameters["id"]!!))
                return@put call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound)
            val newId = call.parameters["newOwnerId"]!!

            MyDatabase.getPersonAsync(currId) ?: return@put call.respondText(
                "A person with the id '$currId' does not exist.",
                status = HttpStatusCode.NotFound)

            MyDatabase.getPersonAsync(newId) ?: return@put call.respondText(
                "A person with the id '$newId' does not exist.",
                status = HttpStatusCode.NotFound)

            MyDatabase.assignTasksTo(currId, newId)
            call.respondText("all tasks assigned successfully", status = HttpStatusCode.OK)
        }
    }
}


fun Application.registerPeopleRoutes() {
    routing {
        peopleRouting()
    }
}