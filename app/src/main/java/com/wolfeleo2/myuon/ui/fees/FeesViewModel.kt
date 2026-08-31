package com.wolfeleo2.myuon.ui.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfeleo2.myuon.data.model.FeeStatement
import com.wolfeleo2.myuon.data.model.StudentProfile
import com.wolfeleo2.myuon.data.repo.AuthRepository
import com.wolfeleo2.myuon.data.repo.FeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FeeScopeMode {
    SEMESTER,
    ACADEMIC_YEAR
}

data class FeesUiState(
    val student: StudentProfile? = null,
    val feeStatement: FeeStatement? = null,
    val scopeMode: FeeScopeMode = FeeScopeMode.SEMESTER,
    val selectedYear: String = "2025/2026",
    val selectedSemester: Int = 2,
    val availableYears: List<String> = listOf("2023/2024", "2024/2025", "2025/2026", "2026/2027"),
    val isProcessingPayment: Boolean = false,
    val paymentSuccessMessage: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class FeesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val feeRepository: FeeRepository
) : ViewModel() {

    private val _scopeMode = MutableStateFlow(FeeScopeMode.SEMESTER)
    private val _selectedYear = MutableStateFlow("2025/2026")
    private val _selectedSemester = MutableStateFlow(2)
    private val _isProcessing = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<FeesUiState> = combine(
        authRepository.studentProfile,
        feeRepository.scopedFeeStatement,
        _scopeMode,
        _selectedYear,
        _selectedSemester
    ) { student, scopedStatement, scope, year, sem ->
        FeesUiState(
            student = student,
            feeStatement = scopedStatement,
            scopeMode = scope,
            selectedYear = year,
            selectedSemester = sem
        )
    }.combine(_isProcessing) { state, processing ->
        state.copy(isProcessingPayment = processing)
    }.combine(_message) { state, msg ->
        state.copy(paymentSuccessMessage = msg)
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeesUiState())

    init {
        viewModelScope.launch {
            authRepository.studentProfile.collect { student ->
                if (student != null) {
                    feeRepository.loadStatementForScope(student.regNo, _selectedYear.value, _selectedSemester.value, _scopeMode.value)
                }
            }
        }
    }

    fun refreshData() {
        val regNo = authRepository.studentProfile.value?.regNo ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            feeRepository.loadStatementForScope(regNo, _selectedYear.value, _selectedSemester.value, _scopeMode.value)
            _isRefreshing.value = false
        }
    }

    fun setScopeMode(mode: FeeScopeMode) {
        _scopeMode.value = mode
        val regNo = authRepository.studentProfile.value?.regNo ?: return
        viewModelScope.launch {
            feeRepository.loadStatementForScope(regNo, _selectedYear.value, _selectedSemester.value, mode)
        }
    }

    fun selectYear(year: String) {
        _selectedYear.value = year
        val regNo = authRepository.studentProfile.value?.regNo ?: return
        viewModelScope.launch {
            feeRepository.loadStatementForScope(regNo, year, _selectedSemester.value, _scopeMode.value)
        }
    }

    fun selectSemester(sem: Int) {
        _selectedSemester.value = sem
        val regNo = authRepository.studentProfile.value?.regNo ?: return
        viewModelScope.launch {
            feeRepository.loadStatementForScope(regNo, _selectedYear.value, sem, _scopeMode.value)
        }
    }

    fun makeMpesaPayment(amount: Double, code: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val success = feeRepository.recordMpesaPayment(amount, code)
            _isProcessing.value = false
            _message.value = if (success) {
                "Payment of KES ${amount.toInt()} successfully recorded and reconciled with SMIS!"
            } else if (amount > (uiState.value.feeStatement?.outstandingBalance ?: 0.0)) {
                "Payment of KES ${amount.toInt()} exceeds your outstanding balance."
            } else {
                "Payment could not be processed."
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
