package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedFeeRepository
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import com.wolfeleo2.myuon.server.db.FeeStatementsTable
import com.wolfeleo2.myuon.server.db.FeeTransactionsTable
import com.wolfeleo2.myuon.server.db.StudentsTable
import com.wolfeleo2.myuon.server.domain.FeeStatement
import com.wolfeleo2.myuon.server.domain.FeeTransaction
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

class FeeRoutesTest {

    private fun setupTestDb(): Pair<Database, ExposedFeeRepository> {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:fee-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        val feeRepo = ExposedFeeRepository(db)
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
            FeeStatementsTable.insert {
                it[id] = UUID.randomUUID()
                it[studentId] = "user-1"
                it[academicYear] = "2025/2026"
                it[semester] = 2
                it[totalInvoiced] = 36000.0
                it[totalPaid] = 36000.0
                it[currentBalance] = 0.0
                it[updatedAt] = "2026-08-30"
            }
        }
        return Pair(db, feeRepo)
    }

    @Test
    fun `GET fees statement returns student fee statement`() = testApplication {
        val (_, repo) = setupTestDb()

        application {
            install(ContentNegotiation) { json() }
            routing { feeRoutes(repo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.get("/fees/statement?regNo=P15/12345/2022&academicYear=2025/2026&semester=2")
        assertEquals(HttpStatusCode.OK, response.status)

        val statement = response.body<FeeStatement>()
        assertEquals(36000.0, statement.totalInvoiced)
        assertEquals(0.0, statement.outstandingBalance)
    }

    @Test
    fun `POST fees mpesa-pay records transaction successfully`() = testApplication {
        val (_, repo) = setupTestDb()

        application {
            install(ContentNegotiation) { json() }
            routing { feeRoutes(repo) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/fees/mpesa-pay") {
            contentType(ContentType.Application.Json)
            setBody("""{"regNo":"P15/12345/2022","amount":18000.0,"phoneNumber":"0712345678"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val transaction = response.body<FeeTransaction>()
        assertEquals(18000.0, transaction.amount)
        assertTrue(transaction.referenceNumber.startsWith("QK"))
    }
}
