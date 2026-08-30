package com.wolfeleo2.myuon.server.plugins

import com.wolfeleo2.myuon.server.routes.ErrorResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlin.time.Duration.Companion.minutes

val authRateLimit = RateLimitName("auth")
val apiRateLimit = RateLimitName("api")

fun Application.configureSecurity() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(RateLimit) {
        register(authRateLimit) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
        }
        register(apiRateLimit) {
            rateLimiter(limit = 100, refillPeriod = 1.minutes)
        }
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Bad Request"))
        }
        exception<IllegalStateException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Invalid State"))
        }
        exception<Throwable> { call, cause ->
            org.slf4j.LoggerFactory.getLogger("Security").error("Unhandled server exception: ${cause.message}", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("An internal error occurred. Please try again later."))
        }
    }
}
