package com.sutec.mobile.server

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallId) { generate { java.util.UUID.randomUUID().toString() } }
    install(CallLogging) { level = Level.INFO }
}
