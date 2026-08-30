package com.wolfeleo2.myuon.ui.academics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfeleo2.myuon.data.model.*
import com.wolfeleo2.myuon.data.repo.AcademicRepository
import com.wolfeleo2.myuon.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AcademicsTab {
    GRADES,
    UNIT_REGISTRATION,
    EXAM_SERVICES
}

data class AcademicsUiState(
    val student: StudentProfile? = null,
    val selectedTab: AcademicsTab = AcademicsTab.GRADES,
    val selectedYear: String = "2025/2026",
    val selectedSemester: Int = 1,
    val availableYears: List<String> = listOf("2025/2026", "2024/2025"),
    val gradeRecords: List<GradeRecord> = emptyList(),
    val courseUnits: List<CourseUnit> = emptyList(),
    val examCard: ExamCard? = null,
    val specialExamRequests: List<SpecialExamRequest> = emptyList(),
    val supplementaryRequests: List<SupplementaryRequest> = emptyList(),
    val missingMarksDisputes: List<MissingMarksDispute> = emptyList(),
    val isRegistering: Boolean = false,
    val registrationMessage: String? = null,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class AcademicsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val academicRepository: AcademicRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AcademicsTab.GRADES)
    private val _selectedYear = MutableStateFlow("2025/2026")
    private val _selectedSemester = MutableStateFlow(1)
    private val _isRegistering = MutableStateFlow(false)
    private val _registrationMessage = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<AcademicsUiState> = combine(
        authRepository.studentProfile,
        _selectedTab,
        _selectedYear,
        _selectedSemester,
        academicRepository.gradeRecords
    ) { student, tab, year, sem, grades ->
        val filteredGrades = grades.filter { it.academicYear == year && it.semester == sem }
        AcademicsUiState(
            student = student,
            selectedTab = tab,
            selectedYear = year,
            selectedSemester = sem,
            gradeRecords = if (filteredGrades.isNotEmpty()) filteredGrades else grades.take(5)
        )
    }.combine(academicRepository.courseUnits) { state, units ->
        state.copy(courseUnits = units)
    }.combine(academicRepository.examCard) { state, examCard ->
        state.copy(examCard = examCard)
    }.combine(academicRepository.specialExamRequests) { state, reqs ->
        state.copy(specialExamRequests = reqs)
    }.combine(academicRepository.supplementaryRequests) { state, supps ->
        state.copy(supplementaryRequests = supps)
    }.combine(academicRepository.missingMarksDisputes) { state, disputes ->
        state.copy(missingMarksDisputes = disputes)
    }.combine(_isRegistering) { state, registering ->
        state.copy(isRegistering = registering)
    }.combine(_registrationMessage) { state, msg ->
        state.copy(registrationMessage = msg)
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.combine(_isLoading) { state, loading ->
        state.copy(isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AcademicsUiState())

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _isRefreshing.value = false
        }
    }

    fun selectTab(tab: AcademicsTab) {
        _selectedTab.value = tab
    }

    fun selectYear(year: String) {
        _selectedYear.value = year
    }

    fun selectSemester(sem: Int) {
        _selectedSemester.value = sem
    }

    fun toggleUnit(unitCode: String) {
        academicRepository.toggleUnitSelection(unitCode)
    }

    fun submitUnitRegistration() {
        viewModelScope.launch {
            _isRegistering.value = true
            academicRepository.submitUnitRegistration()
            _isRegistering.value = false
            _registrationMessage.value = "Units successfully registered and forwarded to Chairman of Department!"
        }
    }

    fun clearMessage() {
        _registrationMessage.value = null
    }

    fun submitSpecialExam(unitCode: String, unitTitle: String, reason: String, explanation: String): Boolean {
        val regNo = uiState.value.student?.regNo ?: "P15/12345/2022"
        val success = academicRepository.submitSpecialExamRequest(regNo, unitCode, unitTitle, reason, explanation)
        _registrationMessage.value = if (success) {
            "Special exam request submitted."
        } else {
            "You already have a request in progress for this unit."
        }
        return success
    }

    fun submitSupplementary(unitCode: String, unitTitle: String, previousScore: Double, paymentRef: String): Boolean {
        val regNo = uiState.value.student?.regNo ?: "P15/12345/2022"
        val success = academicRepository.submitSupplementaryRequest(regNo, unitCode, unitTitle, previousScore, paymentRef)
        _registrationMessage.value = if (success) {
            "Supplementary exam registered."
        } else {
            "You already have a request in progress for this unit."
        }
        return success
    }

    fun submitMissingMarks(unitCode: String, unitTitle: String, lecturer: String, component: String, note: String): Boolean {
        val regNo = uiState.value.student?.regNo ?: "P15/12345/2022"
        val success = academicRepository.submitMissingMarksDispute(regNo, unitCode, unitTitle, lecturer, component, note)
        _registrationMessage.value = if (success) {
            "Missing marks dispute submitted."
        } else {
            "You already have a request in progress for this unit."
        }
        return success
    }
}
