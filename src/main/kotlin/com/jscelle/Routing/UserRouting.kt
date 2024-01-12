package com.jscelle.Routing

import UserService
import com.jscelle.Services.User.User
import com.jscelle.Services.User.UserCreate
import com.jscelle.Services.User.UserCredentials
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Application.userRouting(connection: Connection) {
    val userService = UserService(connection)

    routing {
        // User Signup
        post("/signup") {
            val userCreate = call.receive<UserCreate>()
            val user = userService.signUp(userCreate.name, userCreate.password, status = userCreate.status)
            call.respond(HttpStatusCode.Created, user)
        }

        // User Signin
        post("/signin") {
            val credentials = call.receive<UserCredentials>()
            try {
                val user = userService.signIn(credentials.name, credentials.password)
                call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.Unauthorized, message = e.localizedMessage)
            }
        }

        // Update User Profile
        put("/profile") {
            val updatedUser = call.receive<User>()
            userService.updateProfile(updatedUser)
            call.respond(HttpStatusCode.OK)
        }
    }
}