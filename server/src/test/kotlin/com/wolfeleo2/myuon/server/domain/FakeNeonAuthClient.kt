package com.wolfeleo2.myuon.server.domain

class FakeNeonAuthClient(
    private val signUpResult: (String, String) -> NeonAuthResult = { _, _ -> NeonAuthResult.Failure("not stubbed") },
    private val signInResult: (String, String) -> NeonAuthResult = { _, _ -> NeonAuthResult.Failure("not stubbed") },
) : NeonAuthClient {
    val signInCalls = mutableListOf<Pair<String, String>>()

    override suspend fun signUp(email: String, password: String): NeonAuthResult = signUpResult(email, password)

    override suspend fun signIn(email: String, password: String): NeonAuthResult {
        signInCalls += email to password
        return signInResult(email, password)
    }
}
