package com.wolfeleo2.myuon.server.domain

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthServiceTest {

    private fun freshRepository(): StudentRepository {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:auth-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        return ExposedStudentRepository(db)
    }

    private val newStudent = StudentProfile(
        userId = "",
        regNo = "P15/12345/2022",
        fullName = "Jane Student",
        studentEmail = "jane.student@students.uonbi.ac.ke",
        faculty = "Science",
        department = "Computing",
        program = "BSc Computer Science",
        yearOfStudy = 3,
        semester = 1,
        campus = "Main",
        nationalId = "12345678",
        mobileNumber = "0700000000",
        photoUrl = null,
        isFeeCleared = true,
    )

    @Test
    fun `signUp links the Neon Auth user id to a new students row`() = runTest {
        val repo = freshRepository()
        val neonAuth = FakeNeonAuthClient(
            signUpResult = { _, _ -> NeonAuthResult.Success(userId = "user-1", sessionToken = "token-1") },
        )
        val service = AuthService(neonAuth, repo)

        val outcome = service.signUp("jane.student@students.uonbi.ac.ke", "password123", newStudent)

        val success = assertIs<AuthOutcome.Success>(outcome)
        assertEquals("user-1", success.userId)
        assertEquals("jane.student@students.uonbi.ac.ke", repo.findByUserId("user-1")?.studentEmail)
    }

    @Test
    fun `AD-mode login passes the identifier straight through as the email`() = runTest {
        val repo = freshRepository()
        val neonAuth = FakeNeonAuthClient(
            signInResult = { _, _ -> NeonAuthResult.Success(userId = "user-1", sessionToken = "token-1") },
        )
        val service = AuthService(neonAuth, repo)

        val outcome = service.login(LoginMode.Ad, "jane.student@students.uonbi.ac.ke", "password123")

        assertIs<AuthOutcome.Success>(outcome)
        assertEquals(listOf("jane.student@students.uonbi.ac.ke" to "password123"), neonAuth.signInCalls)
    }

    @Test
    fun `SMIS-mode login resolves regNo to email before signing in`() = runTest {
        val repo = freshRepository()
        repo.create(newStudent.copy(userId = "user-1"))
        val neonAuth = FakeNeonAuthClient(
            signInResult = { _, _ -> NeonAuthResult.Success(userId = "user-1", sessionToken = "token-1") },
        )
        val service = AuthService(neonAuth, repo)

        val outcome = service.login(LoginMode.Smis, "P15/12345/2022", "password123")

        assertIs<AuthOutcome.Success>(outcome)
        assertEquals(listOf("jane.student@students.uonbi.ac.ke" to "password123"), neonAuth.signInCalls)
    }

    @Test
    fun `SMIS-mode login fails fast for an unknown regNo without calling Neon Auth`() = runTest {
        val repo = freshRepository()
        val neonAuth = FakeNeonAuthClient()
        val service = AuthService(neonAuth, repo)

        val outcome = service.login(LoginMode.Smis, "UNKNOWN/000", "password123")

        val failure = assertIs<AuthOutcome.Failure>(outcome)
        assertTrue(failure.reason.contains("UNKNOWN/000"))
        assertTrue(neonAuth.signInCalls.isEmpty())
    }
}
