package com.wolfeleo2.myuon.server.db

import org.jetbrains.exposed.sql.Table

object UnitsTable : Table("units") {
    val code = varchar("code", 20)
    val title = varchar("title", 255)
    val credits = integer("credits").default(3)
    val lecturerName = varchar("lecturer_name", 255)
    val lecturerEmail = varchar("lecturer_email", 255).nullable()
    val lecturerOffice = varchar("lecturer_office", 255).default("Department of Computer Science, Chiromo")
    val venueName = varchar("venue_name", 100).default("Lecture Theatre")
    val campus = varchar("campus", 100).default("Chiromo")
    val department = varchar("department", 255)
    val yearOfStudy = integer("year_of_study")
    val semester = integer("semester")
    val isCore = bool("is_core").default(true)
    val scheduleTime = varchar("schedule_time", 100).default("Mon 09:00 - 11:00")
    val description = text("description").default("")
    val syllabusTopicsJson = text("syllabus_topics_json").default("[]")
    val learningOutcomesJson = text("learning_outcomes_json").default("[]")
    val recommendedTextbooksJson = text("recommended_textbooks_json").default("[]")
    val prerequisitesJson = text("prerequisites_json").default("[]")

    override val primaryKey = PrimaryKey(code)
}

object UnitRegistrationsTable : Table("unit_registrations") {
    val id = uuid("id")
    val studentId = varchar("student_id", 64)
    val unitCode = varchar("unit_code", 20)
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val status = varchar("status", 20).default("REGISTERED")
    val registeredAt = varchar("registered_at", 64).nullable()

    override val primaryKey = PrimaryKey(id)
}

object GradeRecordsTable : Table("grade_records") {
    val id = uuid("id")
    val studentId = varchar("student_id", 64)
    val unitCode = varchar("unit_code", 20)
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val catMark = double("cat_mark").default(0.0)
    val examMark = double("exam_mark").default(0.0)
    val totalScore = double("total_score").default(0.0)
    val gradeLetter = varchar("grade_letter", 5)
    val isPass = bool("is_pass").default(true)
    val isSupplementary = bool("is_supplementary").default(false)
    val isSpecial = bool("is_special").default(false)

    override val primaryKey = PrimaryKey(id)
}

object TimetableItemsTable : Table("timetable_items") {
    val id = uuid("id")
    val unitCode = varchar("unit_code", 20)
    val dayOfWeek = varchar("day_of_week", 15)
    val startTime = varchar("start_time", 20)
    val endTime = varchar("end_time", 20)
    val room = varchar("room", 100)
    val building = varchar("building", 100)
    val sessionType = varchar("session_type", 50).default("LECTURE")

    override val primaryKey = PrimaryKey(id)
}

object ExamTimetableItemsTable : Table("exam_timetable_items") {
    val id = uuid("id")
    val unitCode = varchar("unit_code", 20)
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val examDate = varchar("exam_date", 30)
    val startTime = varchar("start_time", 20)
    val endTime = varchar("end_time", 20)
    val venue = varchar("venue", 100)
    val seatNumber = varchar("seat_number", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}

object AttendanceSessionsTable : Table("attendance_sessions") {
    val id = uuid("id")
    val unitCode = varchar("unit_code", 20)
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val weekNumber = integer("week_number")
    val sessionNumber = integer("session_number")
    val sessionDate = varchar("session_date", 30)

    override val primaryKey = PrimaryKey(id)
}

object StudentAttendanceTable : Table("student_attendance") {
    val id = uuid("id")
    val sessionId = uuid("session_id")
    val studentId = varchar("student_id", 64)
    val isAttended = bool("is_attended").default(false)
    val markedAt = varchar("marked_at", 64).nullable()

    override val primaryKey = PrimaryKey(id)
}

object AcademicRequestsTable : Table("academic_requests") {
    val id = uuid("id")
    val studentId = varchar("student_id", 64)
    val requestType = varchar("request_type", 30)
    val unitCode = varchar("unit_code", 20)
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val reason = text("reason")
    val documentUrl = text("document_url").nullable()
    val status = varchar("status", 20).default("PENDING")
    val reviewerComments = text("reviewer_comments").nullable()
    val submittedAt = varchar("submitted_at", 64).nullable()

    override val primaryKey = PrimaryKey(id)
}
