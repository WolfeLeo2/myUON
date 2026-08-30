package com.wolfeleo2.myuon.server.infra

import com.wolfeleo2.myuon.server.domain.NeonAuthClient
import com.wolfeleo2.myuon.server.domain.NeonAuthResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

class NeonAuthHttpClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val projectId: String = "",
    private val serverSecretKey: String = "",
    private val signUpPath: String = "/sign-up/email",
    private val signInPath: String = "/sign-in/email",
) : NeonAuthClient {

    @Serializable
    private data class CredentialsRequest(val email: String, val password: String, val name: String = "")

    @Serializable
    private data class UserResponse(val id: String)

    @Serializable
    private data class AuthResponse(
        val token: String = "",
        val session: SessionResponse? = null,
        val user: UserResponse? = null,
        val userId: String? = null,
        val sessionToken: String? = null
    )

    @Serializable
    private data class SessionResponse(val token: String = "", val userId: String = "")

    override suspend fun signUp(email: String, password: String): NeonAuthResult =
        request(signUpPath, email, password)

    override suspend fun signIn(email: String, password: String): NeonAuthResult =
        request(signInPath, email, password)

    private suspend fun request(path: String, email: String, password: String): NeonAuthResult {
        val response = httpClient.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            if (projectId.isNotBlank()) header("x-stack-project-id", projectId)
            if (serverSecretKey.isNotBlank()) header("x-stack-secret-server-key", serverSecretKey)
            setBody(CredentialsRequest(email = email, password = password, name = email.substringBefore("@")))
        }
        return if (response.status.isSuccess()) {
            val parsed = response.body<AuthResponse>()
            val userId = parsed.userId ?: parsed.user?.id ?: parsed.session?.userId ?: "user-${System.currentTimeMillis()}"
            val token = parsed.sessionToken ?: parsed.session?.token ?: parsed.token
            NeonAuthResult.Success(userId, token)
        } else {
            NeonAuthResult.Failure(response.bodyAsText())
        }
    }
}
