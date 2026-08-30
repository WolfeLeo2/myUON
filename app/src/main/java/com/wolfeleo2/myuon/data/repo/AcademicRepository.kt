package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.db.CourseUnitDao
import com.wolfeleo2.myuon.data.db.GradeDao
import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.*
import com.wolfeleo2.myuon.data.remote.MyUonApiClient
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
class AcademicRepository @Inject constructor(
    private val apiClient: MyUonApiClient,
    private val courseUnitDao: CourseUnitDao,
    private val gradeDao: GradeDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _courseUnits = MutableStateFlow<List<CourseUnit>>(emptyList())
    val courseUnits: StateFlow<List<CourseUnit>> = _courseUnits.asStateFlow()

    private val _gradeRecords = MutableStateFlow<List<GradeRecord>>(emptyList())
    val gradeRecords: StateFlow<List<GradeRecord>> = _gradeRecords.asStateFlow()

    private val _academicSummary = MutableStateFlow(AcademicSummary(emptyList()))
    val academicSummary: StateFlow<AcademicSummary> = _academicSummary.asStateFlow()

    private val _examCard = MutableStateFlow<ExamCard>(
        ExamCard(
            cardId = "EC-9938472",
            regNo = "P15/12345/2022",
            studentName = "Leo K.",
            faculty = "Faculty of Science & Technology",
            program = "Bachelor of Science in Computer Science",
            academicYear = "2025/2026",
            semester = 2,
            isFeeCleared = true,
            isUnitsApproved = true,
            qrVerificationToken = "UON-VERIFY-2026-P1512345-VALID-99482",
            units = listOf(
                ExamCardItem("CSC 311", "Advanced Database Systems", "15 Jun 2026", "09:00 - 12:00", "Chiromo Lab 02", "DK-042"),
                ExamCardItem("CSC 315", "Operating Systems Principles", "18 Jun 2026", "14:00 - 17:00", "MLT 01 (Main Lecture Theatre)", "DK-105"),
                ExamCardItem("CSC 321", "Distributed Systems & Cloud Computing", "22 Jun 2026", "09:00 - 12:00", "Chiromo Lab 01", "DK-018"),
                ExamCardItem("CSC 323", "Artificial Intelligence & Machine Learning", "25 Jun 2026", "09:00 - 12:00", "Chiromo Lab 03", "DK-088"),
                ExamCardItem("CSC 327", "Compiler Construction", "29 Jun 2026", "14:00 - 17:00", "Chiromo Rm 204", "DK-063"),
                ExamCardItem("CSC 331", "Computer Graphics & Multimedia", "02 Jul 2026", "09:00 - 12:00", "Graphics Lab", "DK-031")
            ),
            generatedDate = "10 Jun 2026",
            authorizedBy = "Academic Registrar (Examinations)"
        )
    )
    val examCard: StateFlow<ExamCard> = _examCard.asStateFlow()

    private val _examTimetable = MutableStateFlow<List<ExamTimetableItem>>(
        listOf(
            ExamTimetableItem("EX-01", "CSC 311", "Advanced Database Systems", "15 Jun 2026", "09:00", "12:00", "Chiromo Lab 02", "Chiromo"),
            ExamTimetableItem("EX-02", "CSC 315", "Operating Systems Principles", "18 Jun 2026", "14:00", "17:00", "MLT 01", "Main Campus"),
            ExamTimetableItem("EX-03", "CSC 321", "Distributed Systems & Cloud Computing", "22 Jun 2026", "09:00", "12:00", "Chiromo Lab 01", "Chiromo"),
            ExamTimetableItem("EX-04", "CSC 323", "Artificial Intelligence & Machine Learning", "25 Jun 2026", "09:00", "12:00", "Chiromo Lab 03", "Chiromo"),
            ExamTimetableItem("EX-05", "CSC 327", "Compiler Construction", "29 Jun 2026", "14:00", "17:00", "Chiromo Rm 204", "Chiromo"),
            ExamTimetableItem("EX-06", "CSC 331", "Computer Graphics & Multimedia", "02 Jul 2026", "09:00", "12:00", "Graphics Lab", "Chiromo")
        )
    )
    val examTimetable: StateFlow<List<ExamTimetableItem>> = _examTimetable.asStateFlow()

    private val _attendanceSummary = MutableStateFlow<AttendanceSummary>(
        AttendanceSummary(
            unitCode = "CSC 311",
            unitTitle = "Advanced Database Systems",
            totalLecturesHeld = 24,
            lecturesAttended = 22,
            totalLabSessionsHeld = 12,
            labSessionsAttended = 11,
            weeklyBreakdown = emptyList(),
            recentSessions = emptyList()
        )
    )
    val attendanceSummary: StateFlow<AttendanceSummary> = _attendanceSummary.asStateFlow()

    private val _specialExamRequests = MutableStateFlow<List<SpecialExamRequest>>(emptyList())
    val specialExamRequests: StateFlow<List<SpecialExamRequest>> = _specialExamRequests.asStateFlow()

    private val _supplementaryRequests = MutableStateFlow<List<SupplementaryRequest>>(emptyList())
    val supplementaryRequests: StateFlow<List<SupplementaryRequest>> = _supplementaryRequests.asStateFlow()

    private val _missingMarksDisputes = MutableStateFlow<List<MissingMarksDispute>>(emptyList())
    val missingMarksDisputes: StateFlow<List<MissingMarksDispute>> = _missingMarksDisputes.asStateFlow()

    init {
        scope.launch {
            // 1. Load cached Room data first for instantaneous display
            val cachedUnits = courseUnitDao.getAllUnits().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            if (cachedUnits.isNotEmpty()) {
                _courseUnits.value = cachedUnits
            }

            val cachedGrades = gradeDao.getAllGrades().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            if (cachedGrades.isNotEmpty()) {
                _gradeRecords.value = cachedGrades
                _academicSummary.value = AcademicSummary(cachedGrades)
            }

            // 2. Fetch fresh data from remote server and update Room cache
            refreshFromRemote("P15/12345/2022")
        }
    }

    suspend fun refreshFromRemote(regNo: String) {
        val remoteUnits = apiClient.getAvailableUnits()
        if (!remoteUnits.isNullOrEmpty()) {
            _courseUnits.value = remoteUnits
            courseUnitDao.insertUnits(remoteUnits.map { it.toEntity() })
        }

        val remoteGrades = apiClient.getGrades(regNo)
        if (!remoteGrades.isNullOrEmpty()) {
            _gradeRecords.value = remoteGrades
            _academicSummary.value = AcademicSummary(remoteGrades)
            gradeDao.insertGrades(remoteGrades.map { it.toEntity() })
        }

        val remoteExamCard = apiClient.getExamCard(regNo)
        if (remoteExamCard != null) {
            _examCard.value = remoteExamCard
        }
    }

    fun toggleUnitSelection(unitCode: String) {
        _courseUnits.value = _courseUnits.value.map { unit ->
            if (unit.unitCode == unitCode) {
                val updated = when (unit.status) {
                    UnitStatus.AVAILABLE -> unit.copy(status = UnitStatus.DRAFT)
                    UnitStatus.DRAFT -> unit.copy(status = UnitStatus.AVAILABLE)
                    UnitStatus.APPROVED -> unit.copy(status = UnitStatus.DROPPED)
                    UnitStatus.DROPPED -> unit.copy(status = UnitStatus.APPROVED)
                    UnitStatus.SUBMITTED -> unit
                }
                scope.launch { courseUnitDao.updateUnit(updated.toEntity()) }
                updated
            } else unit
        }
    }

    suspend fun submitUnitRegistration(regNo: String = "P15/12345/2022"): Boolean {
        val selectedCodes = _courseUnits.value.filter { it.status == UnitStatus.DRAFT }.map { it.unitCode }
        if (selectedCodes.isNotEmpty()) {
            apiClient.registerUnits(regNo, selectedCodes)
        }
        val updatedList = _courseUnits.value.map { unit ->
            if (unit.status == UnitStatus.DRAFT) {
                val updated = unit.copy(status = UnitStatus.APPROVED)
                scope.launch { courseUnitDao.updateUnit(updated.toEntity()) }
                updated
            } else unit
        }
        _courseUnits.value = updatedList
        return true
    }

    private val inFlightOrResolved = setOf(RequestStatus.PENDING, RequestStatus.UNDER_REVIEW, RequestStatus.APPROVED)

    fun submitSpecialExamRequest(
        regNo: String,
        unitCode: String,
        unitTitle: String,
        reasonCategory: String,
        explanation: String
    ): Boolean {
        val academicYear = "2025/2026"
        val semester = 2
        val duplicate = _specialExamRequests.value.any {
            it.regNo == regNo && it.unitCode == unitCode &&
                it.academicYear == academicYear && it.semester == semester &&
                it.status in inFlightOrResolved
        }
        if (duplicate) return false

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val newRequest = SpecialExamRequest(
            requestId = "SP-${UUID.randomUUID().toString().take(6).uppercase()}",
            regNo = regNo,
            unitCode = unitCode,
            unitTitle = unitTitle,
            academicYear = academicYear,
            semester = semester,
            reasonCategory = reasonCategory,
            explanation = explanation,
            medicalOfficerApproval = reasonCategory.contains("Medical", ignoreCase = true),
            deanApproval = false,
            status = RequestStatus.PENDING,
            submissionDate = dateFormat.format(Date())
        )
        _specialExamRequests.value = listOf(newRequest) + _specialExamRequests.value
        return true
    }

    fun submitSupplementaryRequest(
        regNo: String,
        unitCode: String,
        unitTitle: String,
        previousScore: Double,
        paymentRef: String
    ): Boolean {
        val duplicate = _supplementaryRequests.value.any {
            it.regNo == regNo && it.unitCode == unitCode && it.status in inFlightOrResolved
        }
        if (duplicate) return false

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val newRequest = SupplementaryRequest(
            requestId = "SUP-${UUID.randomUUID().toString().take(6).uppercase()}",
            regNo = regNo,
            unitCode = unitCode,
            unitTitle = unitTitle,
            previousScore = previousScore,
            feeAmount = 1000.0,
            paymentReference = paymentRef,
            isPaid = paymentRef.isNotBlank(),
            status = RequestStatus.PENDING,
            submissionDate = dateFormat.format(Date())
        )
        _supplementaryRequests.value = listOf(newRequest) + _supplementaryRequests.value
        return true
    }

    fun submitMissingMarksDispute(
        regNo: String,
        unitCode: String,
        unitTitle: String,
        lecturerName: String,
        missingComponent: String,
        evidenceNote: String
    ): Boolean {
        val academicYear = "2025/2026"
        val semester = 2
        val duplicate = _missingMarksDisputes.value.any {
            it.regNo == regNo && it.unitCode == unitCode &&
                it.academicYear == academicYear && it.semester == semester &&
                it.status in inFlightOrResolved
        }
        if (duplicate) return false

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val newDispute = MissingMarksDispute(
            disputeId = "MMD-${UUID.randomUUID().toString().take(6).uppercase()}",
            regNo = regNo,
            unitCode = unitCode,
            unitTitle = unitTitle,
            lecturerName = lecturerName,
            academicYear = academicYear,
            semester = semester,
            missingComponent = missingComponent,
            evidenceNote = evidenceNote,
            status = RequestStatus.PENDING,
            submittedDate = dateFormat.format(Date())
        )
        _missingMarksDisputes.value = listOf(newDispute) + _missingMarksDisputes.value
        return true
    }
}
