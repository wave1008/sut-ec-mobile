package com.sutec.mobile.server

import com.sutec.mobile.data.dto.AddCartItemRequest
import com.sutec.mobile.data.dto.LoginRequest
import com.sutec.mobile.data.dto.SignupRequest
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

// receive 時に検証。失敗は RequestValidationException → StatusPages で 400。
fun Application.configureValidation() {
    install(RequestValidation) {
        validate<SignupRequest> { r ->
            when {
                r.name.isBlank() -> ValidationResult.Invalid("name must not be blank")
                !r.email.contains("@") -> ValidationResult.Invalid("email is invalid")
                r.password.isBlank() -> ValidationResult.Invalid("password must not be blank")
                else -> ValidationResult.Valid
            }
        }
        validate<LoginRequest> { r ->
            when {
                r.email.isBlank() -> ValidationResult.Invalid("email must not be blank")
                r.password.isBlank() -> ValidationResult.Invalid("password must not be blank")
                else -> ValidationResult.Valid
            }
        }
        validate<AddCartItemRequest> { r ->
            when {
                r.productId.isBlank() -> ValidationResult.Invalid("productId must not be blank")
                r.quantity <= 0 -> ValidationResult.Invalid("quantity must be positive")
                else -> ValidationResult.Valid
            }
        }
    }
}
