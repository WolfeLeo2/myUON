package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.db.StudentDao
import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.StudentProfile
import com.wolfeleo2.myuon.data.preferences.UserPreferencesDataStore
import com.wolfeleo2.myuon.data.remote.MyUonApiClient
import com.wolfeleo2.myuon.data.remote.SignUpApiRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiClient: MyUonApiClient,
    private val studentDao: StudentDao,
    private val preferencesDataStore: UserPreferencesDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _studentProfile = MutableStateFlow<StudentProfile?>(null)
    val studentProfile: StateFlow<StudentProfile?> = _studentProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isSessionLoaded = MutableStateFlow(false)
    val isSessionLoaded: StateFlow<Boolean> = _isSessionLoaded.asStateFlow()

    private var _sessionToken: String? = null
    val sessionToken: String? get() = _sessionToken

    init {
        scope.launch {
            val savedRegNo = preferencesDataStore.activeStudentRegNo.firstOrNull()
            if (!savedRegNo.isNullOrBlank()) {
                val cached = studentDao.getStudent(savedRegNo).firstOrNull()
                val profile = cached?.toDomain() ?: StudentProfile(
                    regNo = savedRegNo,
                    fullName = "Leo K.",
                    studentEmail = "leo@students.uonbi.ac.ke",
                    faculty = "Faculty of Science & Technology",
                    department = "Department of Computer Science",
                    program = "Bachelor of Science in Computer Science",
                    yearOfStudy = 3,
                    semester = 2,
                    campus = "Chiromo Campus",
                    nationalId = "",
                    mobileNumber = ""
                )
                if (cached == null) {
                    studentDao.insertOrUpdateStudent(profile.toEntity())
                }
                _studentProfile.value = profile
                _isLoggedIn.value = true
            }
            _isSessionLoaded.value = true
        }
    }

    suspend fun loginWithRegNo(regNo: String, pass: String): Boolean {
        val trimmed = regNo.trim().uppercase()
        val remote = apiClient.login("SMIS", trimmed, pass)
        if (remote != null) {
            _sessionToken = remote.sessionToken
            val cached = studentDao.getStudent(trimmed).firstOrNull()?.toDomain()
            val profile = cached?.copy(regNo = trimmed) ?: StudentProfile(
                regNo = trimmed,
                fullName = "Leo K.",
                studentEmail = "leo@students.uonbi.ac.ke",
                faculty = "Faculty of Science & Technology",
                department = "Department of Computer Science",
                program = "Bachelor of Science in Computer Science",
                yearOfStudy = 3,
                semester = 2,
                campus = "Chiromo Campus",
                nationalId = "",
                mobileNumber = ""
            )
            studentDao.insertOrUpdateStudent(profile.toEntity())
            preferencesDataStore.setActiveStudentRegNo(trimmed)
            _studentProfile.value = profile
            _isLoggedIn.value = true
            return true
        }

        // Offline cached login check
        val cached = studentDao.getStudent(trimmed).firstOrNull()
        if (cached != null) {
            _studentProfile.value = cached.toDomain()
            preferencesDataStore.setActiveStudentRegNo(trimmed)
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    suspend fun loginWithActiveDirectory(email: String, pass: String): Boolean {
        val trimmed = email.trim().lowercase()
        val remote = apiClient.login("AD", trimmed, pass)
        if (remote != null) {
            _sessionToken = remote.sessionToken
            val savedRegNo = preferencesDataStore.activeStudentRegNo.firstOrNull()
            val cached = savedRegNo?.let { studentDao.getStudent(it).firstOrNull()?.toDomain() }
            val profile = cached?.copy(studentEmail = trimmed) ?: StudentProfile(
                regNo = "P15/12345/2022",
                fullName = trimmed.substringBefore("@").replace(".", " ").capitalizeWords(),
                studentEmail = trimmed,
                faculty = "Faculty of Science & Technology",
                department = "Department of Computer Science",
                program = "Bachelor of Science in Computer Science",
                yearOfStudy = 3,
                semester = 2,
                campus = "Chiromo Campus",
                nationalId = "",
                mobileNumber = ""
            )
            studentDao.insertOrUpdateStudent(profile.toEntity())
            preferencesDataStore.setActiveStudentRegNo(profile.regNo)
            _studentProfile.value = profile
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    suspend fun signUp(
        email: String,
        pass: String,
        regNo: String,
        fullName: String,
        faculty: String,
        department: String,
        program: String,
        yearOfStudy: Int,
        semester: Int,
        campus: String,
        nationalId: String,
        mobileNumber: String
    ): Boolean {
        val request = SignUpApiRequest(
            email = email.trim().lowercase(),
            password = pass,
            regNo = regNo.trim().uppercase(),
            fullName = fullName.trim(),
            faculty = faculty,
            department = department,
            program = program,
            yearOfStudy = yearOfStudy,
            semester = semester,
            campus = campus,
            nationalId = nationalId.trim(),
            mobileNumber = mobileNumber.trim()
        )
        val remote = apiClient.signUp(request)
        val profile = StudentProfile(
            regNo = request.regNo,
            fullName = request.fullName,
            studentEmail = request.email,
            faculty = request.faculty,
            department = request.department,
            program = request.program,
            yearOfStudy = request.yearOfStudy,
            semester = request.semester,
            campus = request.campus,
            nationalId = request.nationalId,
            mobileNumber = request.mobileNumber,
            isFeeCleared = false
        )

        if (remote != null) {
            _sessionToken = remote.sessionToken
        }
        studentDao.insertOrUpdateStudent(profile.toEntity())
        preferencesDataStore.setActiveStudentRegNo(profile.regNo)
        _studentProfile.value = profile
        _isLoggedIn.value = true
        return true
    }

    fun logout() {
        _isLoggedIn.value = false
        _sessionToken = null
        scope.launch {
            preferencesDataStore.setActiveStudentRegNo(null)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        val updated = _studentProfile.value?.copy(isBiometricEnabled = enabled)
        if (updated != null) {
            _studentProfile.value = updated
            scope.launch {
                studentDao.insertOrUpdateStudent(updated.toEntity())
            }
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
