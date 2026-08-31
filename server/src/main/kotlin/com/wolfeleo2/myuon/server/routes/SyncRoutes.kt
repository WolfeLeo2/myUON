package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.domain.SyncRepository
import com.wolfeleo2.myuon.server.domain.SyncScope
import com.wolfeleo2.myuon.server.sync.SyncChannelManager
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable

@Serializable
data class NotifyEventRequest(
    val scope: SyncScope,
    val regNo: String? = null,
    val description: String? = null
)

fun Route.syncRoutes(
    syncRepository: SyncRepository,
    syncChannelManager: SyncChannelManager
) {
    get("/sync/delta") {
        val since = call.request.queryParameters["since"]
        val regNo = call.request.queryParameters["regNo"]
        val delta = syncRepository.getDeltaPayload(since, regNo)
        call.respond(delta)
    }

    post("/sync/notify") {
        val text = call.receiveText()
        val body = runCatching { kotlinx.serialization.json.Json.decodeFromString<NotifyEventRequest>(text) }.getOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid payload"))

        if (body.regNo != null) {
            syncChannelManager.notifyStudent(body.regNo, body.scope, body.description)
        } else {
            syncChannelManager.broadcast(body.scope, body.description)
        }
        call.respond(mapOf("status" to "NOTIFIED", "activeConnections" to syncChannelManager.activeConnectionsCount.toString()))
    }

    webSocket("/sync/events") {
        val regNo = call.request.queryParameters["regNo"]
        syncChannelManager.register(regNo, this)
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    if (text.equals("ping", ignoreCase = true)) {
                        send(Frame.Text("pong"))
                    }
                }
            }
        } finally {
            syncChannelManager.unregister(regNo, this)
        }
    }
}
