package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.AcademicRepository
import com.wolfeleo2.myuon.server.domain.AttendanceSummary
import com.wolfeleo2.myuon.server.domain.AttendanceWeekRecord
import com.wolfeleo2.myuon.server.domain.ClassSessionAttendance
import com.wolfeleo2.myuon.server.domain.ClassType
import com.wolfeleo2.myuon.server.domain.CourseUnit
import com.wolfeleo2.myuon.server.domain.ExamTimetableItem
import com.wolfeleo2.myuon.server.domain.GradeRecord
import com.wolfeleo2.myuon.server.domain.MissingMarksDispute
import com.wolfeleo2.myuon.server.domain.RequestStatus
import com.wolfeleo2.myuon.server.domain.SpecialExamRequest
import com.wolfeleo2.myuon.server.domain.SupplementaryRequest
import com.wolfeleo2.myuon.server.domain.SyllabusTopic
import com.wolfeleo2.myuon.server.domain.TimetableItem
import com.wolfeleo2.myuon.server.domain.UnitRegistration
import com.wolfeleo2.myuon.server.domain.UnitStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ExposedAcademicRepository(private val database: Database) : AcademicRepository {

    override suspend fun getAvailableUnits(
        academicYear: String,
        semester: Int,
        yearOfStudy: Int?
    ): List<CourseUnit> = dbQuery {
        val query = if (yearOfStudy != null) {
            UnitsTable.selectAll().where { (UnitsTable.semester eq semester) and (UnitsTable.yearOfStudy eq yearOfStudy) }
        } else {
            UnitsTable.selectAll().where { UnitsTable.semester eq semester }
        }
        query.map { it.toCourseUnit() }
    }

    override suspend fun getUnitByCode(unitCode: String): CourseUnit? = dbQuery {
        UnitsTable.selectAll().where { UnitsTable.code eq unitCode }
            .map { it.toCourseUnit() }
            .singleOrNull()
    }

    override suspend fun getRegisteredUnits(regNo: String, academicYear: String, semester: Int): List<CourseUnit> = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
            ?: return@dbQuery emptyList()
        val studentId = student[StudentsTable.userId]

        val unitCodes = UnitRegistrationsTable.selectAll().where {
            (UnitRegistrationsTable.studentId eq studentId) and
                (UnitRegistrationsTable.academicYear eq academicYear) and
                (UnitRegistrationsTable.semester eq semester)
        }.map { it[UnitRegistrationsTable.unitCode] }

        UnitsTable.selectAll().where { UnitsTable.code inList unitCodes }
            .map { it.toCourseUnit(UnitStatus.APPROVED) }
    }

    override suspend fun registerUnits(
        regNo: String,
        unitCodes: List<String>,
        academicYear: String,
        semester: Int
    ): List<UnitRegistration> = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
            ?: error("Student not found for regNo: $regNo")
        val studentId = student[StudentsTable.userId]
        val studentYear = student[StudentsTable.yearOfStudy]

        // Validate that all units belong to the student's year of study and requested semester
        val validUnits = UnitsTable.selectAll().where {
            (UnitsTable.code inList unitCodes) and
            (UnitsTable.yearOfStudy eq studentYear) and
            (UnitsTable.semester eq semester)
        }.map { it[UnitsTable.code] }.toSet()

        val invalidUnits = unitCodes.filterNot { it in validUnits }
        require(invalidUnits.isEmpty()) {
            "Units ${invalidUnits.joinToString()} do not belong to Year $studentYear Semester $semester"
        }

        val now = java.time.Instant.now().toString()
        unitCodes.map { code ->
            val id = UUID.randomUUID()
            UnitRegistrationsTable.insert {
                it[UnitRegistrationsTable.id] = id
                it[UnitRegistrationsTable.studentId] = studentId
                it[UnitRegistrationsTable.unitCode] = code
                it[UnitRegistrationsTable.academicYear] = academicYear
                it[UnitRegistrationsTable.semester] = semester
                it[UnitRegistrationsTable.status] = "APPROVED"
                it[UnitRegistrationsTable.registeredAt] = now
            }
            UnitRegistration(
                registrationId = id.toString(),
                regNo = regNo,
                unitCode = code,
                academicYear = academicYear,
                semester = semester,
                status = UnitStatus.APPROVED,
                registeredAt = now
            )
        }
    }

    override suspend fun getGradeRecords(regNo: String): List<GradeRecord> = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
            ?: return@dbQuery emptyList()
        val studentId = student[StudentsTable.userId]

        GradeRecordsTable.selectAll().where { GradeRecordsTable.studentId eq studentId }
            .map { row ->
                val code = row[GradeRecordsTable.unitCode]
                val unit = UnitsTable.selectAll().where { UnitsTable.code eq code }.singleOrNull()
                val title = unit?.get(UnitsTable.title) ?: code
                GradeRecord(
                    id = row[GradeRecordsTable.id].toString(),
                    regNo = regNo,
                    unitCode = code,
                    unitTitle = title,
                    academicYear = row[GradeRecordsTable.academicYear],
                    semester = row[GradeRecordsTable.semester],
                    credits = unit?.get(UnitsTable.credits) ?: 3,
                    catScore = row[GradeRecordsTable.catMark],
                    examScore = row[GradeRecordsTable.examMark],
                    totalScore = row[GradeRecordsTable.totalScore],
                    gradeLetter = row[GradeRecordsTable.gradeLetter],
                    isPass = row[GradeRecordsTable.isPass],
                    isSupplementary = row[GradeRecordsTable.isSupplementary],
                    isSpecial = row[GradeRecordsTable.isSpecial]
                )
            }
    }

    override suspend fun getTimetable(regNo: String): List<TimetableItem> = dbQuery {
        val student = if (regNo.isNotBlank()) {
            StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
        } else null

        val unitCodes = if (student != null) {
            val registered = UnitRegistrationsTable.selectAll()
                .where { UnitRegistrationsTable.studentId eq student[StudentsTable.userId] }
                .map { it[UnitRegistrationsTable.unitCode] }

            if (registered.isNotEmpty()) {
                registered
            } else {
                UnitsTable.selectAll()
                    .where { (UnitsTable.yearOfStudy eq student[StudentsTable.yearOfStudy]) and (UnitsTable.semester eq student[StudentsTable.semester]) }
                    .map { it[UnitsTable.code] }
            }
        } else emptyList()

        val query = if (unitCodes.isNotEmpty()) {
            TimetableItemsTable.selectAll().where { TimetableItemsTable.unitCode inList unitCodes }
        } else {
            TimetableItemsTable.selectAll()
        }

        query.map { row ->
            val code = row[TimetableItemsTable.unitCode]
            val unit = UnitsTable.selectAll().where { UnitsTable.code eq code }.singleOrNull()
            TimetableItem(
                id = row[TimetableItemsTable.id].toString(),
                dayOfWeek = row[TimetableItemsTable.dayOfWeek],
                startTime = row[TimetableItemsTable.startTime],
                endTime = row[TimetableItemsTable.endTime],
                unitCode = code,
                unitTitle = unit?.get(UnitsTable.title) ?: code,
                lecturer = unit?.get(UnitsTable.lecturerName) ?: "Lecturer",
                lecturerEmail = unit?.get(UnitsTable.lecturerEmail) ?: "",
                venue = "${row[TimetableItemsTable.room]}, ${row[TimetableItemsTable.building]}",
                campus = unit?.get(UnitsTable.campus) ?: "Chiromo",
                classType = runCatching { ClassType.valueOf(row[TimetableItemsTable.sessionType].uppercase()) }.getOrDefault(ClassType.LECTURE)
            )
        }
    }

    override suspend fun getExamTimetable(regNo: String): List<ExamTimetableItem> = dbQuery {
        ExamTimetableItemsTable.selectAll().map { row ->
            val code = row[ExamTimetableItemsTable.unitCode]
            val unit = UnitsTable.selectAll().where { UnitsTable.code eq code }.singleOrNull()
            ExamTimetableItem(
                id = row[ExamTimetableItemsTable.id].toString(),
                unitCode = code,
                unitTitle = unit?.get(UnitsTable.title) ?: code,
                examDate = row[ExamTimetableItemsTable.examDate],
                startTime = row[ExamTimetableItemsTable.startTime],
                endTime = row[ExamTimetableItemsTable.endTime],
                venue = row[ExamTimetableItemsTable.venue],
                campus = "Main Campus"
            )
        }
    }

    override suspend fun getAttendanceSessions(unitCode: String): List<ClassSessionAttendance> = dbQuery {
        AttendanceSessionsTable.selectAll().where { AttendanceSessionsTable.unitCode eq unitCode }
            .map { row ->
                ClassSessionAttendance(
                    id = row[AttendanceSessionsTable.id].toString(),
                    unitCode = unitCode,
                    date = row[AttendanceSessionsTable.sessionDate],
                    timeSlot = "09:00 - 11:00",
                    sessionType = "Lecture",
                    topicCovered = "Session ${row[AttendanceSessionsTable.sessionNumber]}",
                    hours = 2.0,
                    isAttended = true,
                    venue = "LT 1"
                )
            }
    }

    override suspend fun getStudentAttendance(regNo: String, unitCode: String): AttendanceSummary? = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
            ?: return@dbQuery null
        val unit = UnitsTable.selectAll().where { UnitsTable.code eq unitCode }.singleOrNull()
            ?: return@dbQuery null

        val sessions = AttendanceSessionsTable.selectAll().where { AttendanceSessionsTable.unitCode eq unitCode }
            .map { it.toSessionAttendance() }

        AttendanceSummary(
            unitCode = unitCode,
            unitTitle = unit[UnitsTable.title],
            totalLecturesHeld = sessions.size,
            lecturesAttended = sessions.count { it.isAttended },
            totalLabSessionsHeld = 0,
            labSessionsAttended = 0,
            weeklyBreakdown = listOf(AttendanceWeekRecord("Week 1", "2026-01-12", sessions)),
            recentSessions = sessions
        )
    }

    override suspend fun submitSpecialExamRequest(request: SpecialExamRequest): SpecialExamRequest = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq request.regNo }.singleOrNull()
        val studentId = student?.get(StudentsTable.userId) ?: request.regNo
        val uuid = runCatching { UUID.fromString(request.requestId) }.getOrElse { UUID.randomUUID() }
        AcademicRequestsTable.insert {
            it[AcademicRequestsTable.id] = uuid
            it[AcademicRequestsTable.studentId] = studentId
            it[AcademicRequestsTable.requestType] = "SPECIAL_EXAM"
            it[AcademicRequestsTable.unitCode] = request.unitCode
            it[AcademicRequestsTable.academicYear] = request.academicYear
            it[AcademicRequestsTable.semester] = request.semester
            it[AcademicRequestsTable.reason] = request.explanation
            it[AcademicRequestsTable.status] = request.status.name
            it[AcademicRequestsTable.submittedAt] = request.submissionDate
        }
        request.copy(requestId = uuid.toString())
    }

    override suspend fun submitSupplementaryRequest(request: SupplementaryRequest): SupplementaryRequest = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq request.regNo }.singleOrNull()
        val studentId = student?.get(StudentsTable.userId) ?: request.regNo
        val uuid = runCatching { UUID.fromString(request.requestId) }.getOrElse { UUID.randomUUID() }
        AcademicRequestsTable.insert {
            it[AcademicRequestsTable.id] = uuid
            it[AcademicRequestsTable.studentId] = studentId
            it[AcademicRequestsTable.requestType] = "SUPPLEMENTARY"
            it[AcademicRequestsTable.unitCode] = request.unitCode
            it[AcademicRequestsTable.academicYear] = "2025/2026"
            it[AcademicRequestsTable.semester] = 2
            it[AcademicRequestsTable.reason] = "Previous Score: ${request.previousScore}"
            it[AcademicRequestsTable.status] = request.status.name
            it[AcademicRequestsTable.submittedAt] = request.submissionDate
        }
        request.copy(requestId = uuid.toString())
    }

    override suspend fun submitMissingMarksDispute(request: MissingMarksDispute): MissingMarksDispute = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq request.regNo }.singleOrNull()
        val studentId = student?.get(StudentsTable.userId) ?: request.regNo
        val uuid = runCatching { UUID.fromString(request.disputeId) }.getOrElse { UUID.randomUUID() }
        AcademicRequestsTable.insert {
            it[AcademicRequestsTable.id] = uuid
            it[AcademicRequestsTable.studentId] = studentId
            it[AcademicRequestsTable.requestType] = "MISSING_MARKS"
            it[AcademicRequestsTable.unitCode] = request.unitCode
            it[AcademicRequestsTable.academicYear] = request.academicYear
            it[AcademicRequestsTable.semester] = request.semester
            it[AcademicRequestsTable.reason] = "${request.missingComponent}: ${request.evidenceNote}"
            it[AcademicRequestsTable.status] = request.status.name
            it[AcademicRequestsTable.submittedAt] = request.submittedDate
        }
        request.copy(disputeId = uuid.toString())
    }

    override suspend fun getRequests(regNo: String): List<Any> = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
        val studentId = student?.get(StudentsTable.userId) ?: regNo

        AcademicRequestsTable.selectAll().where { AcademicRequestsTable.studentId eq studentId }
            .map { row ->
                val type = row[AcademicRequestsTable.requestType]
                val unitCode = row[AcademicRequestsTable.unitCode]
                val unit = UnitsTable.selectAll().where { UnitsTable.code eq unitCode }.singleOrNull()
                val unitTitle = unit?.get(UnitsTable.title) ?: unitCode
                when (type) {
                    "SPECIAL_EXAM" -> SpecialExamRequest(
                        requestId = row[AcademicRequestsTable.id].toString(),
                        regNo = regNo,
                        unitCode = unitCode,
                        unitTitle = unitTitle,
                        academicYear = row[AcademicRequestsTable.academicYear],
                        semester = row[AcademicRequestsTable.semester],
                        reasonCategory = "Medical",
                        explanation = row[AcademicRequestsTable.reason],
                        status = RequestStatus.valueOf(row[AcademicRequestsTable.status]),
                        submissionDate = row[AcademicRequestsTable.submittedAt] ?: ""
                    )
                    "SUPPLEMENTARY" -> SupplementaryRequest(
                        requestId = row[AcademicRequestsTable.id].toString(),
                        regNo = regNo,
                        unitCode = unitCode,
                        unitTitle = unitTitle,
                        previousScore = 35.0,
                        status = RequestStatus.valueOf(row[AcademicRequestsTable.status]),
                        submissionDate = row[AcademicRequestsTable.submittedAt] ?: ""
                    )
                    else -> MissingMarksDispute(
                        disputeId = row[AcademicRequestsTable.id].toString(),
                        regNo = regNo,
                        unitCode = unitCode,
                        unitTitle = unitTitle,
                        lecturerName = "Department",
                        academicYear = row[AcademicRequestsTable.academicYear],
                        semester = row[AcademicRequestsTable.semester],
                        missingComponent = "CAT / Exam",
                        evidenceNote = row[AcademicRequestsTable.reason],
                        status = RequestStatus.valueOf(row[AcademicRequestsTable.status]),
                        submittedDate = row[AcademicRequestsTable.submittedAt] ?: ""
                    )
                }
            }
    }

    private val jsonParser = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    private fun ResultRow.toCourseUnit(status: UnitStatus = UnitStatus.AVAILABLE): CourseUnit {
        val syllabus = runCatching {
            jsonParser.decodeFromString<List<SyllabusTopic>>(this[UnitsTable.syllabusTopicsJson])
        }.getOrDefault(emptyList())

        val outcomes = runCatching {
            jsonParser.decodeFromString<List<String>>(this[UnitsTable.learningOutcomesJson])
        }.getOrDefault(emptyList())

        val textbooks = runCatching {
            jsonParser.decodeFromString<List<String>>(this[UnitsTable.recommendedTextbooksJson])
        }.getOrDefault(emptyList())

        val prereqs = runCatching {
            jsonParser.decodeFromString<List<String>>(this[UnitsTable.prerequisitesJson])
        }.getOrDefault(emptyList())

        return CourseUnit(
            unitCode = this[UnitsTable.code],
            unitTitle = this[UnitsTable.title],
            credits = this[UnitsTable.credits],
            academicYear = "2025/2026",
            semester = this[UnitsTable.semester],
            lecturerName = this[UnitsTable.lecturerName],
            lecturerEmail = this[UnitsTable.lecturerEmail] ?: "",
            lecturerOffice = this[UnitsTable.lecturerOffice],
            venueName = this[UnitsTable.venueName],
            campus = this[UnitsTable.campus],
            isCore = this[UnitsTable.isCore],
            prerequisites = prereqs,
            status = status,
            scheduleTime = this[UnitsTable.scheduleTime],
            description = this[UnitsTable.description],
            syllabusTopics = syllabus,
            learningOutcomes = outcomes,
            recommendedTextbooks = textbooks
        )
    }

    private fun ResultRow.toSessionAttendance() = ClassSessionAttendance(
        id = this[AttendanceSessionsTable.id].toString(),
        unitCode = this[AttendanceSessionsTable.unitCode],
        date = this[AttendanceSessionsTable.sessionDate],
        timeSlot = "09:00 - 11:00",
        sessionType = "Lecture",
        topicCovered = "Session ${this[AttendanceSessionsTable.sessionNumber]}",
        hours = 2.0,
        isAttended = true,
        venue = "LT 1"
    )

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction(database) { block() } }
}
