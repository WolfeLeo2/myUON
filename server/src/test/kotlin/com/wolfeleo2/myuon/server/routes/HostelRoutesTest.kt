package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedHostelRepository
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import com.wolfeleo2.myuon.server.db.HostelsTable
import com.wolfeleo2.myuon.server.db.StudentsTable
import com.wolfeleo2.myuon.server.domain.HostelHall
import com.wolfeleo2.myuon.server.domain.HostelRoomBooking
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
import kotlin.test.Test
import kotlin.test.assertEquals

class HostelRoutesTest {

    private fun setupTestDb(): Pair<Database, ExposedHostelRepository> {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:hostel-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        val hostelRepo = ExposedHostelRepository(db)
        transaction(db) {
            StudentsTable.insert {
                it[userId] = "user-1"
                it[regNo] = "P15/12345/2022"
                it[fullName] = "Jane Student"
                it[studentEmail] = "jane@students.uonbi.ac.ke"
                it[faculty] = "Science"
                it[department] = "Computing"
                it[program] = "BSc Computer Science"
                it[yearOfStudy] = 3
                it[semester] = 2
                it[campus] = "Chiromo"
                it[nationalId] = "12345678"
                it[mobileNumber] = "0700000000"
            }
            HostelsTable.insert {
                it[id] = "hall-1"
                it[name] = "Hall 9"
                it[campus] = "Main Campus"
                it[gender] = "MALE"
                it[totalRooms] = 120
                it[availableRooms] = 14
                it[ratePerSemester] = 6500.0
            }
        }
        return Pair(db, hostelRepo)
    }

    @Test
    fun `GET hostels returns available halls`() = testApplication {
        val (_, repo) = setupTestDb()

        application {
            install(ContentNegotiation) { json() }
            routing { hostelRoutes(repo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.get("/hostels")
        assertEquals(HttpStatusCode.OK, response.status)

        val halls = response.body<List<HostelHall>>()
        assertEquals(1, halls.size)
        assertEquals("Hall 9", halls[0].hallName)
    }

    @Test
    fun `POST hostels book creates student room booking`() = testApplication {
        val (_, repo) = setupTestDb()

        application {
            install(ContentNegotiation) { json() }
            routing { hostelRoutes(repo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/hostels/book") {
            contentType(ContentType.Application.Json)
            setBody("""{"regNo":"P15/12345/2022","hallName":"Hall 9","roomNumber":"204","bedSpace":"A"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val booking = response.body<HostelRoomBooking>()
        assertEquals("P15/12345/2022", booking.regNo)
        assertEquals("204", booking.roomNumber)
    }
}
