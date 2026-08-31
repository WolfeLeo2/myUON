package com.wolfeleo2.myuon.data.sync

import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SyncDeltaMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testSyncDeltaPayloadSerializationAndRoomEntityMapping() {
        val payload = SyncDeltaPayload(
            timestamp = "2026-08-30T17:35:00Z",
            studentProfile = StudentProfile(
                regNo = "P15/12345/2022",
                fullName = "Leo K.",
                studentEmail = "leo@students.uonbi.ac.ke",
                faculty = "Faculty of Science & Technology",
                department = "Department of Computer Science",
                program = "Bachelor of Science in Computer Science",
                yearOfStudy = 3,
                semester = 2,
                campus = "Chiromo Campus",
                nationalId = "38920194",
                mobileNumber = "+254 712 345 678"
            ),
            units = listOf(
                CourseUnit(
                    unitCode = "CSC 311",
                    unitTitle = "Advanced Database Systems",
                    credits = 3,
                    academicYear = "2025/2026",
                    semester = 2,
                    lecturerName = "Prof. Peter Wagacha",
                    lecturerEmail = "pwagacha@uonbi.ac.ke",
                    venueName = "Chiromo Lab 02",
                    campus = "Chiromo Campus"
                )
            ),
            grades = listOf(
                GradeRecord(
                    unitCode = "CSC 111",
                    unitTitle = "Introduction to Computer Science",
                    academicYear = "2023/2024",
                    semester = 1,
                    credits = 3,
                    catScore = 26.0,
                    examScore = 56.0,
                    totalScore = 82.0,
                    gradeLetter = "A",
                    isPass = true
                )
            ),
            attendanceOverview = listOf(
                AttendanceSummary(
                    unitCode = "CSC 311",
                    unitTitle = "Advanced Database Systems",
                    totalLecturesHeld = 12,
                    lecturesAttended = 11,
                    totalLabSessionsHeld = 12,
                    labSessionsAttended = 10,
                    weeklyBreakdown = listOf(
                        AttendanceWeekRecord(
                            weekLabel = "Week 1",
                            weekStartDate = "Mon, Jan 12",
                            sessions = listOf(
                                ClassSessionAttendance(
                                    id = "sess-1",
                                    date = "2026-01-12",
                                    timeSlot = "09:00 - 11:00",
                                    sessionType = "Lecture",
                                    topicCovered = "Week 1 Session 1",
                                    hours = 2.0,
                                    isAttended = true,
                                    venue = "Chiromo Lab 02"
                                )
                            )
                        )
                    )
                )
            ),
            feeStatement = FeeStatement(
                academicYear = "2025/2026",
                semester = 2,
                totalInvoiced = 39250.0,
                totalPaid = 39250.0,
                outstandingBalance = 0.0,
                helbDisbursed = 20000.0,
                hefScholarship = 15000.0,
                invoiceBreakdown = mapOf("Tuition" to 30000.0, "Exam Fee" to 5000.0),
                transactions = listOf(
                    FeeTransaction(
                        id = "tx-1",
                        referenceNumber = "MPESA12345",
                        date = "2026-05-15",
                        description = "Tuition Payment",
                        type = TransactionType.PAYMENT_MPESA,
                        amount = 4250.0,
                        balanceAfter = 0.0,
                        isVerified = true
                    )
                )
            ),
            hostelBooking = HostelRoomBooking(
                bookingId = "BK-1001",
                regNo = "P15/12345/2022",
                hallName = "Hall 3",
                roomNumber = "12",
                bedSpace = "A",
                academicYear = "2025/2026",
                semester = 2,
                rentAmount = 6500.0,
                isPaid = true,
                isKeyIssued = true,
                bookedDate = "2026-05-10"
            ),
            examCard = ExamCard(
                cardId = "EC-9999",
                regNo = "P15/12345/2022",
                studentName = "Leo K.",
                faculty = "Faculty of Science & Technology",
                program = "BSc Computer Science",
                academicYear = "2025/2026",
                semester = 2,
                passportPhotoUrl = null,
                isFeeCleared = true,
                isUnitsApproved = true,
                qrVerificationToken = "UON-VERIFY-12345",
                units = listOf(
                    ExamCardItem(
                        unitCode = "CSC 311",
                        unitTitle = "Advanced Database Systems",
                        examDate = "2026-09-18",
                        examTime = "09:00 - 11:00",
                        venue = "Chiromo Lab 02",
                        deskNumber = "DK-12"
                    )
                ),
                generatedDate = "2026-08-30"
            ),
            examTimetable = listOf(
                ExamTimetableItem(
                    id = "exam-1",
                    unitCode = "CSC 311",
                    unitTitle = "Advanced Database Systems",
                    examDate = "2026-09-18",
                    dayOfWeek = "Friday",
                    startTime = "09:00",
                    endTime = "11:00",
                    session = "Morning Session",
                    venue = "Chiromo Lab 02",
                    campus = "Chiromo Campus",
                    faculty = "Faculty of Science & Technology",
                    chiefInvigilator = "Prof. R. Okoth"
                )
            ),
            academicRequests = AcademicRequestsResponse(
                specialExams = listOf(
                    SpecialExamRequest(
                        requestId = "sp-1",
                        regNo = "P15/12345/2022",
                        unitCode = "CSC 315",
                        unitTitle = "Operating Systems Principles",
                        academicYear = "2025/2026",
                        semester = 2,
                        reasonCategory = "Medical",
                        explanation = "Hospitalized during exam period",
                        status = RequestStatus.PENDING,
                        submissionDate = "2026-08-30"
                    )
                ),
                supplementaries = listOf(
                    SupplementaryRequest(
                        requestId = "sup-1",
                        regNo = "P15/12345/2022",
                        unitCode = "SMA 201",
                        unitTitle = "Calculus II",
                        previousScore = 38.0,
                        feeAmount = 1000.0,
                        paymentReference = "MPESA789",
                        isPaid = true,
                        status = RequestStatus.APPROVED,
                        submissionDate = "2026-08-20"
                    )
                ),
                missingMarks = listOf(
                    MissingMarksDispute(
                        disputeId = "mmd-1",
                        regNo = "P15/12345/2022",
                        unitCode = "CSC 321",
                        unitTitle = "Software Engineering II",
                        lecturerName = "Dr. Lawrence Muchemi",
                        academicYear = "2025/2026",
                        semester = 2,
                        missingComponent = "CAT",
                        evidenceNote = "Submitted to online portal",
                        status = RequestStatus.PENDING,
                        submittedDate = "2026-08-28"
                    )
                )
            )
        )

        // 1. Test JSON Serialization
        val jsonStr = json.encodeToString(payload)
        val decoded = json.decodeFromString<SyncDeltaPayload>(jsonStr)
        assertEquals(payload.timestamp, decoded.timestamp)
        assertEquals(1, decoded.units.size)
        assertEquals("CSC 311", decoded.units[0].unitCode)
        assertNotNull(decoded.feeStatement)
        assertNotNull(decoded.hostelBooking)
        assertNotNull(decoded.examCard)
        assertEquals(1, decoded.examTimetable.size)
        assertNotNull(decoded.academicRequests)

        // 2. Test Entity conversion for Room upsert
        val unitEntity = decoded.units[0].toEntity()
        assertEquals("CSC 311", unitEntity.unitCode)
        val unitDomain = unitEntity.toDomain()
        assertEquals("CSC 311", unitDomain.unitCode)

        val studentEntity = decoded.studentProfile!!.toEntity()
        assertEquals("P15/12345/2022", studentEntity.regNo)
        val studentDomain = studentEntity.toDomain()
        assertEquals("Leo K.", studentDomain.fullName)

        val gradeEntity = decoded.grades[0].toEntity()
        assertEquals("CSC 111", gradeEntity.unitCode)
        assertEquals("A", gradeEntity.gradeLetter)

        assertEquals(1, decoded.attendanceOverview.size)
        val attendanceEntity = decoded.attendanceOverview[0].toEntity()
        assertEquals("CSC 311", attendanceEntity.unitCode)
        assertEquals(11, attendanceEntity.lecturesAttended)
        val attendanceDomain = attendanceEntity.toDomain()
        assertEquals("CSC 311", attendanceDomain.unitCode)

        // Fee statement entity & domain test
        val feeEntity = decoded.feeStatement!!.toEntity()
        assertEquals("2025/2026", feeEntity.academicYear)
        assertEquals(39250.0, feeEntity.totalInvoiced, 0.001)
        val feeDomain = feeEntity.toDomain()
        assertEquals(1, feeDomain.transactions.size)
        assertEquals("MPESA12345", feeDomain.transactions[0].referenceNumber)

        // Hostel booking entity & domain test
        val bookingEntity = decoded.hostelBooking!!.toEntity()
        assertEquals("BK-1001", bookingEntity.bookingId)
        assertEquals("Hall 3", bookingEntity.hallName)
        val bookingDomain = bookingEntity.toDomain()
        assertEquals("12", bookingDomain.roomNumber)
        assertEquals(true, bookingDomain.isPaid)

        // Exam card entity & domain test
        val examCardEntity = decoded.examCard!!.toEntity()
        assertEquals("EC-9999", examCardEntity.cardId)
        val examCardDomain = examCardEntity.toDomain()
        assertEquals(1, examCardDomain.units.size)
        assertEquals("CSC 311", examCardDomain.units[0].unitCode)
        assertEquals("DK-12", examCardDomain.units[0].deskNumber)

        // Exam timetable entity & domain test
        val examSlotEntity = decoded.examTimetable[0].toEntity()
        assertEquals("exam-1", examSlotEntity.id)
        val examSlotDomain = examSlotEntity.toDomain()
        assertEquals("09:00", examSlotDomain.startTime)

        // Academic requests entity & domain test
        val requests = decoded.academicRequests!!
        val specialExamEntity = requests.specialExams[0].toEntity()
        assertEquals("sp-1", specialExamEntity.requestId)
        assertEquals("CSC 315", specialExamEntity.unitCode)
        val specialExamDomain = specialExamEntity.toDomain()
        assertEquals(RequestStatus.PENDING, specialExamDomain.status)

        val suppEntity = requests.supplementaries[0].toEntity()
        assertEquals("sup-1", suppEntity.requestId)
        val suppDomain = suppEntity.toDomain()
        assertEquals(38.0, suppDomain.previousScore, 0.001)
        assertEquals("MPESA789", suppDomain.paymentReference)

        val disputeEntity = requests.missingMarks[0].toEntity()
        assertEquals("mmd-1", disputeEntity.disputeId)
        val disputeDomain = disputeEntity.toDomain()
        assertEquals("CAT", disputeDomain.missingComponent)
    }

    @Test
    fun testSyncEventSerialization() {
        val event = SyncEvent(
            eventType = "DELTA_AVAILABLE",
            scope = SyncScope.UNITS,
            regNo = "P15/12345/2022",
            timestamp = "2026-08-30T17:35:00Z",
            description = "CSC 311 Syllabus Updated"
        )
        val jsonStr = json.encodeToString(event)
        val decoded = json.decodeFromString<SyncEvent>(jsonStr)

        assertEquals("DELTA_AVAILABLE", decoded.eventType)
        assertEquals(SyncScope.UNITS, decoded.scope)
        assertEquals("P15/12345/2022", decoded.regNo)
        assertEquals("CSC 311 Syllabus Updated", decoded.description)
    }
}
