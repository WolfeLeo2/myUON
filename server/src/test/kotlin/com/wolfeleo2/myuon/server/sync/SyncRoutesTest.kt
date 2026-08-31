package com.wolfeleo2.myuon.server.sync

import com.wolfeleo2.myuon.server.domain.CourseUnit
import com.wolfeleo2.myuon.server.domain.SyncDeltaPayload
import com.wolfeleo2.myuon.server.domain.SyncEvent
import com.wolfeleo2.myuon.server.domain.SyncRepository
import com.wolfeleo2.myuon.server.domain.SyncScope
import com.wolfeleo2.myuon.server.routes.NotifyEventRequest
import com.wolfeleo2.myuon.server.routes.syncRoutes
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SyncRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val stubSyncRepository = object : SyncRepository {
        override suspend fun getDeltaPayload(since: String?, regNo: String?): SyncDeltaPayload {
            return SyncDeltaPayload(
                timestamp = "2026-08-30T17:30:00Z",
                units = listOf(
                    CourseUnit(
                        unitCode = "CSC 311",
                        unitTitle = "Advanced Database Systems",
                        credits = 3,
                        academicYear = "2025/2026",
                        semester = 2,
                        lecturerName = "Prof. Peter Wagacha",
                        lecturerEmail = "pwagacha@uonbi.ac.ke",
                        venueName = "Chiromo Lab 02",
                        campus = "Chiromo Campus"
                    )
                )
            )
        }
    }

    @Test
    fun `GET sync delta returns delta payload with units and timestamp`() = testApplication {
        application {
            install(ServerContentNegotiation) { json() }
            install(io.ktor.server.websocket.WebSockets)
            val channelManager = SyncChannelManager()
            routing {
                syncRoutes(stubSyncRepository, channelManager)
            }
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.get("/sync/delta?regNo=P15/12345/2022")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val delta = json.decodeFromString<SyncDeltaPayload>(body)
        assertEquals("2026-08-30T17:30:00Z", delta.timestamp)
        assertEquals(1, delta.units.size)
        assertEquals("CSC 311", delta.units[0].unitCode)
    }

    @Test
    fun `WebSocket connects and receives live broadcast event on notify`() = testApplication {
        val channelManager = SyncChannelManager()

        application {
            install(ServerContentNegotiation) { json() }
            install(io.ktor.server.websocket.WebSockets)
            routing {
                syncRoutes(stubSyncRepository, channelManager)
            }
        }

        val wsClient = createClient {
            install(WebSockets)
        }

        val httpClient = createClient {
            install(ContentNegotiation) { json() }
        }

        wsClient.webSocket("/sync/events?regNo=P15/12345/2022") {
            // Ping test
            send(Frame.Text("ping"))
            val pongFrame = incoming.receive() as Frame.Text
            assertEquals("pong", pongFrame.readText())

            // Trigger notification
            val notifyResponse = httpClient.post("/sync/notify") {
                contentType(ContentType.Application.Json)
                setBody("""{"scope":"UNITS","description":"Syllabus updated"}""")
            }
            assertEquals(HttpStatusCode.OK, notifyResponse.status)

            // Receive broadcast event over WebSocket
            val eventFrame = incoming.receive() as Frame.Text
            val eventText = eventFrame.readText()
            val event = json.decodeFromString<SyncEvent>(eventText)
            assertEquals("DELTA_AVAILABLE", event.eventType)
            assertEquals(SyncScope.UNITS, event.scope)
            assertEquals("Syllabus updated", event.description)
        }
    }
}
