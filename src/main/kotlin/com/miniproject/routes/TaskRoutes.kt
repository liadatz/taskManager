package com.miniproject.routes

import com.miniproject.models.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


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