package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.StudentProfile
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.exceptions.ExposedSQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ExposedStudentRepositoryTest {

    private fun freshRepository(): ExposedStudentRepository {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        return ExposedStudentRepository(db)
    }

    private val sample = StudentProfile(
        userId = "user-1",
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
    fun `create then findByUserId returns the same profile`() = runTest {
        val repo = freshRepository()
        repo.create(sample)

        assertEquals(sample, repo.findByUserId("user-1"))
    }

    @Test
    fun `findByRegNo resolves the same row`() = runTest {
        val repo = freshRepository()
        repo.create(sample)

        assertEquals(sample, repo.findByRegNo("P15/12345/2022"))
    }

    @Test
    fun `findByUserId returns null when the user has no student row`() = runTest {
        val repo = freshRepository()

        assertNull(repo.findByUserId("missing"))
    }

    @Test
    fun `regNo must be unique`() = runTest {
        val repo = freshRepository()
        repo.create(sample)

        assertFailsWith<ExposedSQLException> {
            repo.create(sample.copy(userId = "user-2"))
        }
    }
}
