package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.*
import java.time.Instant

class ExposedSyncRepository(
    private val studentRepository: StudentRepository,
    private val academicRepository: AcademicRepository,
    private val feeRepository: FeeRepository,
    private val hostelRepository: HostelRepository
) : SyncRepository {

    override suspend fun getDeltaPayload(since: String?, regNo: String?): SyncDeltaPayload {
        val now = Instant.now().toString()
        val units = academicRepository.getAvailableUnits(academicYear = "2025/2026", semester = 2)
        val grades = if (!regNo.isNullOrBlank()) academicRepository.getGradeRecords(regNo) else emptyList()
        val timetable = if (!regNo.isNullOrBlank()) academicRepository.getTimetable(regNo) else emptyList()
        val fees = if (!regNo.isNullOrBlank()) feeRepository.getFeeStatement(regNo, academicYear = "2025/2026", semester = 2) else null
        val hostels = hostelRepository.getHostels()
        val student = if (!regNo.isNullOrBlank()) studentRepository.findByRegNo(regNo) else null
        val attendanceOverview = if (!regNo.isNullOrBlank()) academicRepository.getAttendanceOverview(regNo) else emptyList()

        val hostelBooking = if (!regNo.isNullOrBlank()) hostelRepository.getHostelBooking(regNo) else null
        val examTimetable = if (!regNo.isNullOrBlank()) academicRepository.getExamTimetable(regNo) else emptyList()
        val requests = if (!regNo.isNullOrBlank()) academicRepository.getRequests(regNo) else AcademicRequestsResponse()

        val examCard = if (!regNo.isNullOrBlank() && student != null) {
            val examItems = examTimetable.map {
                ExamCardItem(
                    unitCode = it.unitCode,
                    unitTitle = it.unitTitle,
                    examDate = it.examDate,
                    examTime = "${it.startTime} - ${it.endTime}",
                    venue = it.venue,
                    deskNumber = "DK-34"
                )
            }
            ExamCard(
                cardId = "EC-DELTA-01",
                regNo = regNo,
                studentName = student.fullName,
                faculty = student.faculty,
                program = student.program,
                academicYear = "2025/2026",
                semester = 2,
                passportPhotoUrl = student.photoUrl,
                isFeeCleared = student.isFeeCleared,
                isUnitsApproved = true,
                qrVerificationToken = "UON-VERIFY-$regNo",
                units = examItems,
                generatedDate = "2026-08-30"
            )
        } else null

        return SyncDeltaPayload(
            timestamp = now,
            studentProfile = student,
            units = units,
            grades = grades,
            timetable = timetable,
            feeStatement = fees,
            hostels = hostels,
            attendanceOverview = attendanceOverview,
            hostelBooking = hostelBooking,
            examCard = examCard,
            examTimetable = examTimetable,
            academicRequests = requests
        )
    }
}
