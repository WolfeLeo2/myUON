package com.wolfeleo2.myuon.server.domain

interface FeeRepository {
    suspend fun getFeeStatement(regNo: String, academicYear: String, semester: Int): FeeStatement?
    suspend fun getFeeTransactions(regNo: String): List<FeeTransaction>
    suspend fun recordTransaction(transaction: FeeTransaction): FeeTransaction
}
