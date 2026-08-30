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
        feeRepository.semesterFeeStatement,
        feeRepository.academicYearFeeStatement,
        _scopeMode,
        _selectedYear
    ) { student, semStatement, yearStatement, scope, year ->
        val activeStatement = when (scope) {
            FeeScopeMode.SEMESTER -> semStatement
            FeeScopeMode.ACADEMIC_YEAR -> yearStatement
        }
        FeesUiState(
            student = student,
            feeStatement = activeStatement,
            scopeMode = scope,
            selectedYear = year,
            selectedSemester = if (scope == FeeScopeMode.SEMESTER) 2 else 0
        )
    }.combine(_isProcessing) { state, processing ->
        state.copy(isProcessingPayment = processing)
    }.combine(_message) { state, msg ->
        state.copy(paymentSuccessMessage = msg)
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeesUiState())

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _isRefreshing.value = false
        }
    }

    fun setScopeMode(mode: FeeScopeMode) {
        _scopeMode.value = mode
    }

    fun selectYear(year: String) {
        _selectedYear.value = year
    }

    fun selectSemester(sem: Int) {
        _selectedSemester.value = sem
    }

    fun makeMpesaPayment(amount: Double, code: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val success = feeRepository.recordMpesaPayment(amount, code)
            _isProcessing.value = false
            _message.value = if (success) {
                "Payment of KES ${amount.toInt()} successfully recorded and reconciled with SMIS!"
            } else if (amount > feeRepository.semesterFeeStatement.value.outstandingBalance) {
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
