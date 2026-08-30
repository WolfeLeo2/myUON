package com.wolfeleo2.myuon.server.domain

import kotlinx.serialization.Serializable

@Serializable
enum class UnitStatus {
    AVAILABLE,
    DRAFT,
    SUBMITTED,
    APPROVED,
    DROPPED
}

@Serializable
data class SyllabusTopic(
    val weekNumber: Int,
    val title: String,
    val subtopics: List<String> = emptyList()
)

@Serializable
data class CourseUnit(
    val unitCode: String,
    val unitTitle: String,
    val credits: Int = 3,
    val academicYear: String = "2025/2026",
    val semester: Int = 2,
    val lecturerName: String,
    val lecturerEmail: String,
    val lecturerOffice: String = "Department of Computer Science, Chiromo",
    val venueName: String,
    val campus: String,
    val isCore: Boolean = true,
    val prerequisites: List<String> = emptyList(),
    val status: UnitStatus = UnitStatus.AVAILABLE,
    val scheduleTime: String = "Mon 09:00 - 11:00",
    val description: String = "",
    val syllabusTopics: List<SyllabusTopic> = emptyList(),
    val learningOutcomes: List<String> = emptyList(),
    val recommendedTextbooks: List<String> = emptyList()
)

@Serializable
data class UnitRegistration(
    val registrationId: String,
    val regNo: String,
    val unitCode: String,
    val academicYear: String,
    val semester: Int,
    val status: UnitStatus = UnitStatus.APPROVED,
    val registeredAt: String
)

@Serializable
enum class DegreeClass(val label: String) {
    FIRST_CLASS("First Class Honours"),
    SECOND_UPPER("Second Class Honours (Upper Division)"),
    SECOND_LOWER("Second Class Honours (Lower Division)"),
    PASS("Pass"),
    FAIL("Fail")
}

@Serializable
data class GradeRecord(
    val id: String = "",
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val academicYear: String,
    val semester: Int,
    val credits: Int = 3,
    val catScore: Double,      // Out of 30
    val examScore: Double,     // Out of 70
    val totalScore: Double,    // Total percentage out of 100
    val gradeLetter: String,   // A, B, C, D, E/F
    val isPass: Boolean,
    val isSupplementary: Boolean = false,
    val isSpecial: Boolean = false,
    val remarks: String = "Satisfactory"
)

fun cumulativeAverage(records: List<GradeRecord>): Double {
    val totalCredits = records.sumOf { it.credits }
    if (totalCredits == 0) return 0.0
    return records.sumOf { it.totalScore * it.credits } / totalCredits
}

fun classifyDegree(average: Double): DegreeClass = when {
    average >= 70.0 -> DegreeClass.FIRST_CLASS
    average >= 60.0 -> DegreeClass.SECOND_UPPER
    average >= 50.0 -> DegreeClass.SECOND_LOWER
    average >= 40.0 -> DegreeClass.PASS
    else -> DegreeClass.FAIL
}

@Serializable
data class AcademicSummary(
    val regNo: String,
    val cumulativeAverage: Double = 0.0,
    val degreeClass: DegreeClass = DegreeClass.FAIL,
    val totalUnitsPassed: Int = 0,
    val totalCreditsEarned: Int = 0
)

@Serializable
enum class ClassType {
    LECTURE,
    LABORATORY,
    TUTORIAL,
    SEMINAR
}

@Serializable
data class TimetableItem(
    val id: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val unitCode: String,
    val unitTitle: String,
    val lecturer: String,
    val lecturerEmail: String,
    val venue: String,
    val campus: String,
    val classType: ClassType = ClassType.LECTURE,
    val isOnline: Boolean = false,
    val notes: String? = null
)

@Serializable
data class ExamTimetableItem(
    val id: String,
    val unitCode: String,
    val unitTitle: String,
    val examDate: String,
    val startTime: String,
    val endTime: String,
    val venue: String,
    val campus: String,
    val chiefInvigilator: String = "Chief Invigilator",
    val instructions: String = "Arrive 15 minutes before the start time. Carry your student ID and exam card."
)

@Serializable
data class ExamCardItem(
    val unitCode: String,
    val unitTitle: String,
    val examDate: String,
    val examTime: String,
    val venue: String,
    val deskNumber: String? = null
)

@Serializable
data class ExamCard(
    val cardId: String,
    val regNo: String,
    val studentName: String,
    val faculty: String,
    val program: String,
    val academicYear: String,
    val semester: Int,
    val passportPhotoUrl: String? = null,
    val isFeeCleared: Boolean,
    val isUnitsApproved: Boolean,
    val qrVerificationToken: String,
    val units: List<ExamCardItem>,
    val generatedDate: String,
    val authorizedBy: String = "Academic Registrar (Examinations)"
)

@Serializable
data class ClassSessionAttendance(
    val id: String,
    val unitCode: String,
    val date: String,
    val timeSlot: String,
    val sessionType: String,
    val topicCovered: String,
    val hours: Double,
    val isAttended: Boolean = true,
    val venue: String
)

@Serializable
data class AttendanceWeekRecord(
    val weekLabel: String,
    val weekStartDate: String,
    val sessions: List<ClassSessionAttendance>
) {
    val percentageAttended: Double
        get() {
            if (sessions.isEmpty()) return 100.0
            val attended = sessions.count { it.isAttended }
            return (attended.toDouble() / sessions.size) * 100.0
        }
}

@Serializable
data class AttendanceSummary(
    val unitCode: String,
    val unitTitle: String,
    val totalLecturesHeld: Int,
    val lecturesAttended: Int,
    val totalLabSessionsHeld: Int,
    val labSessionsAttended: Int,
    val weeklyBreakdown: List<AttendanceWeekRecord> = emptyList(),
    val recentSessions: List<ClassSessionAttendance> = emptyList()
) {
    val overallPercentage: Double
        get() {
            val total = totalLecturesHeld + totalLabSessionsHeld
            val attended = lecturesAttended + labSessionsAttended
            return if (total > 0) (attended.toDouble() / total) * 100.0 else 100.0
        }

    val isSenateThresholdMet: Boolean
        get() = overallPercentage >= 75.0
}

@Serializable
enum class RequestStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}

@Serializable
data class SpecialExamRequest(
    val requestId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val academicYear: String,
    val semester: Int,
    val reasonCategory: String,
    val explanation: String,
    val medicalOfficerApproval: Boolean = false,
    val deanApproval: Boolean = false,
    val status: RequestStatus = RequestStatus.PENDING,
    val submissionDate: String
)

@Serializable
data class SupplementaryRequest(
    val requestId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val previousScore: Double,
    val feeAmount: Double = 1000.0,
    val paymentReference: String? = null,
    val isPaid: Boolean = false,
    val status: RequestStatus = RequestStatus.PENDING,
    val submissionDate: String
)

@Serializable
data class MissingMarksDispute(
    val disputeId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val lecturerName: String,
    val academicYear: String,
    val semester: Int,
    val missingComponent: String,
    val evidenceNote: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val submittedDate: String
)
