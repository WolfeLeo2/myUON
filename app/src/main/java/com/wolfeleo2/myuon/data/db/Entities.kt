package com.wolfeleo2.myuon.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wolfeleo2.myuon.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val regNo: String,
    val fullName: String,
    val studentEmail: String,
    val faculty: String,
    val department: String,
    val program: String,
    val yearOfStudy: Int,
    val semester: Int,
    val campus: String,
    val nationalId: String,
    val mobileNumber: String,
    val photoUrl: String?,
    val isFeeCleared: Boolean,
    val isBiometricEnabled: Boolean,
    val currentAverage: Double
)

@Entity(tableName = "course_units")
data class CourseUnitEntity(
    @PrimaryKey val unitCode: String,
    val unitTitle: String,
    val credits: Int,
    val academicYear: String,
    val semester: Int,
    val lecturerName: String,
    val lecturerEmail: String,
    val lecturerOffice: String = "Department of Computer Science, Chiromo",
    val venueName: String,
    val campus: String,
    val isCore: Boolean,
    val prerequisitesJson: String = "[]",
    val status: UnitStatus,
    val scheduleTime: String,
    val description: String,
    val syllabusTopicsJson: String = "[]",
    val learningOutcomesJson: String = "[]",
    val recommendedTextbooksJson: String = "[]"
)

@Entity(tableName = "grade_records", primaryKeys = ["unitCode", "academicYear", "semester"])
data class GradeRecordEntity(
    val unitCode: String,
    val unitTitle: String,
    val academicYear: String,
    val semester: Int,
    val credits: Int,
    val catScore: Double,
    val examScore: Double,
    val totalScore: Double,
    val gradeLetter: String,
    val isPass: Boolean,
    val isSupplementary: Boolean,
    val isSpecial: Boolean,
    val remarks: String
)

@Entity(tableName = "timetable_slots")
data class TimetableEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val unitCode: String,
    val unitTitle: String,
    val lecturer: String,
    val lecturerEmail: String,
    val venue: String,
    val campus: String,
    val classType: ClassType,
    val isOnline: Boolean,
    val notes: String?
)

@Entity(tableName = "attendance_summaries")
data class AttendanceSummaryEntity(
    @PrimaryKey val unitCode: String,
    val unitTitle: String,
    val totalLecturesHeld: Int,
    val lecturesAttended: Int,
    val totalLabSessionsHeld: Int,
    val labSessionsAttended: Int,
    val weeklyBreakdownJson: String = "[]",
    val recentSessionsJson: String = "[]"
)

@Entity(tableName = "hostel_halls")
data class HostelHallEntity(
    @PrimaryKey val hallId: String,
    val hallName: String,
    val campus: String,
    val genderTarget: GenderTarget,
    val totalRooms: Int,
    val availableRooms: Int,
    val rentPerSemester: Double,
    val amenitiesJson: String = "[]",
    val imageUrl: String? = null,
    val isBookingOpen: Boolean = true
)

@Entity(tableName = "hostel_bookings")
data class HostelBookingEntity(
    @PrimaryKey val bookingId: String,
    val regNo: String,
    val hallName: String,
    val roomNumber: String,
    val bedSpace: String,
    val academicYear: String,
    val semester: Int,
    val rentAmount: Double,
    val isPaid: Boolean,
    val isKeyIssued: Boolean,
    val bookedDate: String
)

@Entity(tableName = "fee_statements", primaryKeys = ["academicYear", "semester"])
data class FeeStatementEntity(
    val academicYear: String,
    val semester: Int,
    val totalInvoiced: Double,
    val totalPaid: Double,
    val outstandingBalance: Double,
    val helbDisbursed: Double,
    val hefScholarship: Double,
    val invoiceBreakdownJson: String = "{}",
    val transactionsJson: String = "[]"
)

@Entity(tableName = "exam_cards")
data class ExamCardEntity(
    @PrimaryKey val regNo: String,
    val cardId: String,
    val studentName: String,
    val faculty: String,
    val program: String,
    val academicYear: String,
    val semester: Int,
    val passportPhotoUrl: String?,
    val isFeeCleared: Boolean,
    val isUnitsApproved: Boolean,
    val qrVerificationToken: String,
    val unitsJson: String = "[]",
    val generatedDate: String,
    val authorizedBy: String = "Academic Registrar (Examinations)"
)

@Entity(tableName = "exam_timetable_slots")
data class ExamTimetableEntity(
    @PrimaryKey val id: String,
    val unitCode: String,
    val unitTitle: String,
    val examDate: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val session: String,
    val venue: String,
    val campus: String,
    val faculty: String,
    val chiefInvigilator: String,
    val isAuthorized: Boolean = true
)

@Entity(tableName = "special_exam_requests")
data class SpecialExamRequestEntity(
    @PrimaryKey val requestId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val academicYear: String,
    val semester: Int,
    val reasonCategory: String,
    val explanation: String,
    val medicalOfficerApproval: Boolean,
    val deanApproval: Boolean,
    val status: RequestStatus,
    val submissionDate: String
)

@Entity(tableName = "supplementary_requests")
data class SupplementaryRequestEntity(
    @PrimaryKey val requestId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val previousScore: Double,
    val feeAmount: Double,
    val paymentReference: String?,
    val isPaid: Boolean,
    val status: RequestStatus,
    val submissionDate: String
)

@Entity(tableName = "missing_marks_disputes")
data class MissingMarksDisputeEntity(
    @PrimaryKey val disputeId: String,
    val regNo: String,
    val unitCode: String,
    val unitTitle: String,
    val lecturerName: String,
    val academicYear: String,
    val semester: Int,
    val missingComponent: String,
    val evidenceNote: String,
    val status: RequestStatus,
    val submittedDate: String
)

private val json = Json { ignoreUnknownKeys = true }

fun StudentEntity.toDomain() = StudentProfile(
    regNo = regNo,
    fullName = fullName,
    studentEmail = studentEmail,
    faculty = faculty,
    department = department,
    program = program,
    yearOfStudy = yearOfStudy,
    semester = semester,
    campus = campus,
    nationalId = nationalId,
    mobileNumber = mobileNumber,
    photoUrl = photoUrl,
    isFeeCleared = isFeeCleared,
    isBiometricEnabled = isBiometricEnabled
)

fun StudentProfile.toEntity(currentAverage: Double = 0.0) = StudentEntity(
    regNo = regNo,
    fullName = fullName,
    studentEmail = studentEmail,
    faculty = faculty,
    department = department,
    program = program,
    yearOfStudy = yearOfStudy,
    semester = semester,
    campus = campus,
    nationalId = nationalId,
    mobileNumber = mobileNumber,
    photoUrl = photoUrl,
    isFeeCleared = isFeeCleared,
    isBiometricEnabled = isBiometricEnabled,
    currentAverage = currentAverage
)

fun CourseUnitEntity.toDomain(): CourseUnit {
    val topics = runCatching { json.decodeFromString<List<SyllabusTopic>>(syllabusTopicsJson) }.getOrDefault(emptyList())
    val outcomes = runCatching { json.decodeFromString<List<String>>(learningOutcomesJson) }.getOrDefault(emptyList())
    val books = runCatching { json.decodeFromString<List<String>>(recommendedTextbooksJson) }.getOrDefault(emptyList())
    val prereqs = runCatching { json.decodeFromString<List<String>>(prerequisitesJson) }.getOrDefault(emptyList())

    return CourseUnit(
        unitCode = unitCode,
        unitTitle = unitTitle,
        credits = credits,
        academicYear = academicYear,
        semester = semester,
        lecturerName = lecturerName,
        lecturerEmail = lecturerEmail,
        lecturerOffice = lecturerOffice,
        venueName = venueName,
        campus = campus,
        isCore = isCore,
        prerequisites = prereqs,
        status = status,
        scheduleTime = scheduleTime,
        description = description,
        syllabusTopics = topics,
        learningOutcomes = outcomes,
        recommendedTextbooks = books
    )
}

fun CourseUnit.toEntity() = CourseUnitEntity(
    unitCode = unitCode,
    unitTitle = unitTitle,
    credits = credits,
    academicYear = academicYear,
    semester = semester,
    lecturerName = lecturerName,
    lecturerEmail = lecturerEmail,
    lecturerOffice = lecturerOffice,
    venueName = venueName,
    campus = campus,
    isCore = isCore,
    prerequisitesJson = json.encodeToString(prerequisites),
    status = status,
    scheduleTime = scheduleTime,
    description = description,
    syllabusTopicsJson = json.encodeToString(syllabusTopics),
    learningOutcomesJson = json.encodeToString(learningOutcomes),
    recommendedTextbooksJson = json.encodeToString(recommendedTextbooks)
)

fun GradeRecordEntity.toDomain() = GradeRecord(
    unitCode = unitCode,
    unitTitle = unitTitle,
    academicYear = academicYear,
    semester = semester,
    credits = credits,
    catScore = catScore,
    examScore = examScore,
    totalScore = totalScore,
    gradeLetter = gradeLetter,
    isPass = isPass,
    isSupplementary = isSupplementary,
    isSpecial = isSpecial,
    remarks = remarks
)

fun GradeRecord.toEntity() = GradeRecordEntity(
    unitCode = unitCode,
    unitTitle = unitTitle,
    academicYear = academicYear,
    semester = semester,
    credits = credits,
    catScore = catScore,
    examScore = examScore,
    totalScore = totalScore,
    gradeLetter = gradeLetter,
    isPass = isPass,
    isSupplementary = isSupplementary,
    isSpecial = isSpecial,
    remarks = remarks
)

fun AttendanceSummaryEntity.toDomain() = AttendanceSummary(
    unitCode = unitCode,
    unitTitle = unitTitle,
    totalLecturesHeld = totalLecturesHeld,
    lecturesAttended = lecturesAttended,
    totalLabSessionsHeld = totalLabSessionsHeld,
    labSessionsAttended = labSessionsAttended,
    weeklyBreakdown = runCatching { json.decodeFromString<List<AttendanceWeekRecord>>(weeklyBreakdownJson) }.getOrDefault(emptyList()),
    recentSessions = runCatching { json.decodeFromString<List<ClassSessionAttendance>>(recentSessionsJson) }.getOrDefault(emptyList())
)

fun AttendanceSummary.toEntity() = AttendanceSummaryEntity(
    unitCode = unitCode,
    unitTitle = unitTitle,
    totalLecturesHeld = totalLecturesHeld,
    lecturesAttended = lecturesAttended,
    totalLabSessionsHeld = totalLabSessionsHeld,
    labSessionsAttended = labSessionsAttended,
    weeklyBreakdownJson = json.encodeToString(weeklyBreakdown),
    recentSessionsJson = json.encodeToString(recentSessions)
)

fun TimetableEntity.toDomain() = TimetableItem(
    id = id,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    unitCode = unitCode,
    unitTitle = unitTitle,
    lecturer = lecturer,
    lecturerEmail = lecturerEmail,
    venue = venue,
    campus = campus,
    classType = classType,
    isOnline = isOnline,
    notes = notes
)

fun TimetableItem.toEntity() = TimetableEntity(
    id = id,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    unitCode = unitCode,
    unitTitle = unitTitle,
    lecturer = lecturer,
    lecturerEmail = lecturerEmail,
    venue = venue,
    campus = campus,
    classType = classType,
    isOnline = isOnline,
    notes = notes
)

fun HostelHallEntity.toDomain() = HostelHall(
    hallId = hallId,
    hallName = hallName,
    campus = campus,
    genderTarget = genderTarget,
    totalRooms = totalRooms,
    availableRooms = availableRooms,
    rentPerSemester = rentPerSemester,
    amenities = runCatching { json.decodeFromString<List<String>>(amenitiesJson) }.getOrDefault(emptyList()),
    imageUrl = imageUrl,
    isBookingOpen = isBookingOpen
)

fun HostelHall.toEntity() = HostelHallEntity(
    hallId = hallId,
    hallName = hallName,
    campus = campus,
    genderTarget = genderTarget,
    totalRooms = totalRooms,
    availableRooms = availableRooms,
    rentPerSemester = rentPerSemester,
    amenitiesJson = json.encodeToString(amenities),
    imageUrl = imageUrl,
    isBookingOpen = isBookingOpen
)

fun HostelBookingEntity.toDomain() = HostelRoomBooking(
    bookingId = bookingId,
    regNo = regNo,
    hallName = hallName,
    roomNumber = roomNumber,
    bedSpace = bedSpace,
    academicYear = academicYear,
    semester = semester,
    rentAmount = rentAmount,
    isPaid = isPaid,
    isKeyIssued = isKeyIssued,
    bookedDate = bookedDate
)

fun HostelRoomBooking.toEntity() = HostelBookingEntity(
    bookingId = bookingId,
    regNo = regNo,
    hallName = hallName,
    roomNumber = roomNumber,
    bedSpace = bedSpace,
    academicYear = academicYear,
    semester = semester,
    rentAmount = rentAmount,
    isPaid = isPaid,
    isKeyIssued = isKeyIssued,
    bookedDate = bookedDate
)

fun FeeStatementEntity.toDomain() = FeeStatement(
    academicYear = academicYear,
    semester = semester,
    totalInvoiced = totalInvoiced,
    totalPaid = totalPaid,
    outstandingBalance = outstandingBalance,
    helbDisbursed = helbDisbursed,
    hefScholarship = hefScholarship,
    invoiceBreakdown = runCatching { json.decodeFromString<Map<String, Double>>(invoiceBreakdownJson) }.getOrDefault(emptyMap()),
    transactions = runCatching { json.decodeFromString<List<FeeTransaction>>(transactionsJson) }.getOrDefault(emptyList())
)

fun FeeStatement.toEntity() = FeeStatementEntity(
    academicYear = academicYear,
    semester = semester,
    totalInvoiced = totalInvoiced,
    totalPaid = totalPaid,
    outstandingBalance = outstandingBalance,
    helbDisbursed = helbDisbursed,
    hefScholarship = hefScholarship,
    invoiceBreakdownJson = json.encodeToString(invoiceBreakdown),
    transactionsJson = json.encodeToString(transactions)
)

fun ExamCardEntity.toDomain() = ExamCard(
    cardId = cardId,
    regNo = regNo,
    studentName = studentName,
    faculty = faculty,
    program = program,
    academicYear = academicYear,
    semester = semester,
    passportPhotoUrl = passportPhotoUrl,
    isFeeCleared = isFeeCleared,
    isUnitsApproved = isUnitsApproved,
    qrVerificationToken = qrVerificationToken,
    units = runCatching { json.decodeFromString<List<ExamCardItem>>(unitsJson) }.getOrDefault(emptyList()),
    generatedDate = generatedDate,
    authorizedBy = authorizedBy
)

fun ExamCard.toEntity() = ExamCardEntity(
    regNo = regNo,
    cardId = cardId,
    studentName = studentName,
    faculty = faculty,
    program = program,
    academicYear = academicYear,
    semester = semester,
    passportPhotoUrl = passportPhotoUrl,
    isFeeCleared = isFeeCleared,
    isUnitsApproved = isUnitsApproved,
    qrVerificationToken = qrVerificationToken,
    unitsJson = json.encodeToString(units),
    generatedDate = generatedDate,
    authorizedBy = authorizedBy
)

fun ExamTimetableEntity.toDomain() = ExamTimetableItem(
    id = id,
    unitCode = unitCode,
    unitTitle = unitTitle,
    examDate = examDate,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    session = session,
    venue = venue,
    campus = campus,
    faculty = faculty,
    chiefInvigilator = chiefInvigilator,
    isAuthorized = isAuthorized
)

fun ExamTimetableItem.toEntity() = ExamTimetableEntity(
    id = id,
    unitCode = unitCode,
    unitTitle = unitTitle,
    examDate = examDate,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime,
    session = session,
    venue = venue,
    campus = campus,
    faculty = faculty,
    chiefInvigilator = chiefInvigilator,
    isAuthorized = isAuthorized
)

fun SpecialExamRequestEntity.toDomain() = SpecialExamRequest(
    requestId = requestId,
    regNo = regNo,
    unitCode = unitCode,
    unitTitle = unitTitle,
    academicYear = academicYear,
    semester = semester,
    reasonCategory = reasonCategory,
    explanation = explanation,
    medicalOfficerApproval = medicalOfficerApproval,
    deanApproval = deanApproval,
    status = status,
    submissionDate = submissionDate
)

fun SpecialExamRequest.toEntity() = SpecialExamRequestEntity(
    requestId = requestId,
    regNo = regNo,
    unitCode = unitCode,
    unitTitle = unitTitle,
    academicYear = academicYear,
    semester = semester,
    reasonCategory = reasonCategory,
    explanation = explanation,
    medicalOfficerApproval = medicalOfficerApproval,
    deanApproval = deanApproval,
    status = status,
    submissionDate = submissionDate
)

fun SupplementaryRequestEntity.toDomain() = SupplementaryRequest(
    requestId = requestId,
    regNo = regNo,
    unitCode = unitCode,
    unitTitle = unitTitle,
    previousScore = previousScore,
    feeAmount = feeAmount,
    paymentReference = paymentReference,
    isPaid = isPaid,
    status = status,
    submissionDate = submissionDate
)

fun SupplementaryRequest.toEntity() = SupplementaryRequestEntity(
    requestId = requestId,
    regNo = regNo,
    unitCode = unitCode,
    unitTitle = unitTitle,
    previousScore = previousScore,
    feeAmount = feeAmount,
    paymentReference = paymentReference,
    isPaid = isPaid,
    status = status,
    submissionDate = submissionDate
)

fun MissingMarksDisputeEntity.toDomain() = MissingMarksDispute(
    disputeId = disputeId,
    regNo = regNo,
    unitCode = unitCode,
    unitTitle = unitTitle,
    lecturerName = lecturerName,
    academicYear = academicYear,
    semester = semester,
    missingComponent = missingComponent,
    evidenceNote = evidenceNote,
    status = status,
    submittedDate = submittedDate
)

fun MissingMarksDispute.toEntity() = MissingMarksDisputeEntity(
    disputeId = disputeId,
    regNo = regNo,
    unitCode = unitCode,
    unitTitle = unitTitle,
    lecturerName = lecturerName,
    academicYear = academicYear,
    semester = semester,
    missingComponent = missingComponent,
    evidenceNote = evidenceNote,
    status = status,
    submittedDate = submittedDate
)
