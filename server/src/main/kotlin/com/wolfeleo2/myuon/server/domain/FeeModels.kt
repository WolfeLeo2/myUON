package com.wolfeleo2.myuon.server.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    INVOICE,
    PAYMENT_MPESA,
    PAYMENT_BANK,
    HELB_DISBURSEMENT,
    HEF_SCHOLARSHIP,
    WAIVER
}

@Serializable
data class FeeTransaction(
    val id: String,
    val regNo: String,
    val referenceNumber: String,
    val date: String,
    val description: String,
    val type: TransactionType,
    val amount: Double,
    val balanceAfter: Double,
    val isVerified: Boolean = true
)

@Serializable
data class FeeStatement(
    val regNo: String,
    val academicYear: String,
    val semester: Int,
    val totalInvoiced: Double,
    val totalPaid: Double,
    val outstandingBalance: Double,
    val helbDisbursed: Double,
    val hefScholarship: Double,
    val transactions: List<FeeTransaction> = emptyList(),
    val invoiceBreakdown: Map<String, Double> = emptyMap()
)
