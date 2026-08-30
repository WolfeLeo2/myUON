package com.wolfeleo2.myuon.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfeleo2.myuon.data.model.*
import com.wolfeleo2.myuon.data.repo.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val student: StudentProfile? = null,
    val upcomingExam: ExamCardItem? = null,
    val todayClasses: List<TimetableItem> = emptyList(),
    val feeStatement: FeeStatement? = null,
    val activeHostelBooking: HostelRoomBooking? = null,
    val academicSummary: AcademicSummary = AcademicSummary(),
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val academicRepository: AcademicRepository,
    private val feeRepository: FeeRepository,
    private val timetableRepository: TimetableRepository,
    private val hostelRepository: HostelRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        authRepository.studentProfile,
        academicRepository.examCard,
        timetableRepository.timetableSlots,
        feeRepository.semesterFeeStatement,
        hostelRepository.activeBooking
    ) { student, examCard, slots, fee, booking ->
        val nextExam = examCard.units.firstOrNull()
        val todaySlots = slots.take(2)
        DashboardUiState(
            student = student,
            upcomingExam = nextExam,
            todayClasses = todaySlots,
            feeStatement = fee,
            activeHostelBooking = booking
        )
    }.combine(academicRepository.academicSummary) { state, summary ->
        state.copy(academicSummary = summary)
    }.combine(_isSyncing) { state, syncing ->
        state.copy(isSyncing = syncing)
    }.combine(_isLoading) { state, loading ->
        state.copy(isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun refreshPortalData() {
        viewModelScope.launch {
            _isSyncing.value = true
            // Trigger remote repository refreshes
            _isSyncing.value = false
        }
    }
}
