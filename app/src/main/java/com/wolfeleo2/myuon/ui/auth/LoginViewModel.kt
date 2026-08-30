package com.wolfeleo2.myuon.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfeleo2.myuon.data.model.UonCampuses
import com.wolfeleo2.myuon.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val regNo: String = "P15/12345/2022",
    val adEmail: String = "leo@students.uonbi.ac.ke",
    val password: String = "uon@2026",
    val isAdLoginMode: Boolean = false,
    val isSignUpMode: Boolean = false,
    // Sign Up specific fields
    val fullName: String = "Leo K.",
    val campus: String = UonCampuses.ALL[0],
    val faculty: String = "Faculty of Science & Technology",
    val department: String = "Department of Computer Science",
    val program: String = "Bachelor of Science in Computer Science",
    val yearOfStudy: Int = 1,
    val semester: Int = 1,
    val nationalId: String = "38920194",
    val mobileNumber: String = "0712345678",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onRegNoChanged(regNo: String) {
        _uiState.value = _uiState.value.copy(regNo = regNo, errorMessage = null)
    }

    fun onAdEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(adEmail = email, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onFullNameChanged(fullName: String) {
        _uiState.value = _uiState.value.copy(fullName = fullName, errorMessage = null)
    }

    fun onCampusSelected(campus: String) {
        _uiState.value = _uiState.value.copy(campus = campus, errorMessage = null)
    }

    fun onProgramChanged(program: String) {
        _uiState.value = _uiState.value.copy(program = program, errorMessage = null)
    }

    fun onMobileNumberChanged(phone: String) {
        _uiState.value = _uiState.value.copy(mobileNumber = phone, errorMessage = null)
    }

    fun toggleLoginMode() {
        _uiState.value = _uiState.value.copy(
            isAdLoginMode = !_uiState.value.isAdLoginMode,
            errorMessage = null
        )
    }

    fun toggleSignUpMode() {
        _uiState.value = _uiState.value.copy(
            isSignUpMode = !_uiState.value.isSignUpMode,
            errorMessage = null
        )
    }

    fun submit(onSuccess: () -> Unit) {
        if (_uiState.value.isSignUpMode) {
            signUp(onSuccess)
        } else {
            login(onSuccess)
        }
    }

    private fun login(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val success = if (_uiState.value.isAdLoginMode) {
                authRepository.loginWithActiveDirectory(_uiState.value.adEmail, _uiState.value.password)
            } else {
                authRepository.loginWithRegNo(_uiState.value.regNo, _uiState.value.password)
            }

            if (success) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid credentials. Please verify your SMIS / AD account details."
                )
            }
        }
    }

    private fun signUp(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.fullName.isBlank() || state.regNo.isBlank() || state.adEmail.isBlank() || state.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all required fields.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val success = authRepository.signUp(
                email = state.adEmail,
                pass = state.password,
                regNo = state.regNo,
                fullName = state.fullName,
                faculty = state.faculty,
                department = state.department,
                program = state.program,
                yearOfStudy = state.yearOfStudy,
                semester = state.semester,
                campus = state.campus,
                nationalId = state.nationalId,
                mobileNumber = state.mobileNumber
            )

            if (success) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign up failed. Please ensure registration number and email are unique."
                )
            }
        }
    }
}
