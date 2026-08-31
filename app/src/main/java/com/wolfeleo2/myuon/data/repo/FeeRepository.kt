package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.db.FeeDao
import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.FeeStatement
import com.wolfeleo2.myuon.data.model.FeeTransaction
import com.wolfeleo2.myuon.data.model.TransactionType
import com.wolfeleo2.myuon.data.preferences.UserPreferencesDataStore
import com.wolfeleo2.myuon.data.remote.MyUonApiClient
import com.wolfeleo2.myuon.ui.fees.FeeScopeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeeRepository @Inject constructor(
    private val apiClient: MyUonApiClient,
    private val feeDao: FeeDao,
    private val preferencesDataStore: UserPreferencesDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _currentSemesterFeeStatement = MutableStateFlow<FeeStatement?>(null)
    private val _scopedFeeStatement = MutableStateFlow<FeeStatement?>(null)
    private val _academicYearFeeStatement = MutableStateFlow<FeeStatement?>(null)

    val currentSemesterFeeStatement: StateFlow<FeeStatement?> = _currentSemesterFeeStatement.asStateFlow()
    val scopedFeeStatement: StateFlow<FeeStatement?> = _scopedFeeStatement.asStateFlow()
    val semesterFeeStatement: StateFlow<FeeStatement?> = _currentSemesterFeeStatement.asStateFlow()
    val academicYearFeeStatement: StateFlow<FeeStatement?> = _academicYearFeeStatement.asStateFlow()
    val feeStatement: StateFlow<FeeStatement?> = _currentSemesterFeeStatement.asStateFlow()

    init {
        scope.launch {
            // Load cached statements first for instantaneous offline availability
            val cachedStatements = feeDao.getAllFeeStatements().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            val cachedSem = cachedStatements.firstOrNull { it.academicYear == "2025/2026" && it.semester == 2 }
                ?: cachedStatements.firstOrNull { it.semester != 0 }
            val cachedYear = cachedStatements.firstOrNull { it.academicYear == "2025/2026" && it.semester == 0 }
                ?: cachedStatements.firstOrNull { it.semester == 0 }

            if (cachedSem != null) {
                _currentSemesterFeeStatement.value = cachedSem
                _scopedFeeStatement.value = cachedSem
            }
            if (cachedYear != null) {
                _academicYearFeeStatement.value = cachedYear
            }

            preferencesDataStore.activeStudentRegNo.collect { regNo ->
                if (!regNo.isNullOrBlank()) {
                    refreshFromRemote(regNo, "2025/2026", 2)
                    fetchAcademicYearStatement(regNo, "2025/2026")
                }
            }
        }
    }

    suspend fun refreshFromRemote(regNo: String, year: String = "2025/2026", sem: Int = 2) {
        val remoteStatement = apiClient.getFeeStatement(regNo, year, sem)
        if (remoteStatement != null) {
            if (year == "2025/2026" && sem == 2) {
                _currentSemesterFeeStatement.value = remoteStatement
            }
            if (_scopedFeeStatement.value?.academicYear == year && _scopedFeeStatement.value?.semester == sem) {
                _scopedFeeStatement.value = remoteStatement
            } else if (_scopedFeeStatement.value == null && year == "2025/2026" && sem == 2) {
                _scopedFeeStatement.value = remoteStatement
            }
            feeDao.insertStatement(remoteStatement.toEntity())
        }
    }

    suspend fun fetchAcademicYearStatement(regNo: String, year: String = "2025/2026") {
        val remoteYearStatement = apiClient.getFeeStatement(regNo, year, sem = 0)
        if (remoteYearStatement != null) {
            _academicYearFeeStatement.value = remoteYearStatement
            feeDao.insertStatement(remoteYearStatement.toEntity())
        }
    }

    suspend fun loadStatementForScope(regNo: String, year: String, sem: Int, mode: FeeScopeMode) {
        if (mode == FeeScopeMode.SEMESTER) {
            val cached = feeDao.getFeeStatement(year, sem).firstOrNull()?.toDomain()
            if (cached != null) {
                _scopedFeeStatement.value = cached
                if (year == "2025/2026" && sem == 2) {
                    _currentSemesterFeeStatement.value = cached
                }
            }

            val statement = apiClient.getFeeStatement(regNo, year, sem)
            if (statement != null) {
                _scopedFeeStatement.value = statement
                if (year == "2025/2026" && sem == 2) {
                    _currentSemesterFeeStatement.value = statement
                }
                feeDao.insertStatement(statement.toEntity())
            }
        } else {
            val cached = feeDao.getFeeStatement(year, 0).firstOrNull()?.toDomain()
            if (cached != null) {
                _scopedFeeStatement.value = cached
                _academicYearFeeStatement.value = cached
            }

            val statement = apiClient.getFeeStatement(regNo, year, sem = 0)
            if (statement != null) {
                _scopedFeeStatement.value = statement
                _academicYearFeeStatement.value = statement
                feeDao.insertStatement(statement.toEntity())
            }
        }
    }

    fun getFeeStatement(scopeMode: FeeScopeMode): FeeStatement? {
        return when (scopeMode) {
            FeeScopeMode.SEMESTER -> _scopedFeeStatement.value ?: _currentSemesterFeeStatement.value
            FeeScopeMode.ACADEMIC_YEAR -> _academicYearFeeStatement.value
        }
    }

    suspend fun recordMpesaPayment(amount: Double, mpesaCode: String, regNo: String? = null): Boolean {
        val studentRegNo = regNo ?: preferencesDataStore.activeStudentRegNo.firstOrNull() ?: return false
        val currentSem = _currentSemesterFeeStatement.value ?: return false
        if (amount <= 0.0 || amount > currentSem.outstandingBalance) return false

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val refCode = mpesaCode.ifBlank { "MP${UUID.randomUUID().toString().take(8).uppercase()}" }
        val dateStr = dateFormat.format(Date())

        apiClient.recordMpesaPayment(studentRegNo, amount, "0712345678")

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
        val updatedSemStatement = currentSem.copy(
            totalPaid = currentSem.totalPaid + amount,
            outstandingBalance = newSemBalance,
            transactions = listOf(newSemTx) + currentSem.transactions
        )
        _currentSemesterFeeStatement.value = updatedSemStatement
        if (_scopedFeeStatement.value?.academicYear == currentSem.academicYear && _scopedFeeStatement.value?.semester == currentSem.semester) {
            _scopedFeeStatement.value = updatedSemStatement
        }

        val currentYear = _academicYearFeeStatement.value
        if (currentYear != null) {
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
            val updatedYearStatement = currentYear.copy(
                totalPaid = currentYear.totalPaid + amount,
                outstandingBalance = newYearBalance,
                transactions = listOf(newYearTx) + currentYear.transactions
            )
            _academicYearFeeStatement.value = updatedYearStatement

            scope.launch {
                feeDao.insertStatements(listOf(updatedSemStatement.toEntity(), updatedYearStatement.toEntity()))
            }
        } else {
            scope.launch {
                feeDao.insertStatement(updatedSemStatement.toEntity())
            }
        }
        return true
    }
}
