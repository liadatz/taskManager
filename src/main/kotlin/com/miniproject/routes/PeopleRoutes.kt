package com.miniproject.routes

import com.miniproject.models.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


fun Route.peopleRouting() {
    route("/people") {
        post{

        }
        get {

        }
        get("{id}") {

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