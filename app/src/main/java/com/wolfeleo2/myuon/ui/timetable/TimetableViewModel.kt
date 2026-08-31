package com.wolfeleo2.myuon.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfeleo2.myuon.data.model.StudentProfile
import com.wolfeleo2.myuon.data.model.TimetableItem
import com.wolfeleo2.myuon.data.repo.AuthRepository
import com.wolfeleo2.myuon.data.repo.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimetableUiState(
    val student: StudentProfile? = null,
    val selectedDay: String = "Monday",
    val days: List<String> = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
    val allSlots: List<TimetableItem> = emptyList(),
    val filteredSlots: List<TimetableItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow("Monday")
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    
    val uiState: StateFlow<TimetableUiState> = combine(
        authRepository.studentProfile,
        timetableRepository.timetableSlots,
        _selectedDay,
        _isRefreshing
    ) { student, slots, day, refreshing ->
        TimetableUiState(
            student = student,
            selectedDay = day,
            allSlots = slots,
            filteredSlots = slots.filter { it.dayOfWeek.equals(day, ignoreCase = true) },
            isRefreshing = refreshing
        )
    }.combine(_isLoading) { state, loading ->
        state.copy(isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimetableUiState())

    init {
        viewModelScope.launch {
            authRepository.studentProfile.collect { student ->
                if (student != null) {
                    if (timetableRepository.timetableSlots.value.isEmpty()) {
                        _isLoading.value = true
                        timetableRepository.refreshFromRemote(student.regNo)
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun refreshData() {
        val regNo = authRepository.studentProfile.value?.regNo ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            timetableRepository.refreshFromRemote(regNo)
            _isRefreshing.value = false
        }
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
    }
}
