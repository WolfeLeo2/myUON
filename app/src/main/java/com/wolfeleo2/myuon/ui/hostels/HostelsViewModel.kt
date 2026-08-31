package com.wolfeleo2.myuon.ui.hostels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfeleo2.myuon.data.model.GenderTarget
import com.wolfeleo2.myuon.data.model.HostelHall
import com.wolfeleo2.myuon.data.model.HostelRoomBooking
import com.wolfeleo2.myuon.data.model.StudentProfile
import com.wolfeleo2.myuon.data.repo.AuthRepository
import com.wolfeleo2.myuon.data.repo.HostelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HostelsUiState(
    val student: StudentProfile? = null,
    val halls: List<HostelHall> = emptyList(),
    val filteredHalls: List<HostelHall> = emptyList(),
    val activeBooking: HostelRoomBooking? = null,
    val selectedGenderFilter: GenderTarget? = null,
    val isBooking: Boolean = false,
    val message: String? = null,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class HostelsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val hostelRepository: HostelRepository
) : ViewModel() {

    private val _genderFilter = MutableStateFlow<GenderTarget?>(null)
    private val _isBooking = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<HostelsUiState> = combine(
        authRepository.studentProfile,
        hostelRepository.hostels,
        hostelRepository.activeBooking,
        _genderFilter
    ) { student, halls, booking, filter ->
        val filtered = if (filter == null) halls else halls.filter { it.genderTarget == filter || it.genderTarget == GenderTarget.CO_ED }
        HostelsUiState(
            student = student,
            halls = halls,
            filteredHalls = filtered,
            activeBooking = booking,
            selectedGenderFilter = filter
        )
    }.combine(_isBooking) { state, bookingState ->
        state.copy(isBooking = bookingState)
    }.combine(_message) { state, msg ->
        state.copy(message = msg)
    }.combine(_isRefreshing) { state, refreshing ->
        state.copy(isRefreshing = refreshing)
    }.combine(_isLoading) { state, loading ->
        state.copy(isLoading = loading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HostelsUiState())

    init {
        viewModelScope.launch {
            authRepository.studentProfile.collect { student ->
                if (student != null) {
                    if (hostelRepository.hostels.value.isEmpty()) {
                        _isLoading.value = true
                        hostelRepository.refreshFromRemote(student.regNo)
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
            hostelRepository.refreshFromRemote(regNo)
            _isRefreshing.value = false
        }
    }

    fun setGenderFilter(target: GenderTarget?) {
        _genderFilter.value = target
    }

    fun applyForRoom(hall: HostelHall, roomNumber: String, bedSpace: String) {
        viewModelScope.launch {
            _isBooking.value = true
            val regNo = uiState.value.student?.regNo ?: return@launch
            val success = hostelRepository.bookRoom(regNo, hall, roomNumber, bedSpace)
            _isBooking.value = false
            if (success) {
                _message.value = "Room ${roomNumber} ($bedSpace) reserved successfully in ${hall.hallName}!"
            } else {
                _message.value = "Selected hall is currently full. Please select an alternate hall."
            }
        }
    }

    fun payRent() {
        viewModelScope.launch {
            val success = hostelRepository.payHostelRent()
            _message.value = if (success) {
                "Accommodation fee paid and key clearance active!"
            } else {
                "No active hostel booking to pay for."
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
