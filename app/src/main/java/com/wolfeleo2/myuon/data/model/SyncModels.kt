package com.wolfeleo2.myuon.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class SyncScope {
    STUDENT_PROFILE,
    UNITS,
    GRADES,
    TIMETABLE,
    FEES,
    HOSTELS,
    ATTENDANCE,
    ALL
}

@Serializable
data class SyncEvent(
    val eventType: String = "DELTA_AVAILABLE",
    val scope: SyncScope,
    val regNo: String? = null,
    val timestamp: String,
    val description: String? = null
)

@Serializable
data class SyncDeltaPayload(
    val timestamp: String,
    val studentProfile: StudentProfile? = null,
    val units: List<CourseUnit> = emptyList(),
    val grades: List<GradeRecord> = emptyList(),
    val timetable: List<TimetableItem> = emptyList(),
    val feeStatement: FeeStatement? = null,
    val hostels: List<HostelHall> = emptyList(),
    val attendanceOverview: List<AttendanceSummary> = emptyList(),
    val hostelBooking: HostelRoomBooking? = null,
    val examCard: ExamCard? = null,
    val examTimetable: List<ExamTimetableItem> = emptyList(),
    val academicRequests: AcademicRequestsResponse? = null
)
