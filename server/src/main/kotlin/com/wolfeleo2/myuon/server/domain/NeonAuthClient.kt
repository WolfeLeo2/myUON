package com.wolfeleo2.myuon.server.domain

sealed class NeonAuthResult {
    data class Success(val userId: String, val sessionToken: String) : NeonAuthResult()
    data class Failure(val message: String) : NeonAuthResult()
}

interface NeonAuthClient {
    suspend fun signUp(email: String, password: String): NeonAuthResult
    suspend fun signIn(email: String, password: String): NeonAuthResult
}
