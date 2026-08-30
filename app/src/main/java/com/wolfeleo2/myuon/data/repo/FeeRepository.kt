package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.model.FeeStatement
import com.wolfeleo2.myuon.data.model.FeeTransaction
import com.wolfeleo2.myuon.data.model.TransactionType
import com.wolfeleo2.myuon.data.remote.MyUonApiClient
import com.wolfeleo2.myuon.ui.fees.FeeScopeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeeRepository @Inject constructor(
    private val apiClient: MyUonApiClient
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val defaultStatement = FeeStatement(
        academicYear = "2025/2026",
        semester = 2,
        totalInvoiced = 78500.0,
        totalPaid = 78500.0,
        outstandingBalance = 0.0,
        helbDisbursed = 32000.0,
        hefScholarship = 28000.0,
        invoiceBreakdown = mapOf(
            "Tuition Fees" to 62000.0,
            "Examination Fee" to 5000.0,
            "Medical Levy" to 3000.0,
            "Computer & Internet Lab" to 5000.0,
            "Library Fee" to 2000.0,
            "Activity & Sports Fee" to 1000.0,
            "UNSA Student Union" to 500.0
        ),
        transactions = listOf(
            FeeTransaction("TX-89211", "SLP9381948", "15 May 2026", "Semester 2 Tuition Invoice", TransactionType.INVOICE, 78500.0, 78500.0),
            FeeTransaction("TX-89212", "HEF20260520", "20 May 2026", "HEF GoK Band 2 Capitation Scholarship", TransactionType.HEF_SCHOLARSHIP, -28000.0, 50500.0),
            FeeTransaction("TX-89213", "HLB9948172", "22 May 2026", "HELB Loan Semester 2 Disbursement", TransactionType.HELB_DISBURSEMENT, -32000.0, 18500.0),
            FeeTransaction("TX-89214", "QK89217482", "28 May 2026", "M-Pesa Student Fee Top-up via eCitizen", TransactionType.PAYMENT_MPESA, -18500.0, 0.0)
        )
    )

    private val defaultYearStatement = FeeStatement(
        academicYear = "2025/2026",
        semester = 2,
        totalInvoiced = 157000.0,
        totalPaid = 157000.0,
        outstandingBalance = 0.0,
        helbDisbursed = 64000.0,
        hefScholarship = 56000.0,
        invoiceBreakdown = mapOf(
            "Tuition Fees (Sem 1 & 2)" to 124000.0,
            "Examination Fees" to 10000.0,
            "Medical Levy" to 6000.0,
            "Computer & Lab Levies" to 10000.0,
            "Library & Activity" to 6000.0,
            "Student Union" to 1000.0
        ),
        transactions = defaultStatement.transactions
    )

    private val _semesterFeeStatement = MutableStateFlow<FeeStatement>(defaultStatement)
    private val _academicYearFeeStatement = MutableStateFlow<FeeStatement>(defaultYearStatement)

    val semesterFeeStatement: StateFlow<FeeStatement> = _semesterFeeStatement.asStateFlow()
    val academicYearFeeStatement: StateFlow<FeeStatement> = _academicYearFeeStatement.asStateFlow()
    val feeStatement: StateFlow<FeeStatement> = _semesterFeeStatement.asStateFlow()

    init {
        scope.launch {
            refreshFromRemote("P15/12345/2022")
        }
    }

    suspend fun refreshFromRemote(regNo: String) {
        val remoteStatement = apiClient.getFeeStatement(regNo)
        if (remoteStatement != null) {
            _semesterFeeStatement.value = remoteStatement
        }
    }

    fun getFeeStatement(scopeMode: FeeScopeMode): FeeStatement {
        return when (scopeMode) {
            FeeScopeMode.SEMESTER -> _semesterFeeStatement.value
            FeeScopeMode.ACADEMIC_YEAR -> _academicYearFeeStatement.value
        }
    }

    suspend fun recordMpesaPayment(amount: Double, mpesaCode: String, regNo: String = "P15/12345/2022"): Boolean {
        val currentSemForValidation = _semesterFeeStatement.value
        if (amount <= 0.0 || amount > currentSemForValidation.outstandingBalance) return false

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val refCode = mpesaCode.ifBlank { "MP${UUID.randomUUID().toString().take(8).uppercase()}" }
        val dateStr = dateFormat.format(Date())

        apiClient.recordMpesaPayment(regNo, amount, "0712345678")

        val currentSem = _semesterFeeStatement.value
        val newSemBalance = (currentSem.outstandingBalance - amount).coerceAtLeast(0.0)
        val newSemTx = FeeTransaction(
            id = "TX-${UUID.randomUUID().toString().take(6).uppercase()}",
            referenceNumber = refCode,
            date = dateStr,
            description = "M-Pesa eCitizen Paybill (300059) Student Payment",
            type = TransactionType.PAYMENT_MPESA,
            amount = -amount,
            balanceAfter = newSemBalance,
            isVerified = true
        )
        _semesterFeeStatement.value = currentSem.copy(
            totalPaid = currentSem.totalPaid + amount,
            outstandingBalance = newSemBalance,
            transactions = listOf(newSemTx) + currentSem.transactions
        )

        val currentYear = _academicYearFeeStatement.value
        val newYearBalance = (currentYear.outstandingBalance - amount).coerceAtLeast(0.0)
        val newYearTx = FeeTransaction(
            id = "TX-${UUID.randomUUID().toString().take(6).uppercase()}",
            referenceNumber = refCode,
            date = dateStr,
            description = "M-Pesa eCitizen Paybill (300059) Student Payment",
            type = TransactionType.PAYMENT_MPESA,
            amount = -amount,
            balanceAfter = newYearBalance,
            isVerified = true
        )
        _academicYearFeeStatement.value = currentYear.copy(
            totalPaid = currentYear.totalPaid + amount,
            outstandingBalance = newYearBalance,
            transactions = listOf(newYearTx) + currentYear.transactions
        )
        return true
    }
}
