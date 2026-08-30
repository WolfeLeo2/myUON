package com.wolfeleo2.myuon.server.infra

import com.wolfeleo2.myuon.server.domain.NeonAuthResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NeonAuthHttpClientTest {

    @Test
    fun `signIn sends project and secret headers and parses a successful response`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("https://auth.example.neon.tech/sign-in/email", request.url.toString())
            assertEquals("proj-123", request.headers["x-stack-project-id"])
            assertEquals("secret-abc", request.headers["x-stack-secret-server-key"])
            val bodyText = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
            assertTrue(bodyText.contains("\"email\":\"a@b.com\""))

            respond(
                content = """{"userId":"user-1","sessionToken":"token-1"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = NeonAuthHttpClient(
            httpClient = HttpClient(engine) { install(ContentNegotiation) { json() } },
            baseUrl = "https://auth.example.neon.tech",
            projectId = "proj-123",
            serverSecretKey = "secret-abc",
        )

        val result = client.signIn("a@b.com", "password123")

        val success = assertIs<NeonAuthResult.Success>(result)
        assertEquals("user-1", success.userId)
        assertEquals("token-1", success.sessionToken)
    }

    @Test
    fun `signIn returns Failure with the response body on a non-2xx status`() = runTest {
        val engine = MockEngine {
            respond(content = """{"message":"invalid credentials"}""", status = HttpStatusCode.Unauthorized)
        }
        val client = NeonAuthHttpClient(
            httpClient = HttpClient(engine) { install(ContentNegotiation) { json() } },
            baseUrl = "https://auth.example.neon.tech",
            projectId = "proj-123",
            serverSecretKey = "secret-abc",
        )

        val result = client.signIn("a@b.com", "wrong-password")

        val failure = assertIs<NeonAuthResult.Failure>(result)
        assertTrue(failure.message.contains("invalid credentials"))
    }
}
