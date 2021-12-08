package com.miniproject.routes

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
        post {
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
        get {

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
            val getResult = suspendedTransactionAsync(Dispatchers.IO, db = db) {
                Peoples.select { Peoples.id eq paramId }.singleOrNull()
            }
            getResult.await() ?: return@get call.respondText(
                "A person with the id '${call.parameters["id"]}' does not exist.",
                status = HttpStatusCode.NotFound
            )

            // add correct respond
            call.respondText("No customers found", status = HttpStatusCode.NotFound)
        }
        patch("{id}") {

        }
        delete("{id}") {

        }
        get("{id}/tasks") {

        }
        post("{id}/tasks") {

        }
    }
}


fun Application.registerPeopleRoutes() {
    routing {
        peopleRouting()
    }
}