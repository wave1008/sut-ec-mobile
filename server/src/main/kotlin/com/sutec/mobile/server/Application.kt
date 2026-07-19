package com.sutec.mobile.server

import com.sutec.mobile.server.db.Database
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8090
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    Database.init()
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureValidation()
    configureCORS()
    configureSecurity()
    configureRouting()
}
