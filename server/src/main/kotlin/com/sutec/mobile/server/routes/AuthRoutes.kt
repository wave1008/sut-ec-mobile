package com.sutec.mobile.server.routes

import com.sutec.mobile.data.dto.LoginRequest
import com.sutec.mobile.data.dto.SignupRequest
import com.sutec.mobile.server.ApiException
import com.sutec.mobile.server.auth.userId
import com.sutec.mobile.server.repository.UserRepository
import com.sutec.mobile.server.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    post("/auth/signup") {
        call.respond(HttpStatusCode.Created, AuthService.signup(call.receive<SignupRequest>()))
    }
    post("/auth/login") {
        call.respond(AuthService.login(call.receive<LoginRequest>()))
    }
    authenticate("auth-jwt") {
        post("/auth/logout") { call.respond(HttpStatusCode.OK) }
        get("/me") {
            val user = UserRepository.findById(call.userId())
                ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "user not found")
            call.respond(user)
        }
    }
}
