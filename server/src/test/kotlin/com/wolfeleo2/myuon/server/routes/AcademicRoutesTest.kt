package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.db.AcademicRequestsTable
import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedAcademicRepository
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import com.wolfeleo2.myuon.server.db.GradeRecordsTable
import com.wolfeleo2.myuon.server.db.StudentsTable
import com.wolfeleo2.myuon.server.db.UnitsTable
import com.wolfeleo2.myuon.server.domain.AcademicSummary
import com.wolfeleo2.myuon.server.domain.CourseUnit
import com.wolfeleo2.myuon.server.domain.ExamCard
import com.wolfeleo2.myuon.server.domain.GradeRecord
import com.wolfeleo2.myuon.server.domain.StudentProfile
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AcademicRoutesTest {

    private fun setupTestDb(): Pair<Database, ExposedAcademicRepository> {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:academic-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        val academicRepo = ExposedAcademicRepository(db)
        val studentRepo = ExposedStudentRepository(db)

        transaction(db) {
            UnitsTable.insert {
                it[code] = "CSC311"
                it[title] = "Compiler Construction"
                it[credits] = 3
                it[lecturerName] = "Dr. Lawrence Muchemi"
                it[department] = "Computer Science"
                it[yearOfStudy] = 3
                it[semester] = 2
            }
            UnitsTable.insert {
                it[code] = "CSC312"
                it[title] = "Artificial Intelligence"
                it[credits] = 3
                it[lecturerName] = "Prof. Peter Wagacha"
                it[department] = "Computer Science"
                it[yearOfStudy] = 3
                it[semester] = 2
            }
        }
        return Pair(db, academicRepo)
    }

    @Test
    fun `GET units returns available course units for the semester`() = testApplication {
        val (db, repo) = setupTestDb()
        val studentRepo = ExposedStudentRepository(db)

        application {
            install(ContentNegotiation) { json() }
            routing { academicRoutes(repo, studentRepo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.get("/units?semester=2")
        assertEquals(HttpStatusCode.OK, response.status)

        val units = response.body<List<CourseUnit>>()
        assertEquals(2, units.size)
        assertTrue(units.any { it.unitCode == "CSC311" })
    }

    @Test
    fun `POST units register registers student units`() = testApplication {
        val (db, repo) = setupTestDb()
        val studentRepo = ExposedStudentRepository(db)

        studentRepo.create(
            StudentProfile(
                userId = "user-1",
                regNo = "P15/12345/2022",
                fullName = "Jane Student",
                studentEmail = "jane@students.uonbi.ac.ke",
                faculty = "Science",
                department = "Computing",
                program = "BSc Computer Science",
                yearOfStudy = 3,
                semester = 2,
                campus = "Chiromo",
                nationalId = "12345678",
                mobileNumber = "0700000000"
            )
        )

        application {
            install(ContentNegotiation) { json() }
            routing { academicRoutes(repo, studentRepo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/units/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"regNo":"P15/12345/2022","unitCodes":["CSC311","CSC312"],"academicYear":"2025/2026","semester":2}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val registeredResponse = client.get("/units/registered?regNo=P15/12345/2022&semester=2")
        assertEquals(HttpStatusCode.OK, registeredResponse.status)
        val registeredUnits = registeredResponse.body<List<CourseUnit>>()
        assertEquals(2, registeredUnits.size)
    }

    @Test
    fun `GET academics summary computes weighted cumulative GPA`() = testApplication {
        val (db, repo) = setupTestDb()
        val studentRepo = ExposedStudentRepository(db)

        studentRepo.create(
            StudentProfile(
                userId = "user-1",
                regNo = "P15/12345/2022",
                fullName = "Jane Student",
                studentEmail = "jane@students.uonbi.ac.ke",
                faculty = "Science",
                department = "Computing",
                program = "BSc Computer Science",
                yearOfStudy = 3,
                semester = 2,
                campus = "Chiromo",
                nationalId = "12345678",
                mobileNumber = "0700000000"
            )
        )

        transaction(db) {
            GradeRecordsTable.insert {
                it[id] = UUID.randomUUID()
                it[studentId] = "user-1"
                it[unitCode] = "CSC311"
                it[academicYear] = "2025/2026"
                it[semester] = 1
                it[catMark] = 25.0
                it[examMark] = 55.0
                it[totalScore] = 80.0
                it[gradeLetter] = "A"
                it[isPass] = true
            }
        }

        application {
            install(ContentNegotiation) { json() }
            routing { academicRoutes(repo, studentRepo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.get("/academics/summary?regNo=P15/12345/2022")
        assertEquals(HttpStatusCode.OK, response.status)

        val summary = response.body<AcademicSummary>()
        assertEquals(80.0, summary.cumulativeAverage)
        assertEquals("FIRST_CLASS", summary.degreeClass.name)
        assertEquals(1, summary.totalUnitsPassed)
    }

    @Test
    fun `GET exams card generates valid clearance card`() = testApplication {
        val (db, repo) = setupTestDb()
        val studentRepo = ExposedStudentRepository(db)

        studentRepo.create(
            StudentProfile(
                userId = "user-1",
                regNo = "P15/12345/2022",
                fullName = "Jane Student",
                studentEmail = "jane@students.uonbi.ac.ke",
                faculty = "Science",
                department = "Computing",
                program = "BSc Computer Science",
                yearOfStudy = 3,
                semester = 2,
                campus = "Chiromo",
                nationalId = "12345678",
                mobileNumber = "0700000000"
            )
        )

        application {
            install(ContentNegotiation) { json() }
            routing { academicRoutes(repo, studentRepo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.get("/exams/card?regNo=P15/12345/2022")
        assertEquals(HttpStatusCode.OK, response.status)

        val card = response.body<ExamCard>()
        assertEquals("P15/12345/2022", card.regNo)
        assertEquals("Jane Student", card.studentName)
        assertTrue(card.qrVerificationToken.startsWith("UON-VERIFY-"))
    }
}
