package com.sutec.mobile.server

import com.auth0.jwt.JWT
import com.sutec.mobile.server.auth.Jwt
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = Jwt.realm
            verifier(
                JWT.require(Jwt.algorithm)
                    .withIssuer(Jwt.issuer)
                    .withAudience(Jwt.audience)
                    .build(),
            )
            validate { cred ->
                if (cred.payload.getClaim("uid").asString() != null) JWTPrincipal(cred.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorBody(ErrorDetail("UNAUTHORIZED", "invalid or missing token")),
                )
            }
        }
    }
}
