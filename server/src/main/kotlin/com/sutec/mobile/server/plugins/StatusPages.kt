package com.sutec.mobile.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable data class ErrorBody(val error: ErrorDetail)
@Serializable data class ErrorDetail(val code: String, val message: String)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorBody(ErrorDetail(cause.code, cause.message)))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorBody(ErrorDetail("NOT_FOUND", cause.message ?: "not found")))
        }
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorBody(ErrorDetail("VALIDATION_ERROR", cause.reasons.firstOrNull() ?: "invalid request")))
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorBody(ErrorDetail("VALIDATION_ERROR", cause.message ?: "bad request")))
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorBody(ErrorDetail("INTERNAL", cause.message ?: "internal error")),
            )
        }
    }
}
