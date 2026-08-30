package com.wolfeleo2.myuon.server.domain

sealed class LoginMode {
    data object Ad : LoginMode()
    data object Smis : LoginMode()
}

sealed class AuthOutcome {
    data class Success(val userId: String, val sessionToken: String) : AuthOutcome()
    data class Failure(val reason: String) : AuthOutcome()
}

class AuthService(
    private val neonAuthClient: NeonAuthClient,
    private val studentRepository: StudentRepository,
) {
    suspend fun signUp(email: String, password: String, profile: StudentProfile): AuthOutcome =
        when (val result = neonAuthClient.signUp(email, password)) {
            is NeonAuthResult.Success -> {
                studentRepository.create(profile.copy(userId = result.userId, studentEmail = email))
                AuthOutcome.Success(result.userId, result.sessionToken)
            }
            is NeonAuthResult.Failure -> AuthOutcome.Failure(result.message)
        }

    suspend fun login(mode: LoginMode, identifier: String, password: String): AuthOutcome {
        val email = when (mode) {
            LoginMode.Ad -> identifier
            LoginMode.Smis -> {
                val student = studentRepository.findByRegNo(identifier)
                    ?: return AuthOutcome.Failure("No account found for regNo $identifier")
                student.studentEmail
            }
        }
        return when (val result = neonAuthClient.signIn(email, password)) {
            is NeonAuthResult.Success -> AuthOutcome.Success(result.userId, result.sessionToken)
            is NeonAuthResult.Failure -> AuthOutcome.Failure(result.message)
        }
    }
}
