package com.sutec.mobile.server.service

import com.sutec.mobile.data.dto.LoginRequest
import com.sutec.mobile.data.dto.SignupRequest
import com.sutec.mobile.data.dto.TokenResponse
import com.sutec.mobile.data.model.User
import com.sutec.mobile.server.ApiException
import com.sutec.mobile.server.auth.Jwt
import com.sutec.mobile.server.repository.UserRepository
import io.ktor.http.*
import org.mindrot.jbcrypt.BCrypt

// モックの「任意情報で成功」は廃止。password は bcrypt でハッシュ保存し照合する。
object AuthService {

    suspend fun signup(req: SignupRequest): TokenResponse {
        if (req.name.isBlank() || req.email.isBlank() || req.password.isBlank()) {
            throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "name, email and password must not be blank")
        }
        if (UserRepository.findByEmail(req.email) != null) {
            throw ApiException(HttpStatusCode.Conflict, "CONFLICT", "email already registered")
        }
        val hash = BCrypt.hashpw(req.password, BCrypt.gensalt())
        val user = UserRepository.create(req.name.trim(), req.email.trim(), hash)
        return TokenResponse(Jwt.sign(user.id), user)
    }

    suspend fun login(req: LoginRequest): TokenResponse {
        if (req.email.isBlank() || req.password.isBlank()) {
            throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "email and password must not be blank")
        }
        val row = UserRepository.findByEmail(req.email.trim())
            ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "invalid credentials")
        if (!BCrypt.checkpw(req.password, row.passwordHash)) {
            throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "invalid credentials")
        }
        val user = User(row.id.toString(), row.name, row.email)
        return TokenResponse(Jwt.sign(user.id), user)
    }
}
