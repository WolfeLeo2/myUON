package com.wolfeleo2.myuon.server.domain

interface SyncRepository {
    suspend fun getDeltaPayload(since: String?, regNo: String?): SyncDeltaPayload
}
