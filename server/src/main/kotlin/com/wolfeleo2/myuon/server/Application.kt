package com.wolfeleo2.myuon.server

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedAcademicRepository
import com.wolfeleo2.myuon.server.db.ExposedFeeRepository
import com.wolfeleo2.myuon.server.db.ExposedHostelRepository
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import com.wolfeleo2.myuon.server.db.ExposedSyncRepository
import com.wolfeleo2.myuon.server.domain.AuthService
import com.wolfeleo2.myuon.server.infra.NeonAuthHttpClient
import com.wolfeleo2.myuon.server.plugins.apiRateLimit
import com.wolfeleo2.myuon.server.plugins.authRateLimit
import com.wolfeleo2.myuon.server.plugins.configureSecurity
import com.wolfeleo2.myuon.server.routes.academicRoutes
import com.wolfeleo2.myuon.server.routes.authRoutes
import com.wolfeleo2.myuon.server.routes.feeRoutes
import com.wolfeleo2.myuon.server.routes.hostelRoutes
import com.wolfeleo2.myuon.server.routes.syncRoutes
import com.wolfeleo2.myuon.server.sync.SyncChannelManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.slf4j.event.Level
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlin.time.Duration.Companion.seconds
import java.io.File

object EnvConfig {
    private val localEnv: Map<String, String> by lazy {
        val files = listOf(
            File("server/.env.local"),
            File(".env.local"),
            File("server/.env"),
            File(".env")
        )
        val map = mutableMapOf<String, String>()
        for (file in files) {
            if (file.exists() && file.isFile) {
                file.readLines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotBlank() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        val key = trimmed.substringBefore("=").trim()
                        val value = trimmed.substringAfter("=").trim().trim('"', '\'')
                        if (key.isNotBlank()) {
                            map.putIfAbsent(key, value)
                        }
                    }
                }
            }
        }
        map
    }

    fun get(name: String): String? = System.getenv(name) ?: localEnv[name]

    fun require(name: String): String =
        get(name) ?: error("Missing required environment variable: $name")
}

fun main() {
    val port = EnvConfig.get("PORT")?.toIntOrNull() ?: 8080
    val isDev = System.getProperty("io.ktor.development")?.toBooleanStrictOrNull()
        ?: System.getenv("io.ktor.development")?.toBooleanStrictOrNull()
        ?: (System.getenv("KTOR_ENV")?.lowercase() == "development" || System.getenv("KTOR_ENV") == null)

    System.setProperty("io.ktor.development", isDev.toString())

    embeddedServer(
        factory = Netty,
        port = port,
        watchPaths = if (isDev) listOf("classes", "resources") else emptyList(),
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
        )
    }
    install(CallLogging) {
        level = Level.INFO
    }
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    configureSecurity()

    val rawDbUrl = requireEnv("DATABASE_URL")
    val parsedDb = com.wolfeleo2.myuon.server.db.PostgresUrlParser.parse(
        rawUrl = rawDbUrl,
        defaultUser = EnvConfig.get("DATABASE_USER") ?: "",
        defaultPassword = EnvConfig.get("DATABASE_PASSWORD") ?: ""
    )

    val database = DatabaseFactory.connect(
        url = parsedDb.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = parsedDb.user,
        password = parsedDb.password,
    )
    val studentRepository = ExposedStudentRepository(database)
    val academicRepository = ExposedAcademicRepository(database)
    val feeRepository = ExposedFeeRepository(database)
    val hostelRepository = ExposedHostelRepository(database)
    val syncRepository = ExposedSyncRepository(
        studentRepository = studentRepository,
        academicRepository = academicRepository,
        feeRepository = feeRepository,
        hostelRepository = hostelRepository
    )
    val syncChannelManager = SyncChannelManager()

    val httpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }
    val neonAuthClient = NeonAuthHttpClient(
        httpClient = httpClient,
        baseUrl = requireEnv("NEON_AUTH_BASE_URL"),
        projectId = EnvConfig.get("NEON_AUTH_PROJECT_ID") ?: "",
        serverSecretKey = EnvConfig.get("NEON_AUTH_SERVER_SECRET_KEY") ?: "",
    )
    val authService = AuthService(neonAuthClient, studentRepository)

    routing {
        get("/health") { call.respondText("OK") }
        swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml")

        syncRoutes(syncRepository, syncChannelManager)

        rateLimit(authRateLimit) {
            authRoutes(authService)
        }

        rateLimit(apiRateLimit) {
            academicRoutes(academicRepository, studentRepository)
            feeRoutes(feeRepository)
            hostelRoutes(hostelRepository)
        }
    }
}

internal fun requireEnv(name: String): String = EnvConfig.require(name)
