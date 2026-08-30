package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.domain.AuthOutcome
import com.wolfeleo2.myuon.server.domain.AuthService
import com.wolfeleo2.myuon.server.domain.LoginMode
import com.wolfeleo2.myuon.server.domain.StudentProfile
import com.wolfeleo2.myuon.server.domain.Validation
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val regNo: String,
    val fullName: String,
    val faculty: String,
    val department: String,
    val program: String,
    val yearOfStudy: Int,
    val semester: Int,
    val campus: String,
    val nationalId: String,
    val mobileNumber: String,
    val photoUrl: String? = null,
    val isFeeCleared: Boolean = true,
)

@Serializable
data class LoginRequest(val mode: String, val identifier: String, val password: String)

@Serializable
data class AuthResponse(val userId: String, val sessionToken: String)

@Serializable
data class ErrorResponse(val error: String)

fun Route.authRoutes(authService: AuthService) {
    post("/auth/signup") {
        val body = call.receive<SignUpRequest>()
        Validation.validateEmail(body.email)
        Validation.validateRegNo(body.regNo)
        Validation.validatePhone(body.mobileNumber)
        Validation.validateCampus(body.campus)
        Validation.validateYearAndSemester(body.yearOfStudy, body.semester)
        require(body.password.length >= 6) { "Password must be at least 6 characters" }
        require(body.fullName.isNotBlank()) { "Full name is required" }

        val profile = StudentProfile(
            userId = "",
            regNo = body.regNo,
            fullName = body.fullName,
            studentEmail = body.email,
            faculty = body.faculty,
            department = body.department,
            program = body.program,
            yearOfStudy = body.yearOfStudy,
            semester = body.semester,
            campus = body.campus,
            nationalId = body.nationalId,
            mobileNumber = body.mobileNumber,
            photoUrl = body.photoUrl,
            isFeeCleared = body.isFeeCleared,
        )
        when (val outcome = authService.signUp(body.email, body.password, profile)) {
            is AuthOutcome.Success -> call.respond(HttpStatusCode.Created, AuthResponse(outcome.userId, outcome.sessionToken))
            is AuthOutcome.Failure -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(outcome.reason))
        }
    }

    post("/auth/login") {
        val body = call.receive<LoginRequest>()
        require(body.identifier.isNotBlank()) { "Identifier is required" }
        require(body.password.isNotBlank()) { "Password is required" }

        val mode = when (body.mode.uppercase()) {
            "AD" -> {
                Validation.validateEmail(body.identifier)
                LoginMode.Ad
            }
            "SMIS" -> {
                Validation.validateRegNo(body.identifier)
                LoginMode.Smis
            }
            else -> {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("mode must be AD or SMIS"))
                return@post
            }
        }
        when (val outcome = authService.login(mode, body.identifier, body.password)) {
            is AuthOutcome.Success -> call.respond(HttpStatusCode.OK, AuthResponse(outcome.userId, outcome.sessionToken))
            is AuthOutcome.Failure -> call.respond(HttpStatusCode.Unauthorized, ErrorResponse(outcome.reason))
        }
    }
}
