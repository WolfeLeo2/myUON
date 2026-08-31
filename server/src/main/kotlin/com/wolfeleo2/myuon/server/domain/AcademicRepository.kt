package com.wolfeleo2.myuon.server.domain

interface AcademicRepository {
    suspend fun getAvailableUnits(academicYear: String, semester: Int, yearOfStudy: Int? = null): List<CourseUnit>
    suspend fun getUnitByCode(unitCode: String): CourseUnit?
    suspend fun getRegisteredUnits(regNo: String, academicYear: String, semester: Int): List<CourseUnit>
    suspend fun registerUnits(regNo: String, unitCodes: List<String>, academicYear: String, semester: Int): List<UnitRegistration>
    suspend fun getGradeRecords(regNo: String): List<GradeRecord>
    suspend fun getTimetable(regNo: String): List<TimetableItem>
    suspend fun getExamTimetable(regNo: String): List<ExamTimetableItem>
    suspend fun getAttendanceSessions(unitCode: String): List<ClassSessionAttendance>
    suspend fun getStudentAttendance(regNo: String, unitCode: String): AttendanceSummary?
    suspend fun getAttendanceOverview(regNo: String): List<AttendanceSummary>
    suspend fun submitSpecialExamRequest(request: SpecialExamRequest): SpecialExamRequest
    suspend fun submitSupplementaryRequest(request: SupplementaryRequest): SupplementaryRequest
    suspend fun submitMissingMarksDispute(request: MissingMarksDispute): MissingMarksDispute
    suspend fun getRequests(regNo: String): AcademicRequestsResponse
}
