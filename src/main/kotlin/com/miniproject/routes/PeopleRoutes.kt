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
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction


// Table definition
object Peoples : Table() {
    val id: Column<Int> = integer("id").autoIncrement().uniqueIndex()
    val name: Column<String> = varchar("name", 50)
    val email: Column<String> = varchar("email", 50).uniqueIndex()
    val favoritePL: Column<String> = varchar("favoritePL", 50)
    override val primaryKey = PrimaryKey(id)
}

fun Route.peopleRouting() {
    transaction { SchemaUtils.create(Peoples) }
    route("/people") {
        post ("/"){
            val person = try {
                call.receive<Person>()
            } catch (e: Exception) {
                return@post call.respondText(
                    "Required data fields are missing, data makes no sense, or data contains illegal values.",
                    status = HttpStatusCode.BadRequest
                )
            }
            val checkEmailExists = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.email eq person.email }.singleOrNull()
            }
            if ((checkEmailExists.await()) != null)
                return@post call.respondText(
                    "A person with email '${person.email}' already exists.",
                    status = HttpStatusCode.BadRequest
                )
            val postResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.insert {
                    it[name] = person.name
                    it[email] = person.email
                    it[favoritePL] = person.favoriteProgrammingLanguage
                }
            }
            call.response.header(HttpHeaders.Location, "http://localhost:8080/people/${postResult.await()[Peoples.id]}")
            call.response.header("x-Created-Id", "${postResult.await()[Peoples.id]}")
            call.respondText("Person created successfully", status = HttpStatusCode.Created)
        }
        get("/") {
            val personList = ArrayList<Person>()
            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.selectAll().forEach {
                    val count = Tasks.select { Tasks.ownerId eq it[Peoples.id].toString() }.count()
                    personList.add(Person(id = it[Peoples.id].toString(),
                                          name = it[Peoples.name],
                                          email = it[Peoples.email],
                                          favoriteProgrammingLanguage = it[Peoples.favoritePL],
                                          activeTaskCount = count.toInt()))
                }
            }
            getResult.await()
            call.respond(personList)
        }
        get("{id}") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            val countResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.ownerId eq paramId.toString() }.count()
            }

            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.id eq paramId }.singleOrNull()
            }

            getResult.await() ?: return@get call.respondText(
                "A person with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            val answer = Person(
                id = getResult.await()!![Peoples.id].toString(),
                name = getResult.await()!![Peoples.name],
                email = getResult.await()!![Peoples.email],
                favoriteProgrammingLanguage = getResult.await()!![Peoples.favoritePL],
                activeTaskCount = countResult.await().toInt()
            )

            call.respond(answer)
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
                Peoples.select { Peoples.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@patch call.respondText(
                "A person with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            var fieldToChange: Map<String, String> = HashMap()
            fieldToChange = Gson().fromJson(call.receiveText(), fieldToChange.javaClass)
            if (fieldToChange["name"] != null)
                suspendedTransactionAsync(Dispatchers.IO, db = db) {
                    Peoples.update ({ Peoples.id eq paramId }) {
                        it[name] = fieldToChange["name"]!!
                    }
                }.await()
            if (fieldToChange["email"] != null)
                suspendedTransactionAsync(Dispatchers.IO, db = db) {
                    Peoples.update ({ Peoples.id eq paramId }) {
                        it[email] = fieldToChange["email"]!!
                    }
                }.await()
            if (fieldToChange["favoriteProgrammingLanguage"] != null)
                suspendedTransactionAsync(Dispatchers.IO, db = db) {
                    Peoples.update ({ Peoples.id eq paramId }) {
                        it[favoritePL] = fieldToChange["favoriteProgrammingLanguage"]!!
                    }
                }.await()

            getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.id eq paramId }.single()
            }

            val countResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.ownerId eq paramId.toString() }.count()
            }

            val answer = Person(
                id = getResult.await()[Peoples.id].toString(),
                name = getResult.await()[Peoples.name],
                email = getResult.await()[Peoples.email],
                favoriteProgrammingLanguage = getResult.await()[Peoples.favoritePL],
                activeTaskCount = countResult.await().toInt()
            )
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
            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@delete call.respondText(
                "A person with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            val deleteResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.deleteWhere { Peoples.id eq paramId }
            }

            deleteResult.await()
            call.respondText("Person removed successfully.", status = HttpStatusCode.OK)

        }
        get("{id}/tasks") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            val getResultPerson = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.id eq paramId }.singleOrNull()
            }
            getResultPerson.await() ?: return@get call.respondText(
                "A person with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )
            val tasksList = ArrayList<Task>()
            val getResultTask = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.select { Tasks.ownerId eq paramId.toString() }.forEach {
                    tasksList.add(Task(it[Tasks.title], it[Tasks.details], it[Tasks.dueDate], it[Tasks.status], it[Tasks.ownerId]))
                }
            }
            getResultTask.await()
            call.respond(tasksList)

        }
        post("{id}/tasks") {
            val paramId = try {
                call.parameters["id"]!!.toInt()
            } catch (e: Exception) {
                return@post call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.NotFound
                )
            }
            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@post call.respondText(
                "A person with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            val task = try {
                call.receive<Task>()
            } catch (e: Exception) {
                println(e)
                return@post call.respondText(
                    "Required data fields are missing, data makes no sense, or data contains illegal values.",
                    status = HttpStatusCode.BadRequest
                )
            }

            task.ownerId = paramId.toString()

            val postResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Tasks.insert {
                    it[title] = task.title
                    it[status] = task.status
                    it[dueDate] = task.dueDate
                    it[details] = task.details
                    it[ownerId] =  task.ownerId
                }
            }

            postResult.await()
            call.response.header(HttpHeaders.Location, "http://localhost:8080/tasks/${postResult.await()[Tasks.id]}")
            call.response.header("x-Created-Id", "${postResult.await()[Tasks.id]}")
            call.respondText("Task created and assigned successfully", status = HttpStatusCode.Created)


        }
    }
}


fun Application.registerPeopleRoutes() {
    routing {
        peopleRouting()
    }
}