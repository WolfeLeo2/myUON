package com.wolfeleo2.myuon.server.db

import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedAcademicRepositoryTest {

    private fun freshRepository(): Pair<ExposedAcademicRepository, org.jetbrains.exposed.sql.Database> {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:academic-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        return ExposedAcademicRepository(db) to db
    }

    @Test
    fun `getAttendanceOverview joins sessions to a student's attendance without throwing`() = runTest {
        val (repo, db) = freshRepository()
        val studentUserId = "user-1"
        val attendanceSessionId = UUID.randomUUID()

        transaction(db) {
            StudentsTable.insert {
                it[userId] = studentUserId
                it[regNo] = "P15/12345/2022"
                it[fullName] = "Jane Student"
                it[studentEmail] = "jane@students.uonbi.ac.ke"
                it[faculty] = "Science"
                it[department] = "Computing"
                it[program] = "BSc Computer Science"
                it[yearOfStudy] = 3
                it[semester] = 1
                it[campus] = "Main"
                it[nationalId] = "12345678"
                it[mobileNumber] = "0700000000"
            }
            UnitsTable.insert {
                it[code] = "COMP301"
                it[title] = "Databases"
                it[lecturerName] = "Dr. Smith"
                it[department] = "Computing"
                it[yearOfStudy] = 3
                it[semester] = 1
            }
            AttendanceSessionsTable.insert {
                it[id] = attendanceSessionId
                it[unitCode] = "COMP301"
                it[academicYear] = "2025/2026"
                it[semester] = 1
                it[weekNumber] = 1
                it[sessionNumber] = 1
                it[sessionDate] = "2026-01-12"
            }
            StudentAttendanceTable.insert {
                it[id] = UUID.randomUUID()
                it[sessionId] = attendanceSessionId
                it[studentId] = studentUserId
                it[isAttended] = true
            }
        }

        val overview = repo.getAttendanceOverview("P15/12345/2022")

        val comp301 = overview.single { it.unitCode == "COMP301" }
        assertEquals(1, comp301.totalLecturesHeld)
        assertEquals(1, comp301.lecturesAttended)
    }
}
