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
    val isRefreshing: Boolean = false
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow("Monday")
    private val _isRefreshing = MutableStateFlow(false)
    
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimetableUiState())

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _isRefreshing.value = false
        }
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
    }
}
