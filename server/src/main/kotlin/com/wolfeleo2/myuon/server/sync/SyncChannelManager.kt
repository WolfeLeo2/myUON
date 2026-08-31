package com.wolfeleo2.myuon.server.sync

import com.wolfeleo2.myuon.server.domain.SyncEvent
import com.wolfeleo2.myuon.server.domain.SyncScope
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class SyncChannelManager {
    private val sessions = ConcurrentHashMap<String, MutableSet<DefaultWebSocketSession>>()
    private val broadcastSessions = ConcurrentHashMap.newKeySet<DefaultWebSocketSession>()
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun register(regNo: String?, session: DefaultWebSocketSession) = mutex.withLock {
        broadcastSessions.add(session)
        if (!regNo.isNullOrBlank()) {
            val set = sessions.computeIfAbsent(regNo.trim().uppercase()) { ConcurrentHashMap.newKeySet() }
            set.add(session)
        }
    }

    suspend fun unregister(regNo: String?, session: DefaultWebSocketSession) = mutex.withLock {
        broadcastSessions.remove(session)
        if (!regNo.isNullOrBlank()) {
            sessions[regNo.trim().uppercase()]?.remove(session)
        }
    }

    suspend fun broadcast(scope: SyncScope, description: String? = null) {
        val event = SyncEvent(
            eventType = "DELTA_AVAILABLE",
            scope = scope,
            timestamp = Instant.now().toString(),
            description = description
        )
        val text = json.encodeToString(event)
        broadcastSessions.forEach { session ->
            runCatching {
                session.send(Frame.Text(text))
            }
        }
    }

    suspend fun notifyStudent(regNo: String, scope: SyncScope, description: String? = null) {
        val event = SyncEvent(
            eventType = "DELTA_AVAILABLE",
            scope = scope,
            regNo = regNo,
            timestamp = Instant.now().toString(),
            description = description
        )
        val text = json.encodeToString(event)
        sessions[regNo.trim().uppercase()]?.forEach { session ->
            runCatching {
                session.send(Frame.Text(text))
            }
        }
    }

    val activeConnectionsCount: Int get() = broadcastSessions.size
}
