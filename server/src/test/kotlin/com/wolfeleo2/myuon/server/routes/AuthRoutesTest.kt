package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import com.wolfeleo2.myuon.server.domain.AuthService
import com.wolfeleo2.myuon.server.domain.FakeNeonAuthClient
import com.wolfeleo2.myuon.server.domain.NeonAuthResult
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRoutesTest {

    private fun freshAuthService(neonAuth: FakeNeonAuthClient): AuthService {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:routes-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        return AuthService(neonAuth, ExposedStudentRepository(db))
    }

    @Test
    fun `signup creates an account and returns 201`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            val authService = freshAuthService(
                FakeNeonAuthClient(signUpResult = { _, _ -> NeonAuthResult.Success("user-1", "token-1") }),
            )
            routing { authRoutes(authService) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"email":"jane@students.uonbi.ac.ke","password":"password123","regNo":"P15/12345/2022",
                    |"fullName":"Jane Student","faculty":"Science","department":"Computing",
                    |"program":"BSc Computer Science","yearOfStudy":3,"semester":1,"campus":"Main",
                    |"nationalId":"12345678","mobileNumber":"0700000000"}""".trimMargin(),
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText())
        assertEquals("user-1", body.jsonObject["userId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `login with an unknown mode returns 400`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            routing { authRoutes(freshAuthService(FakeNeonAuthClient())) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"mode":"BOGUS","identifier":"a@b.com","password":"x"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login with a wrong password returns 401`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            val authService = freshAuthService(
                FakeNeonAuthClient(signInResult = { _, _ -> NeonAuthResult.Failure("invalid credentials") }),
            )
            routing { authRoutes(authService) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"mode":"AD","identifier":"jane@students.uonbi.ac.ke","password":"wrong"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
